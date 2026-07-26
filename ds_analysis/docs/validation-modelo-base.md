# Validación del Modelo Base - Money Buddy

## Propósito

Este notebook (`Validation_modelo_base.ipynb`) cumple dos objetivos principales para el Sprint 1:

1. **Validar el contrato** del dataset financiero frente a la API de análisis financiero del backend.
2. **Generar un modelo base** de clasificación de categorías de transacciones.

## Estructura del notebook

| Sección | Descripción |
|---------|-------------|
| 1. Introducción | Contexto y objetivos del análisis |
| 2. Carga del dataset | Lectura del CSV `personal_finance_dataset_8000_extended.csv` |
| 3. EDA básico | Exploración: dimensiones, tipos, nulos, estadísticos, distribuciones por categoría, método de pago, ubicación, dispositivo, día, momento del día |
| 4. Calidad del dataset | Verificación de nulos, duplicados, montos negativos, categorías vacías |
| 5. Cobertura respecto al contrato | Tabla comparativa entre campos del dataset y campos requeridos por la API |
| 6. Normalización | Renombrado de columnas, generación de campos simulados (ingreso_mensual, credito_total, etc.), normalización de tipoPago y tipo |
| 7. Preparación para modelo base | Creación de pipeline (OneHotEncoder + CountVectorizer + LogisticRegression), entrenamiento, evaluación y serialización |

## Datasets

### Entrada
- **Archivo:** `data_sets/personal_finance_dataset_8000_extended.csv`
- **Registros:** 8,000 transacciones
- **Columnas originales:** 15 (Date, Description, Amount, Category, PaymentMethod, Location, AccountType, TransactionType, DeviceUsed, Currency, MerchantType, LoyaltyProgram, Weekday, Month, TimeOfDay)

### Salida
- **Archivo:** `data_sets/personal_finance_dataset_normalized.csv`
- **Registros:** 8,000
- **Columnas:** 12 (credito_total, ingreso_mensual, frecuencia_ahorro, nivel_endeudamiento, pago_mensual_deudas, tipo, fecha, descripcion, tipoPago, monto, meses_a_deber, categoria)

## Modelo base

| Aspecto | Detalle |
|---------|---------|
| Tipo | Clasificación de categorías de transacciones |
| Algoritmo | LogisticRegression (liblinear, max_iter=1000) |
| Features | descripcion (CountVectorizer), monto, tipoPago (OneHot), tipo (OneHot) |
| Target | categoria (10 clases) |
| Split | 80% train / 20% test, stratify=y, random_state=42 |
| Accuracy | 58% |
| Artefacto | `models_analysis/baseline_transaction_classifier.joblib` |

### Métricas por categoría

| Categoría | Precision | Recall | F1-score |
|-----------|-----------|--------|----------|
| Bills | 0.61 | 0.55 | 0.58 |
| Clothing | 1.00 | 0.14 | 0.25 |
| Electronics | 0.78 | 0.89 | 0.83 |
| Entertainment | 0.00 | 0.00 | 0.00 |
| Food | 0.28 | 0.85 | 0.42 |
| Grocery | 0.88 | 0.70 | 0.78 |
| Healthcare | 0.82 | 0.90 | 0.86 |
| Online Shopping | 0.51 | 0.90 | 0.65 |
| Transport | 0.58 | 0.32 | 0.42 |
| Travel | 1.00 | 0.49 | 0.66 |

## Campos simulados

El dataset original no contiene datos de perfil financiero del usuario. Se generaron valores aleatorios para cumplir con el contrato de la API:

| Campo | Tipo | Rango simulado |
|-------|------|----------------|
| credito_total | float | 5,000 - 50,000 |
| ingreso_mensual | float | 1,000 - 10,000 |
| frecuencia_ahorro | string | NULA, BAJA, MEDIA, ALTA |
| nivel_endeudamiento | int | 0 - 99 |
| pago_mensual_deudas | float | 100 - 2,000 |
| meses_a_deber | int | 0 (no crédito), 1-11 (crédito) |

## Dependencias

- pandas
- numpy
- matplotlib
- seaborn
- scikit-learn (OneHotEncoder, CountVectorizer, LogisticRegression, train_test_split, classification_report)
- joblib

## Notas

- El accuracy del 58% es esperado para un modelo base con datos sintéticos. El modelo sirve como línea de base para comparar mejoras futuras.
- La categoría `Entertainment` tiene F1=0.00, lo que indica que el modelo no logra distinguirla. Esto puede mejorarse con features adicionales o un modelo más complejo.
- Las rutas en el notebook están configuradas para ejecución local (relativas a `notebooks/`). Para Google Colab, descomentar las rutas de Drive.
