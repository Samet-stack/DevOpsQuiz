# Étapes 7 et 8 : OPERATE et MONITOR

Partie du rapport rédigée pour le ticket 9. Les blocs « CAPTURE » sont à remplir avec de vraies captures d'écran prises pendant la démonstration. Rien ci-dessous ne remplace un résultat réel.

Prérequis : l'image `devopsquiz` est construite (`docker build -t devopsquiz:latest .`) et le dossier des scores dans le conteneur est `/app/data`. Le volume `devopsquiz-scores` est celui utilisé par le Jenkinsfile.

Le pipeline Jenkins lance déjà un conteneur en arrière-plan nommé `devopsquiz-app` : on peut le consulter avec `docker ps`, `docker logs devopsquiz-app` et `docker stop devopsquiz-app`.

## Étape 7 : OPERATE (exploitation)

### Afficher les conteneurs et leur état

```bash
docker ps            # conteneurs en cours d'exécution
docker ps -a         # tous les conteneurs, y compris arrêtés
docker images        # images disponibles
```

CAPTURE 7.1 : sortie de `docker ps -a`.

### Lancer, arrêter, redémarrer l'application

Le quiz est interactif, il faut donc `-it` et un nom de conteneur :

```bash
docker run -it --name quiz -v devopsquiz-scores:/app/data devopsquiz:latest
docker stop quiz        # si le conteneur tourne en arrière-plan
docker start -ai quiz   # redémarrer le même conteneur
```

CAPTURE 7.2 : arrêt puis redémarrage du conteneur `quiz`.

### Recréer le conteneur et vérifier que les scores sont toujours là

```bash
# 1. jouer une partie, puis quitter (choix 4)
docker rm quiz                                         # supprime le conteneur
docker run -it --name quiz2 -v devopsquiz-scores:/app/data devopsquiz:latest
# 2. choisir 3 (Afficher les meilleurs scores) : la partie précédente doit apparaître
```

Les scores restent car ils sont écrits dans `/app/data`, qui est un volume Docker et non l'intérieur du conteneur. On peut aussi les lire directement :

```bash
docker run --rm -v devopsquiz-scores:/app/data --entrypoint cat devopsquiz:latest /app/data/scores.csv
```

CAPTURE 7.3 : le classement affiché après recréation du conteneur.

### Différence entre une image et un conteneur

Une **image** est un modèle figé et en lecture seule : elle contient le système de fichiers de base, le JDK et l'application compilée. On la construit une fois avec `docker build` et on peut la partager. Un **conteneur** est une instance en cours d'exécution (ou arrêtée) de cette image : Docker ajoute une fine couche modifiable par-dessus. Avec une seule image on peut lancer autant de conteneurs qu'on veut. Quand on supprime un conteneur, tout ce qu'il a écrit dans sa couche modifiable disparaît, d'où l'intérêt du volume pour les scores. Comparaison avec la programmation : l'image est la classe, le conteneur est l'objet.

## Étape 8 : MONITOR (supervision)

### Les logs de l'application

Le quiz écrit un log au démarrage, à la fermeture et à chaque erreur de saisie (format `[date] [NIVEAU] message`). Les logs `ERROR` partent sur la sortie d'erreur et sont visibles avec Docker.

```bash
docker logs quiz            # tous les logs du conteneur
docker logs -f quiz         # suivre en direct
docker logs --tail 20 quiz  # les 20 dernières lignes
docker logs quiz 2>&1 | grep ERROR
```

CAPTURE 8.1 : extrait de `docker logs` montrant un démarrage et une fermeture.

### Provoquer et identifier une erreur

Dans le menu, saisir du texte au lieu d'un chiffre (par exemple `abc`), puis un nombre hors limites (par exemple `9`). Version non interactive, rejouable dans le pipeline :

```bash
printf 'abc\n9\n4\n' | docker run --rm -i devopsquiz:latest
```

CAPTURE 8.2 : les lignes `[ERROR]` obtenues dans `docker logs`.

### Analyse de l'incident (à compléter avec les logs réels)

| Élément | Contenu |
|---|---|
| Symptôme observé | Message « Entrez un chiffre... » affiché au joueur. |
| Ce que disent les logs | Coller ici les lignes `[ERROR]` exactes et leur date. |
| Cause | Saisie non numérique ou hors de la plage 1 à 4 dans le menu. |
| Impact | Aucun plantage : l'application redemande la saisie. À confirmer avec la capture. |
| Correction possible | Déjà gérée par le menu. Pistes d'amélioration : limiter le nombre d'essais, afficher les choix valides après chaque erreur. |

Deuxième incident utile à documenter : un fichier de scores corrompu. Ajouter volontairement une ligne invalide dans `scores.csv`, relancer le quiz et lire dans `docker logs` la ligne `Ligne de score ignorée : ...`. Le classement s'affiche quand même, seule la ligne illisible est écartée.

### Vérification automatique dans le pipeline

Le script `scripts/verify-docker.sh` rejoue ces contrôles sans clavier (démarrage, saisie invalide, persistance du volume). Il renvoie un code différent de 0 en cas d'échec, ce qui fait échouer le stage Jenkins qui l'appelle.
