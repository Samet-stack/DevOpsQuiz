pipeline {
    agent any

    environment {
        PATH = "${env.HOME}/.local/jdk-21/bin:${env.PATH}"
        JUNIT_JAR = "${env.HOME}/.local/lib/junit-platform-console-standalone.jar"
        IMAGE_NAME = "devopsquiz"
        CONTAINER_NAME = "devopsquiz-app"
        VOLUME_NAME = "devopsquiz-scores"
    }

    stages {
        stage('Informations Git') {
            steps {
                sh 'git log -1 --oneline'
            }
        }

        stage('Build (javac)') {
            steps {
                sh 'mkdir -p build/classes'
                sh 'javac -encoding UTF-8 -d build/classes $(find src/main/java -name "*.java")'
            }
        }

        stage('Test (JUnit)') {
            steps {
                sh 'chmod +x run-tests.sh'
                sh './run-tests.sh'
            }
        }

        stage('Deploy (Docker)') {
            steps {
                sh '''
                    docker build -t ${IMAGE_NAME}:latest .
                    chmod +x scripts/verify-docker.sh
                    ./scripts/verify-docker.sh ${IMAGE_NAME}:latest
                    docker volume create ${VOLUME_NAME} >/dev/null
                    docker rm -f ${CONTAINER_NAME} >/dev/null 2>&1 || true
                    docker run -dit --name ${CONTAINER_NAME} -v ${VOLUME_NAME}:/app/data ${IMAGE_NAME}:latest
                    docker ps --filter "name=${CONTAINER_NAME}"
                '''
            }
        }
    }

    post {
        success {
            echo 'VALIDATION REUSSIE : build, tests JUnit et deploiement Docker OK'
        }
        failure {
            echo 'VALIDATION ECHOUEE : deploiement bloque, consulter Console Output'
        }
    }
}
