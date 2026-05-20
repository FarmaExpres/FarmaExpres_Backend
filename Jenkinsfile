pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        ENV_FILE = ''
        SHOULD_DEPLOY = 'false'
        CI_NAME = 'Jenkins / FarmaExpres Backend CI'
    }

    stages {
        stage('Detectar ambiente') {
            steps {
                script {
                    env.SHOULD_DEPLOY = 'false'
                    env.ENV_FILE = ''

                    def branchName = (env.BRANCH_NAME ?: env.GIT_BRANCH ?: '').replaceFirst('^origin/', '').trim()
                    def branchKey = branchName.toLowerCase()

                    if (branchKey == 'develop') {
                        env.ENV_FILE = '.env.dev'
                        env.SHOULD_DEPLOY = 'true'
                    } else if (branchKey == 'qa') {
                        env.ENV_FILE = '.env.qa'
                        env.SHOULD_DEPLOY = 'true'
                    } else if (branchKey == 'main') {
                        env.ENV_FILE = '.env.main'
                        env.SHOULD_DEPLOY = 'true'
                    } else {
                        echo "Rama de trabajo o Pull Request: ${branchName}. Solo se ejecutan pruebas."
                    }

                    echo "Pipeline: ${env.CI_NAME}"
                    echo "Rama: ${branchName}"
                    echo "Despliegue habilitado: ${env.SHOULD_DEPLOY}"
                    if (env.SHOULD_DEPLOY == 'true') {
                        echo "Archivo de ambiente: ${env.ENV_FILE}"
                    }
                }
            }
        }

        stage('Pruebas Java') {
            steps {
                script {
                    runInDir('auth-service', 'sh mvnw test', 'mvnw.cmd test')
                    runInDir('inventory-service', 'sh mvnw test', 'mvnw.cmd test')
                    runInDir('api-gateway', 'sh mvnw test', 'mvnw.cmd test')
                    runInDir('audit-service', 'sh mvnw test', 'mvnw.cmd test')
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Pruebas Node') {
            steps {
                script {
                    runInDir('alert-service', 'npm ci', 'npm ci')
                    runInDir('alert-service', 'npm test', 'npm test')
                }
            }
        }

        stage('Desplegar ambiente') {
            when {
                expression {
                    return env.SHOULD_DEPLOY == 'true'
                }
            }
            steps {
                script {
                    runCommand(
                        "docker compose --env-file ${env.ENV_FILE} up -d --build",
                        "docker compose --env-file ${env.ENV_FILE} up -d --build"
                    )
                }
            }
        }
    }

    post {
        success {
            echo "${env.CI_NAME}: SUCCESS"
        }
        failure {
            echo "${env.CI_NAME}: FAILED. No se realiza despliegue si las pruebas fallan."
        }
    }
}

def runInDir(String directory, String unixCommand, String windowsCommand) {
    dir(directory) {
        runCommand(unixCommand, windowsCommand)
    }
}

def runCommand(String unixCommand, String windowsCommand) {
    if (isUnix()) {
        sh unixCommand
    } else {
        bat windowsCommand
    }
}
