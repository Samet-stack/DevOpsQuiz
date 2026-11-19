# DevOpsQuiz

Application console Java de quiz informatique (Linux, Git, Jenkins, Docker, DevOps) avec pipeline CI/CD complet sans Maven.

## Prérequis

- Java JDK 21 (`java`, `javac`)
- JUnit Platform Console Standalone (`junit-platform-console-standalone.jar`)
- Git
- Jenkins LTS
- Docker

Pour installer les outils sur Ubuntu / WSL2, voir [INSTALL.md](INSTALL.md).

## Compilation et exécution (sans Maven)

```bash
mkdir -p build/classes
javac -encoding UTF-8 -d build/classes $(find src/main/java -name "*.java")
java -cp build/classes DevOpsQuiz
```

## Tests JUnit (sans Maven)

```bash
chmod +x run-tests.sh
./run-tests.sh
```

## Pipeline Jenkins

Le fichier `Jenkinsfile` automatise les 4 étapes suivantes sur chaque push (via Poll SCM `H/5 * * * *`) :
1. Informations Git (`git log -1 --oneline`)
2. Build (`javac -encoding UTF-8 -d build/classes ...`)
3. Test (`./run-tests.sh` avec JUnit Platform Console)
4. Deploy (`docker build`, vérification non interactive `./scripts/verify-docker.sh`, déploiement du conteneur `devopsquiz-app` avec le volume `devopsquiz-scores`)

## Docker

Construire l'image et vérifier automatiquement le conteneur :

```bash
docker build -t devopsquiz:latest .
./scripts/verify-docker.sh devopsquiz:latest
```

Lancer une partie interactive en conservant les scores dans un volume :

```bash
docker volume create devopsquiz-scores
docker run -it --rm -v devopsquiz-scores:/app/data devopsquiz:latest
```

Exploitation et logs (voir aussi [docs/OPERATE-MONITOR.md](docs/OPERATE-MONITOR.md)) :

```bash
docker ps -a
docker logs devopsquiz-app
```
