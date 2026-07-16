# Guía de contribución

Este repositorio trabaja con **GitHub Flow**. Todo cambio debe salir de un Issue, desarrollarse en una rama corta, revisarse por Pull Request y fusionarse a `main` solo cuando esté aprobado.

## Flujo rápido

1. Tomar un Issue del sprint.
2. Crear una rama desde `main`.
3. Hacer commits pequeños con formato convencional.
4. Abrir un Pull Request vinculado al Issue.
5. Pedir review cuando el PR esté listo.
6. Fusionar con **Squash and merge** después de la aprobación.
7. Eliminar la rama después del merge.

## Reglas del equipo

| Tema | Regla |
| --- | --- |
| Rama principal | No se permite `push` directo a `main`. |
| Entrada de cambios | Todo cambio entra por Pull Request. |
| Issues | Todo PR debe estar vinculado a un Issue con `Closes #numero`. |
| Review | Cada PR requiere al menos 1 aprobación antes del merge. |
| Merge | Usar **Squash and merge** como estrategia estándar. |
| Limpieza | Eliminar la rama de trabajo después de fusionar. |

## Nombres de ramas

Usar este formato:

```txt
tipo/numero-issue-descripcion-corta
```

Tipos permitidos:

| Tipo | Uso |
| --- | --- |
| `feature` | Nueva funcionalidad |
| `bugfix` | Corrección de errores |
| `docs` | Documentación |
| `refactor` | Mejora interna sin cambiar comportamiento |
| `test` | Agregado o corrección de pruebas |
| `chore` | Tareas técnicas menores |

Ejemplos:

```txt
feature/24-login-google
bugfix/31-error-carrito
docs/12-readme-api
```

## Commits convencionales

Usar mensajes claros que indiquen qué cambió:

```txt
feat: agrega formulario de login
fix: corrige validación de email
docs: actualiza guía de instalación
refactor: simplifica cálculo de balance
test: agrega pruebas para registro de usuario
chore: actualiza configuración del proyecto
```

Evitar mensajes genéricos como `cambios`, `avance`, `update` o `fix` sin contexto.

## Requisitos de Pull Request

- Debe explicar qué se hizo.
- Debe indicar cómo se probó.
- Debe vincular el Issue con `Closes #numero`.
- Debe incluir capturas si afecta la interfaz.
- Debe ser pequeño y enfocado; si requiere más de 20 a 30 minutos de review, dividirlo.
- No debe mezclar funcionalidades, refactors y correcciones no relacionadas.

## Expectativas de Code Review

- Revisar si el PR resuelve el Issue indicado.
- Verificar que el código sea claro y no rompa comportamiento existente.
- Confirmar que se hayan probado los casos principales.
- Marcar archivos temporales, logs, código comentado o cambios no relacionados.
- Hacer comentarios concretos, respetuosos y enfocados en el código.

## GitHub Projects

| Estado | Cuándo se usa |
| --- | --- |
| Por hacer | Issue priorizado para el sprint |
| En progreso | Alguien tomó el Issue y creó una rama |
| En revisión | Hay un PR abierto y vinculado al Issue |
| Terminado | El PR fue aprobado y fusionado a `main` |

Si las automatizaciones no están configuradas, el responsable del Issue debe moverlo manualmente.

## Checklist antes de pedir review

- [ ] El Issue está claro y vinculado en el PR.
- [ ] La rama usa el nombre correcto.
- [ ] La rama está actualizada con `main`.
- [ ] El proyecto compila o corre localmente.
- [ ] Se probaron los flujos afectados.
- [ ] La descripción del PR está completa.
- [ ] No hay cambios no relacionados.
- [ ] No hay secretos, archivos temporales, logs innecesarios ni código comentado.
