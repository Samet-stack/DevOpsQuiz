#!/usr/bin/env bash
# Compile le code et lance les tests JUnit 5 sans Maven (ticket 8).
# Utilisable à la main et depuis le Jenkinsfile : code de sortie != 0 si un test échoue.
#
# Prérequis : javac/java et le JAR junit-platform-console-standalone (voir INSTALL.md).
# Variable optionnelle : JUNIT_JAR = chemin du JAR.

set -euo pipefail

cd "$(dirname "$0")"

JUNIT_JAR="${JUNIT_JAR:-$HOME/.local/lib/junit-platform-console-standalone.jar}"

if [ ! -f "$JUNIT_JAR" ]; then
  echo "ERREUR : JAR JUnit introuvable : $JUNIT_JAR" >&2
  echo "Voir INSTALL.md (étape 2) ou définir JUNIT_JAR." >&2
  exit 2
fi

rm -rf build/classes build/test-classes
mkdir -p build/classes build/test-classes

echo "==> Compilation du code (javac)"
find src/main/java -name '*.java' > build/sources-main.txt
javac -encoding UTF-8 -d build/classes @build/sources-main.txt

echo "==> Compilation des tests (javac + JUnit dans le classpath)"
if find src/test/java -name '*.java' 2>/dev/null | grep -q .; then
  find src/test/java -name '*.java' > build/sources-test.txt
  javac -encoding UTF-8 -cp "$JUNIT_JAR:build/classes" -d build/test-classes @build/sources-test.txt
else
  echo "ERREUR : aucun test trouvé dans src/test/java" >&2
  exit 2
fi

echo "==> Exécution des tests (JUnit Platform Console)"
java -jar "$JUNIT_JAR" execute \
  --class-path "build/classes:build/test-classes" \
  --scan-class-path build/test-classes \
  --fail-if-no-tests \
  --disable-banner
