# Despliegue en Render y Vercel

Esta guía describe el camino recomendado para desplegar Money Buddy con Backend y `ml-service` en Render, y Frontend en Vercel. No incluye secretos ni URLs productivas reales: esos valores se cargan manualmente en los dashboards de cada plataforma.

## Ruta rápida

1. Desplegar `ml-service` en Render.
2. Desplegar Backend en Render apuntando a la URL pública o privada disponible de `ml-service`.
3. Desplegar Frontend en Vercel apuntando a la URL pública del Backend.
4. Ejecutar el checklist de smoke test al final de esta guía.

## Resumen de servicios

| Servicio | Plataforma | Root/context | Runtime | Health check |
| --- | --- | --- | --- | --- |
| Backend | Render | `backend` | Docker | `/actuator/health` |
| ML service | Render | `ml` | Docker | `/health` |
| Frontend | Vercel | `frontend` | Vite build | Validación por navegación web |

## Render Backend

Crear un Web Service en Render desde el repositorio.

| Campo | Valor |
| --- | --- |
| Root Directory | `backend` |
| Runtime | Docker |
| Dockerfile | `Dockerfile` dentro de `backend` |
| Health Check Path | `/actuator/health` |

Variables de entorno:

| Variable | Valor esperado | Cómo cargarla |
| --- | --- | --- |
| `ML_SERVICE_BASE_URL` | URL base de `ml-service`, sin barra final. Ejemplo de formato: `https://<ml-service-render>.onrender.com` | Manual en dashboard |
| `APP_CORS_ALLOWED_ORIGINS` | Orígenes permitidos separados por coma. Debe incluir la URL pública de Vercel. | Manual en dashboard |

Notas:

- No inventar URLs productivas durante la configuración. Primero crear los servicios y copiar las URLs reales asignadas por cada plataforma.
- `APP_CORS_ALLOWED_ORIGINS` debe contener exactamente el origen del navegador, por ejemplo `https://<frontend>.vercel.app`, sin path final.
- El health check exitoso debe responder `UP` en `/actuator/health`.

## Render ML service

Crear un Web Service en Render desde el repositorio.

| Campo | Valor |
| --- | --- |
| Root Directory | `ml` |
| Runtime | Docker |
| Dockerfile | `Dockerfile` dentro de `ml` |
| Health Check Path | `/health` |

Variables de entorno para modelos:

| Variable | Valor esperado | Cómo cargarla |
| --- | --- | --- |
| `MODEL_PATH` | Ruta del modelo de estabilidad financiera dentro del contenedor. Default recomendado: `./models/modelo_financial_stability.pkl` | Manual si se cambia el default |
| `CLASSIFIER_MODEL_PATH` | Ruta del clasificador de transacciones. Default recomendado: `./models/classify-transactions.pkl` | Manual si se cambia el default |
| `LABEL_ENCODER_CATEGORIA_PATH` | Ruta del label encoder. Default recomendado: `./models/label_encoder_categoria.pkl` | Manual si se cambia el default |
| `SCALER_CATEGORIA_PATH` | Ruta del scaler. Default recomendado: `./models/scaler_categoria.pkl` | Manual si se cambia el default |

Variables de entorno para OCI/Object Storage:

| Variable | Valor esperado | Cómo cargarla |
| --- | --- | --- |
| `OCI_MODEL_DOWNLOAD_ENABLED` | `true` solo si Render debe descargar artefactos desde OCI al iniciar. | Manual en dashboard |
| `OCI_MODEL_FORCE_DOWNLOAD` | `false` para flujo normal; `true` solo para forzar reemplazo de artefactos. | Manual en dashboard |
| `OCI_CONFIG_FILE` | Ruta del archivo de configuración OCI disponible dentro del runtime. | Manual/secreto según configuración del dashboard |
| `OCI_CONFIG_PROFILE` | Perfil OCI a usar, por ejemplo `DEFAULT`. | Manual en dashboard |
| `OCI_NAMESPACE` | Namespace de OCI Object Storage. | Manual en dashboard |
| `OCI_BUCKET_NAME` | Bucket donde están los artefactos. | Manual en dashboard |
| `OCI_FINANCIAL_STABILITY_OBJECT` | Nombre del objeto para `modelo_financial_stability.pkl`. | Manual en dashboard |
| `OCI_TRANSACTION_CLASSIFIER_OBJECT` | Nombre del objeto para `classify-transactions.pkl`. | Manual en dashboard |
| `OCI_LABEL_ENCODER_CATEGORIA_OBJECT` | Nombre del objeto para `label_encoder_categoria.pkl`. | Manual en dashboard |
| `OCI_SCALER_CATEGORIA_OBJECT` | Nombre del objeto para `scaler_categoria.pkl`. | Manual en dashboard |

