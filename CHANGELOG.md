# Changelog

## [1.1.0] - 2026-11-19
- Affichage du numéro de version dans le menu principal
- Affichage du nombre de questions disponibles par catégorie
- Mesure et affichage du temps total en secondes à la fin de chaque partie
- Restauration du README complet avec les commandes sans Maven, Jenkins et Docker

## [1.0.0] - 2026-11-19
- Application console DevOpsQuiz (menu 1 à 4, règles, gestion des erreurs de saisie, logs)
- Banque de questions Linux, Git, Jenkins, Docker et DevOps (BanqueQuestions, Question)
- Sauvegarde et classement des scores dans un fichier persistant (ScoreManager)
- 24 tests unitaires JUnit 5 sans Maven (run-tests.sh)
- Pipeline Jenkins (Jenkinsfile) et conteneurisation Docker avec volume (Dockerfile, verify-docker.sh)
