# CI/CD con Jenkins

Este proyecto implementa la propuesta del ADR-023 mediante el `Jenkinsfile` ubicado en la raiz del repositorio.

## Comportamiento de la pipeline

- Todas las ramas ejecutan pruebas de `auth-service`, `inventory-service`, `api-gateway`, `audit-service` y `alert-service`.
- Las ramas de trabajo y Pull Requests solo validan pruebas; no despliegan ambientes.
- Solo las ramas exactas `Develop`, `QA` y `main` despliegan con Docker Compose.
- `Develop` usa `.env.dev`, `QA` usa `.env.qa` y `main` usa `.env.main`.

## Comandos de despliegue

Jenkins ejecuta uno de estos comandos solamente cuando la rama coincide con un ambiente:

```bash
docker compose --env-file .env.dev up -d --build
docker compose --env-file .env.qa up -d --build
docker compose --env-file .env.main up -d --build
```

## Configuracion recomendada en Jenkins

1. Crear un proyecto de tipo Multibranch Pipeline.
2. Conectar el repositorio de GitHub o GitLab.
3. Configurar el webhook hacia Jenkins para pushes y Pull Requests o Merge Requests.
4. Usar el `Jenkinsfile` de la rama como definicion de pipeline.
5. Asegurar que el agente tenga Java, Maven Wrapper, Node.js, npm, Docker y Docker Compose.

## Proteccion de ramas

Para que Jenkins funcione como compuerta tecnica, protege las ramas `Develop`, `QA` y `main` en GitHub o GitLab.

La regla recomendada es exigir que el check de Jenkins pase antes de permitir el merge:

```text
Jenkins / FarmaExpres Backend CI
```

Con esta configuracion, una rama de trabajo puede avanzar hacia `Develop`, `QA` o `main` solamente si todas las pruebas terminan correctamente.