Valores sensibles o específicos del entorno:

- Credenciales OCI, private keys, fingerprints, OCIDs, namespaces reales, buckets reales y URLs firmadas no deben guardarse en Git.
- Cargar esos valores únicamente como secretos o valores manuales del dashboard.
- Si `OCI_MODEL_DOWNLOAD_ENABLED=true`, verificar que el runtime tenga disponible la configuración OCI y la llave privada referenciada por esa configuración.

Validación esperada:

- `GET /health` responde JSON con el estado de `model_loaded`, `classifier_loaded`, `label_encoder_loaded`, `scaler_loaded` y descarga OCI.
- Si falta un artefacto requerido para inferencia real, el servicio debe reportarlo en health o responder `503` en los endpoints de predicción/clasificación.

## Vercel Frontend

Importar el repositorio en Vercel y configurar el proyecto del frontend.

| Campo | Valor |
| --- | --- |
| Root Directory | `frontend` |
| Framework | Vite |
| Install Command | `npm install` o el default detectado por Vercel |
| Build Command | `npm run build` |
| Output Directory | `dist` |

Variable de entorno:

| Variable | Valor esperado | Cómo cargarla |
| --- | --- | --- |
| `VITE_API_BASE_URL` | URL pública del Backend en Render, sin barra final. Ejemplo de formato: `https://<backend-render>.onrender.com` | Manual en dashboard |

Notas:

- En Vite, las variables disponibles para el navegador deben usar prefijo `VITE_`.
- Si se usa Vercel CLI, desplegar desde la raíz del monorepo indicando el proyecto con `vercel --cwd frontend`.
- Después de conocer la URL pública final de Vercel, actualizar `APP_CORS_ALLOWED_ORIGINS` en Render Backend con ese origen.

## Blueprint opcional de Render

El archivo [`../render.yaml`](../render.yaml) declara los dos Web Services de Render con `rootDir`, Docker runtime, health checks y variables manuales. Es una base segura para crear los servicios sin incluir secretos.

Usar el blueprint solo si el equipo quiere administrar Backend y `ml-service` juntos desde Render. Si prefieren controlar cada servicio desde el dashboard, seguir las secciones anteriores y cargar los mismos campos manualmente.

Después de aplicar el blueprint:

1. Completar los valores `sync: false` desde el dashboard de Render.
2. Verificar que `ml-service` tenga artefactos ML disponibles si la demo requiere inferencia real.
3. Copiar la URL pública real del Backend a `VITE_API_BASE_URL` en Vercel.
4. Copiar la URL pública real del Frontend a `APP_CORS_ALLOWED_ORIGINS` en Render Backend.

## Orden de despliegue

1. `ml-service` en Render.
2. Backend en Render, usando `ML_SERVICE_BASE_URL` con la URL real de `ml-service`.
3. Frontend en Vercel, usando `VITE_API_BASE_URL` con la URL real del Backend.
4. Actualizar CORS del Backend con la URL final del Frontend si cambió durante el despliegue.

## Smoke test posterior

Checklist mínimo después del despliegue:

- [ ] `GET https://<backend-render>/actuator/health` responde estado saludable y `UP`.
- [ ] `GET https://<ml-service-render>/health` responde JSON y muestra el estado esperado de modelos/OCI.
- [ ] La URL pública de Vercel carga el dashboard del frontend.
- [ ] El navegador no muestra errores CORS al enviar un análisis desde el frontend.
- [ ] `POST https://<backend-render>/analisis-financiero` responde un análisis válido con un payload de prueba.
- [ ] Si ML real está habilitado, la respuesta del Backend refleja inferencia/clasificación esperada o los errores controlados del `ml-service`.

Comandos útiles para validar manualmente:

```bash
curl https://<backend-render>/actuator/health
curl https://<ml-service-render>/health
```

No commitear archivos `.env`, `.oci`, `.pkl`, caches ni credenciales generadas durante la validación.
