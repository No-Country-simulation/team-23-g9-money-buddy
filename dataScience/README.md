# Data science API REST
El API REST del proyecto de money buddy cuenta con la ayuda de 3 IA's (por el momento), las cuales ayudan a predecir y categorizar el perfil financiero del usuario y las categorias de cada compra que realiza. Este documento contiene la siguiente informacion:
- 1. Estructura de carpetas
- 2. Configuracion de claves privadas
- 3. Instalacion de librerias
- 4. Uso de docker
- 5. EndPoints 

## Estructura del pryecto
```
datascience/
├── .oci/                   # Carpeta para acceso a servicios del bucket
│   └──config.example       # Contiene un ejemplo de los datos que debe de contener la configuracion para el acceso al bucket de OCI
├── app/
│   ├── __init__.py         # archivo vacio que ayuda a mitigar errores de importacion ante docker
│   ├── main.py             # Archivo principal que tiene los endpoints y la configuracion del framework en fastAPI
│   ├── models.py           # Archivo encargado de cargar los modelos en el proyecto
│   ├── oci_storage.oy      # Archivo encargado de descargar los modelos dentro del bucket de OCI
│   ├── processing.py       # Archivo encargado de procesar los datos antes de generar una prediccion por los modelos
│   ├── recomendation.py    # Motor de recomendaciones, que procesa la informacion y dependiendo de los resultados da como maximo 5 recomendaciones
|   └── schemas.py          # Archivo que define todas las entradas y salidas de los datos principales
├── models/                 # Carpeta que guardara los modelos que se descarguen de OCI
├── .env.example            # archivo de ejemplo de las claves privadas necesarias para el proyecto
├── .dockerignore           # archivo que evita subir configuraciones privadas y archivos no necesarios para el proyecto
├── requirements.txt        # documento que tiene todas las dependencias
└── Dockerfile              # Archivo que rige la creacion del docker
```
## Configuracion de claves privadas
1. Dentro de la carpeta de .oci siguiendo la estructura:
```
datascience/
├── .oci/                   
│   └──config.example 
```
Clonar el archivo config.example y renombrarlo a solo .config, y sustituir los valores de muestra por valores reales proporcionados por el dueño del bucket, ademas de poner en esa misma carpeta la llave privada para el acceso al mismo

2. En la carpeta raiz del proyecto siguiendo la estructura:
datascience/
└── .env.example 
Clonar el archivo .env.example y renombrarlo a solo .env, y sustituir los valores de muestra por valores reales proporcionados por el dueño del bucket

## Instalacion de librerias
Este paso es solo si se quiere ejecutar la seccion del proyecto por separado de lo demas o en local sin uso de otras herramientos como docker, para esto se recomenda tener la version de python 3.12 o superior
### 1. Instalacion del entorno virtual
```
cd datascience
python -m venv .venv
.venv/Scripts/activate  #comando para activar el entorno virtual
```
### 2. instalacion de requirements
```
pip install -r requirements.txt   
```
### 3. Ejecucion del proyecto
```
uvicorn dataScience.app.main:app --reload --no-cache   # si es que estas en la carpeta raiz
uvicorn app.main:app --reload --no-cache               # si estas en la carpeta de datascience
```
### 4. Salir del proyecto y del entorno virtual
```
CTRL + C # desactiva la ejecucion del proyecto
deactivate # se sale del entorno virtual
```

## Uso de docker
Para el uso correcto de docker y sin fallos es necesario realizar la configuracion de claves privadas, una vez solventado eso ejecutar los siguientes comandos
```
docker-compose up -d
```
Si se realizo algun cambio al proyecto ejecturar
```
docker-compose down
docker-compose up -d --build
```

## Endpoints
| Método |         Ruta         |                           Descripción                            |
|--------|----------------------|------------------------------------------------------------------|
|GET     |/health               |Regresa un json de si esta funcionando la API                     |
|POST    |/predecir-categoria   |Realiza la prediccion de la categoria                             |
|POST    |/perfil-financiero    |Realiza la prediccion del score financiero y el perfil financiero |

### Parametros /predecir-categoria
- Income
    - tipo: str
    - fecha: str
    - descripcion: str
    - tipoPago: str
    - meses_a_deber: Optional[int] = None
    - monto: float

- Response
    - tipo: str
    - fecha: str
    - descripcion: str
    - tipoPago: str
    - meses_a_deber: Optional[int] = None
    - monto: float
    - categoria: Optional[str] = None

