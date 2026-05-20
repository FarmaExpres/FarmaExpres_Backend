pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        ENV_FILE = ''
        CI_NAME = 'Jenkins / FarmaExpres Backend CI'
    }

    stages {
        stage('Detectar ambiente') {
            steps {
                script {
                    env.ENV_FILE = ''

                    def branchName = getBranchName()
                    def branchKey = getBranchKey()
                    def shouldDeploy = false

                    if (branchKey.contains('develop')) {
                        env.ENV_FILE = '.env.dev'
                        shouldDeploy = true
                    } else if (branchKey.contains('qa')) {
                        env.ENV_FILE = '.env.qa'
                        shouldDeploy = true
                    } else if (branchKey.contains('main')) {
                        env.ENV_FILE = '.env.main'
                        shouldDeploy = true
                    } else {
                        echo "Rama de trabajo o Pull Request: ${branchName}. Solo se ejecutan pruebas."
                    }

                    echo "Pipeline: ${env.CI_NAME}"
                    echo "Rama: ${branchName}"
                    echo "Rama normalizada: ${branchKey}"
                    echo "Despliegue habilitado: ${shouldDeploy}"
                    if (shouldDeploy) {
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
            steps {
                script {
                    def branchKey = getBranchKey()

                    if (branchKey.contains('develop')) {
                        env.ENV_FILE = '.env.dev'
                    } else if (branchKey.contains('qa')) {
                        env.ENV_FILE = '.env.qa'
                    } else if (branchKey.contains('main')) {
                        env.ENV_FILE = '.env.main'
                    } else {
                        echo "Despliegue omitido para rama ${getBranchName()}"
                        return
                    }

                    echo "Desplegando ambiente con ${env.ENV_FILE}"
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

def getBranchName() {
    return (env.BRANCH_NAME ?: env.GIT_BRANCH ?: '').replaceFirst('^origin/', '').trim()
}

def getBranchKey() {
    return getBranchName().toLowerCase()
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
