pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        CI_NAME = 'Jenkins / FarmaExpres Backend CI'
    }

    stages {
        stage('Detectar ambiente') {
            steps {
                script {
                    def branchName = getBranchName()
                    def branchKey = getBranchKey()
                    def envFile = ''
                    def shouldDeploy = false

                    if (branchKey.contains('develop')) {
                        envFile = '.env.dev'
                        shouldDeploy = true
                    } else if (branchKey.contains('qa')) {
                        envFile = '.env.qa'
                        shouldDeploy = true
                    } else if (branchKey.contains('main')) {
                        envFile = '.env.main'
                        shouldDeploy = true
                    } else {
                        echo "Rama de trabajo o Pull Request: ${branchName}. Solo se ejecutan pruebas."
                    }

                    echo "Pipeline: ${env.CI_NAME}"
                    echo "Rama: ${branchName}"
                    echo "Rama normalizada: ${branchKey}"
                    echo "Despliegue habilitado: ${shouldDeploy}"
                    if (shouldDeploy) {
                        echo "Archivo de ambiente: ${envFile}"
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
                    def envFile = ''

                    if (branchKey.contains('develop')) {
                        envFile = '.env.dev'
                    } else if (branchKey.contains('qa')) {
                        envFile = '.env.qa'
                    } else if (branchKey.contains('main')) {
                        envFile = '.env.main'
                    } else {
                        echo "Despliegue omitido para rama ${getBranchName()}"
                        return
                    }

                    echo "Desplegando ambiente con ${envFile}"
                    runCommand(
                        "if docker compose version >/dev/null 2>&1; then docker compose --env-file ${envFile} up -d --build; else docker-compose --env-file ${envFile} up -d --build; fi",
                        "docker compose --env-file ${envFile} up -d --build"
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