#### Ejemplo
| Income | Response |
|--------|----------|
| `{"transacciones":[{"tipo":"Egreso","fecha":"2026-07-01","descripcion":"Supermercado","tipoPago":"Efectivo","meses_a_deber":0,"monto":420,"categoria":"alimentacion"},{"tipo":"Egreso","fecha":"2026-07-02","descripcion":"Gas","tipoPago":"Credito","meses_a_deber":1,"monto":420,"categoria":"servicios"},{"tipo":"Ingreso","fecha":"2026-07-01","descripcion":"Encargo","tipoPago":"Transferencia","meses_a_deber":0,"monto":420,"categoria":"Ingreso"}]}` | `{"success":true,"message":"transacciones clasificadas correctamente","data":{"transacciones_clasificadas":[{"tipo":"Egreso","fecha":"2026-07-01","descripcion":"Supermercado","tipoPago":"Efectivo","meses_a_deber":0,"monto":420.0,"categoria":"vivienda"},{"tipo":"Egreso","fecha":"2026-07-02","descripcion":"Gas","tipoPago":"Credito","meses_a_deber":1,"monto":420.0,"categoria":"vivienda"},{"tipo":"Ingreso","fecha":"2026-07-01","descripcion":"Encargo","tipoPago":"Transferencia","meses_a_deber":0,"monto":420.0,"categoria":"otro"}]}}` |

### Parametros /perfil-financiero
- Income
    - credito_total: float
    - ingreso_mensual: float
    - frecuencia_ahorro: str
    - nivel_endeudamiento: float
    - pago_mensual_deudas: float
    - transacciones: list[TransaccionClasificada] #estructura de /predecir-categoria
    
- Response
    - perfil_financiero: str
    - score_financiero: float
    - resumen_gastos: dict[str, float]
    - ingreso_mensual: float
    - deuda_total: float
    - credito_total: float
    - frecuencia_ahorro: str
    - nivel_endeudamiento: float
    - pago_mensual_deudas: float
    - gasto_total: float
    - ratio_pago_deudas: float
    - ratio_deuda_ingreso: float
    - porcentaje_alimentos: float = 0
    - porcentaje_transporte: float = 0
    - porcentaje_entretenimiento: float = 0
    - porcentaje_salud: float = 0
    - porcentaje_vivienda: float = 0
    - porcentaje_educacion: float = 0
    - porcentaje_viajes: float = 0
    - porcentaje_servicios: float = 0
    - porcentaje_otros: float = 0
    - transacciones_clasificadas: list[TransaccionClasificada]
    - recomendaciones: list[str]

| Income | Response |
|--------|----------|
| `{"ingreso_mensual":4000,"credito_total":20000,"frecuencia_ahorro":"Nula","nivel_endeudamiento":100,"pago_mensual_deudas":700,"transacciones":[{"tipo":"Egreso","fecha":"2026-07-01","descripcion":"Supermercado","tipoPago":"Efectivo","meses_a_deber":0,"monto":420,"categoria":"alimentacion"},{"tipo":"Egreso","fecha":"2026-07-02","descripcion":"Gas","tipoPago":"Credito","meses_a_deber":1,"monto":420,"categoria":"servicios"},{"tipo":"Ingreso","fecha":"2026-07-01","descripcion":"Encargo","tipoPago":"Transferencia","meses_a_deber":0,"monto":420,"categoria":"Ingreso"}]}` | `{"success":true,"message":"Perfil financiero obtenido correctamente","data":{"perfil_financiero":"En observacion","score_financiero":49.44,"resumen_gastos":{"alimentacion":420.0,"servicios":420.0},"indicadores":{"ingreso_mensual":2000.0,"deuda_total":10000.0,"credito_total":20000.0,"frecuencia_ahorro":"Baja","nivel_endeudamiento":50.0,"pago_mensual_deudas":700.0,"gasto_total":840.0,"ratio_pago_deudas":0.35,"ratio_deuda_ingreso":5.0,"porcentaje_alimentos":50.0,"porcentaje_transporte":0.0,"porcentaje_entretenimiento":0.0,"porcentaje_salud":0.0,"porcentaje_vivienda":0.0,"porcentaje_educacion":0.0,"porcentaje_viajes":0.0,"porcentaje_servicios":50.0,"porcentaje_otros":0.0},"transacciones_clasificadas":[{"tipo":"Egreso","fecha":"2026-07-01","descripcion":"Supermercado","tipoPago":"Efectivo","meses_a_deber":0,"monto":420.0,"categoria":"alimentacion"},{"tipo":"Egreso","fecha":"2026-07-02","descripcion":"Gas","tipoPago":"Credito","meses_a_deber":1,"monto":420.0,"categoria":"servicios"},{"tipo":"Ingreso","fecha":"2026-07-01","descripcion":"Encargo","tipoPago":"Transferencia","meses_a_deber":0,"monto":420.0,"categoria":"Ingreso"}],"recomendaciones":["Planificar el menu semanal con lista de compras fija; meta de bajar a 18% del gasto total en 8 semanas."]}}` |
 
