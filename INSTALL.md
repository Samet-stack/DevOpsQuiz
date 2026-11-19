# Installation

Voici les commandes qu'on a utilisées sur Ubuntu WSL2. Selon ton PC ou ton système il faudra peut-être adapter certains chemins.

**Java 21**

```bash
mkdir -p ~/.local/jdk-21 ~/.local/lib ~/.local/jenkins
curl -fsSL "https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse" -o /tmp/temurin21.tar.gz
tar -xzf /tmp/temurin21.tar.gz -C ~/.local/jdk-21 --strip-components=1
rm -f /tmp/temurin21.tar.gz
```

Ajoute ces lignes à la fin de `~/.bashrc`

```bash
export JAVA_HOME=$HOME/.local/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
export JUNIT_JAR=$HOME/.local/lib/junit-platform-console-standalone.jar
```

Puis recharge et teste

```bash
source ~/.bashrc
java -version
javac -version
```

**JUnit 5**

```bash
curl -fsSL "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar" -o ~/.local/lib/junit-platform-console-standalone.jar
java -jar "$JUNIT_JAR" --version
```

**Jenkins**

```bash
curl -fsSL "https://get.jenkins.io/war-stable/latest/jenkins.war" -o ~/.local/jenkins/jenkins.war
java -jar ~/.local/jenkins/jenkins.war --httpPort=8080
```

Ouvre `http://localhost:8080` dans ton navigateur. Le mot de passe initial est dans `~/.jenkins/secrets/initialAdminPassword`.

**Docker**

```bash
sudo apt update && sudo apt install -y docker.io
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
newgrp docker
docker run --rm hello-world
```
