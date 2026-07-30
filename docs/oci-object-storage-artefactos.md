# OCI Object Storage Free Tier para artefactos

Este documento define el plan seguro para crear un bucket de OCI Object Storage Free Tier y usarlo como almacenamiento de artefactos del proyecto. No confirma que el bucket ya exista: la creacion y la evidencia deben completarse manualmente desde una cuenta OCI lista para operar.

## Proposito

Usar Object Storage como punto unico para guardar artefactos tecnicos del proyecto, principalmente entregables de Data Science y archivos auxiliares que Backend pueda descargar o usar durante la integracion.

## Estado actual

| Punto | Estado |
| --- | --- |
| Bucket OCI | Pendiente de creacion manual |
| Upload verificado | Pendiente de evidencia |
| Recursos creados desde este repositorio | Ninguno |
| Secretos requeridos en Git | Ninguno |

## Plan de bucket

| Campo | Valor propuesto |
| --- | --- |
| Nombre | `money-buddy-artifacts-free-tier` |
| Servicio | OCI Object Storage |
| Tipo de almacenamiento | Standard |
| Visibilidad | Private |
| Uso | Artefactos internos del MVP |
| Versionado | Desactivado para MVP, salvo necesidad explicita del equipo |
| Retencion | Sin reglas especiales para MVP |

Si el nombre ya existe en el namespace de la tenancy, usar un sufijo corto y no sensible, por ejemplo `money-buddy-artifacts-free-tier-dev`.

## Archivos permitidos

Se pueden subir archivos que no contengan secretos ni datos personales reales:

- modelos serializados o exportados por Data Science, por ejemplo `.pkl`, `.joblib`, `.onnx` o `.zip`.
- datasets sinteticos o anonimizados usados para pruebas.
- documentos tecnicos de intercambio entre Data Science y Backend.
- artefactos generados del MVP que sean necesarios para integracion o demo.

No subir archivos con datos financieros reales de usuarios, credenciales, llaves privadas, tokens, exports completos de configuracion OCI ni logs con URLs firmadas.

## Creacion desde consola OCI

1. Entrar a la consola de OCI con una cuenta autorizada del proyecto.
2. Seleccionar el compartimento acordado para el MVP.
3. Ir a `Storage` > `Object Storage & Archive Storage` > `Buckets`.
4. Crear un bucket con el nombre `money-buddy-artifacts-free-tier`.
5. Mantener el bucket como `Private`.
6. Usar `Standard` como storage tier.
7. Dejar versionado y reglas avanzadas desactivadas para el MVP, salvo decision explicita del equipo.
8. Guardar una captura o texto de evidencia sin mostrar identificadores sensibles.

## Upload manual desde consola OCI

1. Entrar al bucket creado.
2. Elegir `Upload`.
3. Subir un archivo de prueba seguro, por ejemplo `artifact-upload-check.txt` con contenido no sensible.
4. Confirmar que el objeto aparece listado dentro del bucket.
5. Registrar evidencia sin exponer URLs firmadas ni identificadores sensibles.

## Ruta opcional por CLI

Usar CLI solo si ya esta configurada localmente de forma segura. No guardar ni commitear archivos de configuracion OCI en este repositorio.

Ejemplo de comandos con placeholders seguros:

```bash
oci os bucket create \
  --compartment-id "<compartment-ocid>" \
  --name "money-buddy-artifacts-free-tier" \
  --public-access-type NoPublicAccess \
  --storage-tier Standard

oci os object put \
  --bucket-name "money-buddy-artifacts-free-tier" \
  --name "artifact-upload-check.txt" \
  --file "./artifact-upload-check.txt"

oci os object list \
  --bucket-name "money-buddy-artifacts-free-tier"
```

Los placeholders no deben reemplazarse dentro de archivos versionados. Ejecutarlos solo en una terminal local segura.

## Checklist de evidencia

Antes de cerrar el issue o el PR, registrar evidencia verificable y sin secretos:

- [ ] Nombre del bucket creado.
- [ ] Region visible solo si no expone informacion sensible del equipo.
- [ ] Fecha de creacion o verificacion.
- [ ] Nombre del objeto subido como prueba.
- [ ] Resultado esperado: el objeto aparece listado en el bucket privado.
- [ ] Confirmacion de que no se publico el bucket.
- [ ] Confirmacion de que no se agregaron credenciales ni configuracion OCI al repositorio.

## Plantilla segura para issue o PR

```markdown
## Evidencia OCI Object Storage

- Bucket: `money-buddy-artifacts-free-tier`
- Visibilidad: Private
- Storage tier: Standard
- Objeto de prueba: `artifact-upload-check.txt`
- Fecha de verificacion: YYYY-MM-DD
- Verificacion realizada: el objeto aparece listado dentro del bucket privado.
- Secretos expuestos: No
- Archivos OCI agregados al repositorio: No

Notas:
- No se adjuntan tenancy OCID, user OCID, fingerprint, llaves privadas, tokens, config local ni URLs firmadas.
```

## Reglas de seguridad

No commitear ni pegar en issues, PRs, chats o logs publicos:

- archivos de configuracion OCI.
- llaves privadas.
- tokens.
- tenancy OCID.
- user OCID.
- fingerprint.
- secretos especificos de region.
- configuracion generada por la CLI.
- logs con URLs firmadas.
- credenciales descargadas desde OCI.

Si algun dato sensible se expone por error, eliminarlo del canal afectado y rotar la credencial inmediatamente.

## Rollback y limpieza

Si el bucket se creo mal o ya no se necesita:

1. Verificar que no contenga artefactos necesarios para la demo o integracion.
2. Descargar o mover solo artefactos no sensibles que deban conservarse.
3. Eliminar los objetos del bucket.
4. Eliminar el bucket desde consola OCI.
5. Registrar en el issue o PR que la limpieza fue realizada sin exponer identificadores sensibles.

## Traspaso de owner

Owner operativo sugerido: integrante con acceso a la cuenta OCI del equipo.

Responsabilidades del owner:

- crear o validar el bucket.
- subir el archivo de prueba.
- pegar la plantilla de evidencia segura en el issue o PR.
- confirmar que el bucket sigue privado.
- avisar al equipo si cambia el nombre del bucket o el compartimento usado.
