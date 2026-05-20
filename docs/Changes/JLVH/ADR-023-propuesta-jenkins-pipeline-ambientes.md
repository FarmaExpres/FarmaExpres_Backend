# ADR-023: Propuesta de Jenkins Pipeline por Ambientes

**Fecha:** 2026-05-18  
**Estado:** Propuesto  
**Proyecto:** FarmaExpres Backend  
**Componente:** CI/CD, Jenkins, GitHub/GitLab, Docker Compose  

## 1. Contexto

El proyecto `FarmaExpres Backend` esta compuesto por varios microservicios:

- `api-gateway`
- `auth-service`
- `inventory-service`
- `audit-service`
- `alert-service`

El proyecto ya cuenta con una estrategia de ambientes basada en archivos `.env`:

- `.env.dev`
- `.env.qa`
- `.env.main`

Tambien existe un `docker-compose.yml` que permite levantar los servicios usando el archivo de ambiente correspondiente.

Actualmente se requiere definir como podria integrarse Jenkins al flujo de trabajo para:

- ejecutar pruebas automaticamente
- validar Pull Requests
- evitar merges con errores
- respetar los ambientes `Develop`, `QA` y `main`
- desplegar solamente cuando el cambio llegue formalmente a una rama de ambiente

## 2. Problema

Sin una pipeline automatizada, cada integrante puede validar el proyecto de forma diferente antes de hacer merge. Esto genera riesgos como:

- subir cambios que rompen un microservicio
- integrar codigo sin ejecutar pruebas
- desplegar manualmente en el ambiente incorrecto
- mezclar cambios de `Develop`, `QA` y `main` sin control
- depender de validaciones manuales tardias

El objetivo no es que Jenkins mueva cambios automaticamente entre ramas, sino que actue como una compuerta tecnica.

## 3. Decision Propuesta

Implementar una pipeline de Jenkins que ejecute pruebas en todas las ramas y despliegue solamente cuando la rama sea una rama de ambiente.

Las ramas de ambiente serian:

```text
Develop
QA
main
```

Las ramas de trabajo o historias de usuario podrian usar nombres como:

```text
HU-001-dev
HU-002-dev
feature/login
bugfix/auth-token
```

Estas ramas de trabajo sirven para desarrollar y validar cambios, pero no deben desplegar ambientes por si solas.

## 4. Flujo General

El flujo recomendado seria:

```text
HU-001-dev
   |
   | Pull Request aprobado
   v
Develop
   |
   | Pull Request aprobado
   v
QA
   |
   | Pull Request aprobado
   v
main
```

Jenkins no debe hacer merge automatico entre ambientes.

La promocion entre ambientes debe realizarse mediante Pull Request o Merge Request aprobado por el equipo.

## 5. Comportamiento Por Tipo De Rama

### 5.1 Ramas de trabajo

Ejemplos:

```text
HU-001-dev
feature/inventory-entry
bugfix/gateway-auth
```

Comportamiento esperado:

```text
Push a rama de trabajo
   -> Jenkins ejecuta pruebas
   -> Jenkins reporta resultado al Pull Request
   -> No despliega ambiente
```

Si una prueba falla:

```text
Jenkins marca FAILED
El Pull Request no deberia poder mezclarse
No se realiza despliegue
```

### 5.2 Rama Develop

Comportamiento esperado:

```text
Merge hacia Develop
   -> Jenkins ejecuta pruebas
   -> Si pasan, despliega usando .env.dev
   -> Si fallan, no despliega
```

Comando conceptual:

```bash
docker compose --env-file .env.dev up -d --build
```

### 5.3 Rama QA

Comportamiento esperado:

```text
Merge hacia QA
   -> Jenkins ejecuta pruebas
   -> Si pasan, despliega usando .env.qa
   -> Si fallan, no despliega
```

Comando conceptual:

```bash
docker compose --env-file .env.qa up -d --build
```

### 5.4 Rama main

Comportamiento esperado:

```text
Merge hacia main
   -> Jenkins ejecuta pruebas
   -> Si pasan, despliega usando .env.main
   -> Si fallan, no despliega
```

Comando conceptual:

```bash
docker compose --env-file .env.main up -d --build
```

## 6. Regla Principal De Ambientes

El ambiente no debe definirse por el nombre de una subrama.

Ejemplo:

```text
HU-001-dev
```

El sufijo `-dev` ayuda al equipo a entender que la historia apunta primero al ambiente de desarrollo, pero Jenkins no deberia usar ese sufijo para desplegar.

