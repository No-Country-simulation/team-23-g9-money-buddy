# Money Buddy

---

## Backend API

API REST base de Money Buddy construida con Java 21, Spring Boot 3 y Maven.

### Requisitos locales

- JDK 21
- Maven 3.9+
- Docker, opcional para ejecutar en contenedor

### Ejecutar localmente

```bash
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### Endpoints iniciales

- `GET /api`: estado base de la API.
- `GET /actuator/health`: health check de Spring Actuator.

### Verificar

```bash
cd backend
mvn test
```

### Docker

```bash
cd backend
docker build -t money-buddy-api .
docker run --rm -p 8080:8080 money-buddy-api
```

---

## Colaboración

Antes de trabajar en el repositorio, revisá la [guía de contribución](CONTRIBUTING.md).

Ahí está definido el flujo del equipo: ramas por tarea, commits convencionales, Pull Requests, Code Review y vinculación con Issues del sprint.
