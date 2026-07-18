# Money Buddy

---

## Backend API

API REST base de Money Buddy construida con Java 21, Spring Boot 3 y Maven.

Esta guía explica cómo levantar el backend localmente después de clonar o actualizar el repositorio.

### Requisitos locales

- Git
- JDK 21
- Maven 3.9+, si se ejecuta el backend sin Docker
- Docker Desktop, si se ejecuta el backend con Docker en una computadora local

Docker Desktop debe estar instalado y abierto antes de usar `docker compose` o `docker build`.

### Qué hace Docker en este proyecto

Docker crea una imagen del backend con Java y la aplicación ya empaquetada. Docker Compose usa esa imagen para iniciar el servicio en el puerto `8080`, sin depender de que cada integrante configure Maven para ejecutar la aplicación manualmente.

### Flujo recomendado

1. Clonar el repositorio o traer los últimos cambios.
2. Entrar a la carpeta del proyecto.
3. Levantar el backend con Docker Compose o con Maven local.
4. Verificar los endpoints base.

```bash
git clone <url-del-repositorio>
cd hackathon-one-g9-latam
```

Si ya tienes el repositorio local:

```bash
git pull
```

### Ejecutar con Docker Compose

```bash
docker compose up --build
```

La API queda disponible en `http://localhost:8080`.

Para detener el servicio:

```bash
docker compose down
```

### Ejecutar con Maven local

```bash
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### Endpoints iniciales

- `GET /api`: estado base de la API.
- `GET /actuator/health`: health check de Spring Actuator.

### Verificar endpoints

Con la aplicación levantada, ejecutar en otra terminal:

```bash
curl http://localhost:8080/api
curl http://localhost:8080/actuator/health
```

El health check debe responder con estado `UP`.

### Ejecutar pruebas

```bash
cd backend
mvn test
```

---

## Colaboración

Antes de trabajar en el repositorio, revisa la [guía de contribución](CONTRIBUTING.md).

Ahí está definido el flujo del equipo: ramas por tarea, conventional commits, Pull Requests, Code Review y vinculación con Issues del sprint.