La regla recomendada es:

```text
Solo Develop despliega dev
Solo QA despliega qa
Solo main despliega main
```

Esto evita errores como desplegar por accidente una rama mal nombrada.

## 7. Ejecucion De Pruebas

Jenkins debe ejecutar las pruebas de todos los microservicios antes de permitir un despliegue.

Servicios Java/Maven:

```bash
cd auth-service
./mvnw test

cd ../inventory-service
./mvnw test

cd ../api-gateway
./mvnw test

cd ../audit-service
./mvnw test
```

Servicio Node:

```bash
cd alert-service
npm ci
npm test
```

En un agente Jenkins sobre Windows, los comandos Maven se ejecutarian con:

```bat
mvnw.cmd test
```

Si uno solo de los microservicios falla en pruebas, Jenkins debe detener la pipeline.

Resultado esperado:

```text
auth-service       OK
inventory-service  OK
api-gateway        OK
audit-service      OK
alert-service      FAILED

Resultado final: FAILED
Despliegue: No realizado
```

## 8. Integracion Con GitHub O GitLab

Para que Jenkins marque checks en Pull Requests, debe integrarse con GitHub o GitLab.

### 8.1 GitHub

Componentes usuales:

- GitHub Plugin
- GitHub Branch Source Plugin
- Multibranch Pipeline
- Webhook desde GitHub hacia Jenkins
- Token de GitHub configurado como credencial en Jenkins

Flujo:

```text
Se crea o actualiza un Pull Request
   -> GitHub envia webhook a Jenkins
   -> Jenkins ejecuta la pipeline
   -> Jenkins reporta SUCCESS o FAILED al commit
   -> GitHub muestra el check en el Pull Request
```

Ejemplo de check exitoso:

```text
Checks

OK Jenkins / FarmaExpres Backend CI
```

Ejemplo de check fallido:

```text
Checks

FAILED Jenkins / FarmaExpres Backend CI
```

### 8.2 GitLab

Componentes usuales:

- GitLab Plugin
- Webhook desde GitLab hacia Jenkins
- Token de GitLab configurado como credencial
- Commit status reportado desde Jenkins hacia GitLab

Flujo:

```text
Se crea o actualiza un Merge Request
   -> GitLab envia webhook a Jenkins
   -> Jenkins ejecuta la pipeline
   -> Jenkins reporta SUCCESS o FAILED
   -> GitLab muestra el estado del pipeline en el Merge Request
```

## 9. Comentarios Automaticos En Pull Requests

Los comentarios automaticos son opcionales. Jenkins puede configurarse para comentar el resultado en el Pull Request o Merge Request.

### 9.1 Comentario cuando todo pasa

Ejemplo:

```text
Jenkins CI - FarmaExpres Backend

Resultado: SUCCESS

Rama origen: HU-001-dev
Rama destino: Develop

Validaciones ejecutadas:
- auth-service: OK
- inventory-service: OK
- api-gateway: OK
- audit-service: OK
- alert-service: OK

Despliegue:
- No aplica para ramas de trabajo.
- El despliegue ocurrira cuando el cambio sea mergeado a Develop.
```

### 9.2 Comentario cuando falla

Ejemplo:

```text
Jenkins CI - FarmaExpres Backend

Resultado: FAILED

Rama origen: HU-001-dev
Rama destino: Develop

Validaciones ejecutadas:
- auth-service: OK
- inventory-service: OK
- api-gateway: OK
- audit-service: OK
- alert-service: FAILED

Error:
- Fallo npm test en alert-service.

Despliegue:
- No realizado.

Revisar logs:
https://jenkins.ejemplo.com/job/farmaexpres-backend/123/
```

## 10. Proteccion De Ramas

Para que Jenkins realmente bloquee merges con errores, no basta con ejecutar la pipeline. Tambien se deben proteger las ramas en GitHub o GitLab.

Ramas que deberian protegerse:

```text
Develop
QA
main
```

Regla recomendada:

```text
Require status checks to pass before merging
```

Check requerido:

```text
Jenkins / FarmaExpres Backend CI
```

Con esta regla:

```text
Si Jenkins esta en SUCCESS
   -> El Pull Request puede mezclarse

Si Jenkins esta en FAILED
   -> El Pull Request queda bloqueado
```

## 11. Jenkinsfile Conceptual

Este ejemplo es conceptual y debe ajustarse al servidor Jenkins real.

