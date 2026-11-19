DevOpsQuiz

Projet de L3 MIAGE APP pour faire un quiz informatique en Java dans le terminal et mettre en place la chaîne CI/CD avec Git, javac, JUnit, Jenkins et Docker.

Avant de commencer, regarde INSTALL.md pour installer les outils sur ton PC. Les commandes peuvent changer un peu selon ton système.

## Tests, scores et vérification Docker

### Lancer les tests (sans Maven)

```bash
./run-tests.sh
```

Le script compile le code avec `javac` dans `build/classes`, compile les tests avec le JAR JUnit (variable `JUNIT_JAR`, voir INSTALL.md), puis lance JUnit Platform Console. Il renvoie un code différent de 0 si un test échoue : c'est ce qui bloque le déploiement dans Jenkins.

### Fichier des scores

Chaque partie est ajoutée à `data/scores.csv` au format `pseudo;score;total;date`. Ce dossier n'est pas versionné (`.gitignore`). Les tests utilisent un dossier temporaire (propriété `devopsquiz.data`) pour ne pas toucher à tes vrais scores. Dans Docker, `/app/data` est un volume : les scores survivent à la suppression du conteneur.

### Vérifier l'image Docker sans clavier

```bash
docker build -t devopsquiz:latest .
./scripts/verify-docker.sh devopsquiz:latest
```

Le script vérifie que l'application démarre et se ferme proprement, qu'une saisie invalide produit un log `ERROR` sans planter, et que les scores sont conservés dans un volume. Il est appelé par le stage Deploy du Jenkinsfile.

### Exploitation et supervision

Les commandes `docker ps`, `docker logs`, l'arrêt et la recréation du conteneur, ainsi que l'analyse d'un incident, sont décrites dans `docs/OPERATE-MONITOR.md`.
