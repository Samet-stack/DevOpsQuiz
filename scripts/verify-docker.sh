#!/usr/bin/env bash
# Vérification Docker NON interactive pour le pipeline (ticket 9).
# Le quiz est un jeu en console : on lui envoie des saisies par un pipe au lieu d'un clavier.
#
# Usage : scripts/verify-docker.sh [image]      (image par défaut : devopsquiz:latest)
# Variable optionnelle : DATA_DIR = dossier des scores dans le conteneur (défaut : /app/data)
#
# Vérifie :
#   1. l'image existe ;
#   2. l'application démarre, affiche le menu et se ferme proprement (choix 4) ;
#   3. une saisie invalide produit un log ERROR sans faire planter l'application ;
#   4. les scores écrits dans le volume survivent à la suppression du conteneur.
# Code de sortie != 0 dès qu'une vérification échoue (le stage Jenkins échoue alors).

set -uo pipefail

IMAGE="${1:-devopsquiz:latest}"
DATA_DIR="${DATA_DIR:-/app/data}"
VOLUME="devopsquiz-verif-$$"
ECHECS=0

ok()   { echo "  OK    $1"; }
ko()   { echo "  ECHEC $1"; ECHECS=$((ECHECS + 1)); }

nettoyage() { docker volume rm "$VOLUME" >/dev/null 2>&1 || true; }
trap nettoyage EXIT

echo "==> 1. L'image $IMAGE existe"
if docker image inspect "$IMAGE" >/dev/null 2>&1; then
  ok "image trouvée"
else
  ko "image introuvable : $IMAGE (lancer docker build avant)"
  exit 1
fi

echo "==> 2. Démarrage, menu, sortie propre"
SORTIE=$(printf '4\n' | docker run --rm -i "$IMAGE" 2>&1)
CODE=$?
[ "$CODE" -eq 0 ] && ok "code de sortie 0" || ko "code de sortie $CODE"
echo "$SORTIE" | grep -q "DEVOPS QUIZ" && ok "menu affiché" || ko "menu non affiché"
echo "$SORTIE" | grep -q "Lancement de DevOpsQuiz" && ok "log de démarrage présent" || ko "log de démarrage absent"

echo "==> 3. Saisie invalide : log ERROR, pas de plantage"
SORTIE=$(printf 'abc\n4\n' | docker run --rm -i "$IMAGE" 2>&1)
CODE=$?
[ "$CODE" -eq 0 ] && ok "l'application continue et se ferme proprement" || ko "code de sortie $CODE"
echo "$SORTIE" | grep -q "ERROR" && ok "erreur tracée dans les logs" || ko "aucun log ERROR"

echo "==> 4. Persistance des scores avec un volume Docker"
docker volume create "$VOLUME" >/dev/null
docker run --rm -v "$VOLUME:$DATA_DIR" --entrypoint sh "$IMAGE" \
  -c "echo 'Verif;7;10;2026-01-01 10:00:00' >> $DATA_DIR/scores.csv" >/dev/null 2>&1
RELU=$(docker run --rm -v "$VOLUME:$DATA_DIR" --entrypoint sh "$IMAGE" \
  -c "cat $DATA_DIR/scores.csv" 2>/dev/null)
echo "$RELU" | grep -q "Verif;7;10" && ok "score retrouvé dans un nouveau conteneur" || ko "score perdu"

echo
if [ "$ECHECS" -eq 0 ]; then
  echo "Toutes les vérifications Docker sont passées."
  exit 0
fi
echo "$ECHECS vérification(s) en échec."
exit 1