```groovy
pipeline {
    agent any

    environment {
        ENV_FILE = ''
        SHOULD_DEPLOY = 'false'
    }

    stages {
        stage('Detectar ambiente') {
            steps {
                script {
                    if (env.BRANCH_NAME == 'Develop') {
                        ENV_FILE = '.env.dev'
                        SHOULD_DEPLOY = 'true'
                    } else if (env.BRANCH_NAME == 'QA') {
                        ENV_FILE = '.env.qa'
                        SHOULD_DEPLOY = 'true'
                    } else if (env.BRANCH_NAME == 'main') {
                        ENV_FILE = '.env.main'
                        SHOULD_DEPLOY = 'true'
                    } else {
                        echo "Rama de trabajo: ${env.BRANCH_NAME}. Solo se ejecutan pruebas."
                    }
                }
            }
        }

        stage('Pruebas Java') {
            steps {
                dir('auth-service') {
                    bat 'mvnw.cmd test'
                }
                dir('inventory-service') {
                    bat 'mvnw.cmd test'
                }
                dir('api-gateway') {
                    bat 'mvnw.cmd test'
                }
                dir('audit-service') {
                    bat 'mvnw.cmd test'
                }
            }
        }

        stage('Pruebas Node') {
            steps {
                dir('alert-service') {
                    bat 'npm ci'
                    bat 'npm test'
                }
            }
        }

        stage('Desplegar ambiente') {
            when {
                expression {
                    return SHOULD_DEPLOY == 'true'
                }
            }
            steps {
                bat "docker compose --env-file ${ENV_FILE} up -d --build"
            }
        }
    }
}
```

Si Jenkins corre sobre Linux, se debe reemplazar `bat` por `sh`.

## 12. Consideraciones Sobre Secretos

Actualmente los archivos `.env.dev`, `.env.qa` y `.env.main` contienen valores sensibles como passwords y `JWT_SECRET`.

Para un entorno real, se recomienda:

- no guardar secretos reales en Git
- usar Jenkins Credentials
- usar variables de ambiente inyectadas por Jenkins
- mantener en el repositorio archivos de ejemplo como `.env.dev.example`

En contexto academico puede mantenerse simple, pero la buena practica es separar configuracion sensible del codigo fuente.

## 13. Ventajas Esperadas

- Validacion automatica de todos los microservicios
- Menor riesgo de romper `Develop`, `QA` o `main`
- Trazabilidad de cada Pull Request
- Checks visibles para el equipo
- Bloqueo de merges si las pruebas fallan
- Despliegues consistentes con `.env.dev`, `.env.qa` y `.env.main`
- Separacion clara entre ramas de trabajo y ramas de ambiente

## 14. Riesgos Y Mitigaciones

| Riesgo | Impacto | Mitigacion |
|--------|---------|------------|
| Tests lentos | La pipeline tarda demasiado | Mantener pruebas smoke rapidas y ampliar gradualmente |
| Jenkins mal configurado | No reporta checks al PR | Validar webhook, credenciales y permisos |
| Secretos expuestos | Riesgo de seguridad | Migrar secretos a Jenkins Credentials |
| Despliegue accidental | Ambiente incorrecto | Desplegar solo si la rama exacta es `Develop`, `QA` o `main` |
| Falta de proteccion de ramas | Se puede mergear con Jenkins fallando | Activar branch protection rules |

## 15. Criterios De Aceptacion Propuestos

La integracion de Jenkins se consideraria lista cuando:

1. Jenkins detecte Pull Requests o Merge Requests automaticamente.
2. Jenkins ejecute pruebas de todos los microservicios.
3. Jenkins reporte checks `SUCCESS` o `FAILED` en GitHub/GitLab.
4. Las ramas `Develop`, `QA` y `main` tengan proteccion de rama.
5. Un Pull Request no pueda mezclarse si Jenkins falla.
6. Las ramas de trabajo ejecuten pruebas pero no desplieguen.
7. La rama `Develop` despliegue usando `.env.dev`.
8. La rama `QA` despliegue usando `.env.qa`.
9. La rama `main` despliegue usando `.env.main`.

## 16. Resumen

Jenkins debe funcionar como una compuerta de calidad para FarmaExpres Backend.

La idea principal es:

```text
Todas las ramas ejecutan pruebas.
Solo Develop, QA y main despliegan.
Los cambios avanzan entre ambientes mediante Pull Request aprobado.
GitHub/GitLab bloquea el merge si Jenkins falla.
```

Con este enfoque se respetan los ambientes y se reduce el riesgo de integrar o desplegar cambios defectuosos.
