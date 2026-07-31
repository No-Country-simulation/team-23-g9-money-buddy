import { useState, type FormEvent } from 'react'
import accountBalanceIcon from './assets/account_balance.svg'
import attachMoneyIcon from './assets/attach_money.svg'
import creditCardIcon from './assets/credit_card.svg'
import ecoIcon from './assets/eco.svg'
import financeIcon from './assets/finance.svg'
import headerNoteIcon from './assets/header-note-icon.svg'
import savingsIcon from './assets/savings.svg'

const TRANSACTION_TYPE = {
  INCOME: 'Ingreso',
  EXPENSE: 'Egreso',
} as const

const PAYMENT_TYPE = {
  CASH: 'Efectivo',
  DEBIT: 'Debito',
  CREDIT: 'Credito',
} as const

const SAVING_FREQUENCY = {
  NONE: 'NULA',
  LOW: 'BAJA',
  MEDIUM: 'MEDIA',
  HIGH: 'ALTA',
} as const

const SUBMIT_STATUS = {
  IDLE: 'idle',
  LOADING: 'loading',
  SUCCESS: 'success',
  ERROR: 'error',
} as const

type TransactionType = (typeof TRANSACTION_TYPE)[keyof typeof TRANSACTION_TYPE]
type PaymentType = (typeof PAYMENT_TYPE)[keyof typeof PAYMENT_TYPE]
type SavingFrequency = (typeof SAVING_FREQUENCY)[keyof typeof SAVING_FREQUENCY]
type SubmitStatus = (typeof SUBMIT_STATUS)[keyof typeof SUBMIT_STATUS]

interface FinancialFormState {
  credito_total: string
  ingreso_mensual: string
  frecuencia_ahorro: SavingFrequency
  pago_mensual_deudas: string
}

interface MovementFormState {
  tipo: TransactionType
  fecha: string
  descripcion: string
  tipo_pago: PaymentType
  meses_a_deber: string
  monto: string
}

interface Movement extends MovementFormState {
  id: string
}

interface TransactionRequest {
  tipo: TransactionType
  fecha: string
  descripcion: string
  tipo_pago?: PaymentType
  meses_a_deber?: number
  monto: number
}

interface FinancialAnalysisRequest {
  credito_total: number
  ingreso_mensual: number
  frecuencia_ahorro: SavingFrequency
  pago_mensual_deudas: number
  transacciones: TransactionRequest[]
}

interface AnalysisState {
  status: SubmitStatus
  response: unknown
  errors: string[]
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const MAX_MOVEMENTS = 100
const MAX_DESCRIPTION_LENGTH = 200

const initialFinancialForm: FinancialFormState = {
  credito_total: '1500',
  ingreso_mensual: '1000',
  frecuencia_ahorro: SAVING_FREQUENCY.MEDIUM,
  pago_mensual_deudas: '150',
}

const initialMovementForm: MovementFormState = {
  tipo: TRANSACTION_TYPE.EXPENSE,
  fecha: new Date().toISOString().slice(0, 10),
  descripcion: '',
  tipo_pago: PAYMENT_TYPE.DEBIT,
  meses_a_deber: '',
  monto: '',
}

const initialMovements: Movement[] = [
  {
    id: 'sample-income',
    tipo: TRANSACTION_TYPE.INCOME,
    fecha: '2026-07-01',
    descripcion: 'Salario',
    tipo_pago: PAYMENT_TYPE.DEBIT,
    meses_a_deber: '',
    monto: '1000',
  },
  {
    id: 'sample-market',
    tipo: TRANSACTION_TYPE.EXPENSE,
    fecha: '2026-07-20',
    descripcion: 'Supermercado',
    tipo_pago: PAYMENT_TYPE.DEBIT,
    meses_a_deber: '',
    monto: '120.50',
  },
]

function toMoney(value: string | number) {
  const amount = typeof value === 'number' ? value : Number(value)
  return new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 2,
  }).format(Number.isFinite(amount) ? amount : 0)
}

function toNumber(value: string) {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isPaymentType(value: string): value is PaymentType {
  return Object.values(PAYMENT_TYPE).includes(value as PaymentType)
}

function buildTransaction(movement: Movement): TransactionRequest {
  const transaction: TransactionRequest = {
    tipo: movement.tipo,
    fecha: movement.fecha,
    descripcion: movement.descripcion.trim(),
    monto: toNumber(movement.monto),
  }

  if (movement.tipo === TRANSACTION_TYPE.EXPENSE) {
    transaction.tipo_pago = movement.tipo_pago
  }

  if (movement.tipo === TRANSACTION_TYPE.EXPENSE && movement.tipo_pago === PAYMENT_TYPE.CREDIT) {
    transaction.meses_a_deber = Math.trunc(toNumber(movement.meses_a_deber))
  }

  return transaction
}

function buildRequest(form: FinancialFormState, movements: Movement[]): FinancialAnalysisRequest {
  return {
    credito_total: toNumber(form.credito_total),
    ingreso_mensual: toNumber(form.ingreso_mensual),
    frecuencia_ahorro: form.frecuencia_ahorro,
    pago_mensual_deudas: toNumber(form.pago_mensual_deudas),
    transacciones: movements.map(buildTransaction),
  }
}

function validateRequest(request: FinancialAnalysisRequest) {
  const errors: string[] = []

  if (request.ingreso_mensual <= 0) {
    errors.push('El ingreso mensual debe ser mayor a 0.')
  }

  if (request.credito_total < 0) {
    errors.push('El crédito total no puede ser negativo.')
  }

  if (request.pago_mensual_deudas < 0) {
    errors.push('El pago mensual de deudas no puede ser negativo.')
  }

  if (request.transacciones.length === 0) {
    errors.push('Agregá al menos un movimiento para analizar tus finanzas.')
  }

  if (request.transacciones.length > MAX_MOVEMENTS) {
    errors.push(`Podés analizar hasta ${MAX_MOVEMENTS} movimientos por solicitud.`)
  }

  request.transacciones.forEach((movement, index) => {
    const label = `Movimiento ${index + 1}`

    if (!movement.fecha) {
      errors.push(`${label}: completá la fecha.`)
    }

    if (!movement.descripcion.trim()) {
      errors.push(`${label}: escribí una descripción.`)
    }

    if (movement.descripcion.length > MAX_DESCRIPTION_LENGTH) {
      errors.push(`${label}: la descripción no puede superar ${MAX_DESCRIPTION_LENGTH} caracteres.`)
    }

    if (movement.monto <= 0) {
      errors.push(`${label}: el monto debe ser mayor a 0.`)
    }

    if (movement.tipo === TRANSACTION_TYPE.EXPENSE) {
      if (!movement.tipo_pago || !isPaymentType(movement.tipo_pago)) {
        errors.push(`${label}: seleccioná el tipo de pago.`)
      }

      if (movement.tipo_pago === PAYMENT_TYPE.CREDIT && (!movement.meses_a_deber || movement.meses_a_deber < 1)) {
        errors.push(`${label}: los meses a deber deben ser 1 o más para pagos con crédito.`)
      }
    }
  })

  return errors
}

function getBackendErrorMessages(payload: unknown) {
  if (!isRecord(payload) || !Array.isArray(payload.errores)) {
    return []
  }

  return payload.errores.flatMap((error) => {
    if (!isRecord(error) || typeof error.mensaje !== 'string') {
      return []
    }

    return typeof error.campo === 'string' && error.campo.trim()
      ? [`${error.campo}: ${error.mensaje}`]
      : [error.mensaje]
  })
}

function getFinancialHeadline(response: unknown) {
  if (!isRecord(response) || !isRecord(response.data)) {
    return null
  }

  const profile = response.data.perfil_financiero
  const score = response.data.score_financiero

  if (typeof profile !== 'string' && typeof score !== 'number') {
    return null
  }

  return {
    profile: typeof profile === 'string' ? profile : 'sin perfil',
    score: typeof score === 'number' ? score : null,
  }
}

export default function App() {
  const [financialForm, setFinancialForm] = useState<FinancialFormState>(initialFinancialForm)
  const [movementForm, setMovementForm] = useState<MovementFormState>(initialMovementForm)
  const [movements, setMovements] = useState<Movement[]>(initialMovements)
  const [editingMovementId, setEditingMovementId] = useState<string | null>(null)
  const [analysis, setAnalysis] = useState<AnalysisState>({
    status: SUBMIT_STATUS.IDLE,
    response: null,
    errors: [],
  })

  const requestBody = buildRequest(financialForm, movements)
  const requestPreview = JSON.stringify(requestBody, null, 2)
  const headline = getFinancialHeadline(analysis.response)
  const isSubmitting = analysis.status === SUBMIT_STATUS.LOADING
  const reachedMovementLimit = !editingMovementId && movements.length >= MAX_MOVEMENTS

  function updateFinancialField<Key extends keyof FinancialFormState>(
    key: Key,
    value: FinancialFormState[Key],
  ) {
    setFinancialForm((current) => ({ ...current, [key]: value }))
  }

  function updateMovementField<Key extends keyof MovementFormState>(
    key: Key,
    value: MovementFormState[Key],
  ) {
    setMovementForm((current) => ({ ...current, [key]: value }))
  }

  function resetMovementForm() {
    setMovementForm(initialMovementForm)
    setEditingMovementId(null)
  }

  function handleMovementSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (editingMovementId) {
      setMovements((current) =>
        current.map((movement) =>
          movement.id === editingMovementId ? { ...movementForm, id: editingMovementId } : movement,
        ),
      )
    } else {
      if (movements.length >= MAX_MOVEMENTS) {
        setAnalysis({
          status: SUBMIT_STATUS.ERROR,
          response: null,
          errors: [`Podés agregar hasta ${MAX_MOVEMENTS} movimientos por análisis.`],
        })
        return
      }

      setMovements((current) => [...current, { ...movementForm, id: crypto.randomUUID() }])
    }

    resetMovementForm()
  }

  function handleEditMovement(movement: Movement) {
    setMovementForm({
      tipo: movement.tipo,
      fecha: movement.fecha,
      descripcion: movement.descripcion,
      tipo_pago: movement.tipo_pago,
      meses_a_deber: movement.meses_a_deber,
      monto: movement.monto,
    })
    setEditingMovementId(movement.id)
  }

  function handleRemoveMovement(id: string) {
    setMovements((current) => current.filter((movement) => movement.id !== id))
    if (editingMovementId === id) {
      resetMovementForm()
    }
  }

  async function handleSubmitAnalysis() {
    const validationErrors = validateRequest(requestBody)

    if (validationErrors.length > 0) {
      setAnalysis({ status: SUBMIT_STATUS.ERROR, response: null, errors: validationErrors })
      return
    }

    setAnalysis({ status: SUBMIT_STATUS.LOADING, response: null, errors: [] })

    try {
      const response = await fetch(`${API_BASE_URL}/analisis-financiero`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
      })
      const payload: unknown = await response.json().catch(() => null)

      if (!response.ok) {
        const backendErrors = getBackendErrorMessages(payload)
        setAnalysis({
          status: SUBMIT_STATUS.ERROR,
          response: payload,
          errors:
            backendErrors.length > 0 ? backendErrors : [`El backend respondió con estado ${response.status}.`],
        })
        return
      }

      setAnalysis({ status: SUBMIT_STATUS.SUCCESS, response: payload, errors: [] })
    } catch (error) {
      const message = error instanceof Error ? error.message : 'No se pudo completar la solicitud.'
      setAnalysis({
        status: SUBMIT_STATUS.ERROR,
        response: null,
        errors: [`${message} Revisá si el backend está levantado en ${API_BASE_URL}.`],
      })
    }
  }

  return (
    <div className="app-shell">
      <header className="hero-header">
        <a className="brand" href="#top" aria-label="Ir al inicio de Money Buddy">
          <span className="brand-mark" aria-hidden="true">
            <img src={savingsIcon} alt="" />
          </span>
          <span>
            <small>Tu mejor aliado</small>
            <strong>
              <span className="brand-word-money">Money</span>{' '}
              <span className="brand-word-buddy">Buddy</span>
            </strong>
          </span>
        </a>
        <p className="header-note">
          <img src={headerNoteIcon} alt="" aria-hidden="true" />
          <span>
            Pequeñas decisiones hoy, <strong>grandes cambios mañana.</strong>
          </span>
        </p>
      </header>

      <main id="top" className="dashboard" aria-label="Dashboard de análisis financiero">
        <section className="left-column" aria-label="Datos para analizar">
          <article className="card finance-card">
            <div className="section-heading">
              <h1>Cuéntanos sobre tus finanzas</h1>
              <p>Completá los campos para preparar tu análisis.</p>
            </div>

            <div className="field-grid">
              <label className="field">
                <span className="field-label">
                  <span className="field-icon field-icon-income" aria-hidden="true">
                    <img src={attachMoneyIcon} alt="" />
                  </span>
                  Ingreso mensual
                </span>
                <input
                  type="number"
                  min="1"
                  step="0.01"
                  inputMode="decimal"
                  value={financialForm.ingreso_mensual}
                  onChange={(event) => updateFinancialField('ingreso_mensual', event.currentTarget.value)}
                />
              </label>

              <label className="field">
                <span className="field-label">
                  <span className="field-icon field-icon-credit" aria-hidden="true">
                    <img src={creditCardIcon} alt="" />
                  </span>
                  Crédito total
                </span>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  inputMode="decimal"
                  value={financialForm.credito_total}
                  onChange={(event) => updateFinancialField('credito_total', event.currentTarget.value)}
                />
                <small>Límite o monto total de tus tarjetas/líneas de crédito.</small>
              </label>

              <label className="field">
                <span className="field-label">
                  <span className="field-icon field-icon-debt" aria-hidden="true">
                    <img src={accountBalanceIcon} alt="" />
                  </span>
                  Pago mensual de deudas
                </span>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  inputMode="decimal"
                  value={financialForm.pago_mensual_deudas}
                  onChange={(event) => updateFinancialField('pago_mensual_deudas', event.currentTarget.value)}
                />
              </label>

              <fieldset className="frequency-group">
                <legend>
                  <span className="field-label">
                    <span className="field-icon field-icon-savings" aria-hidden="true">
                      <img src={savingsIcon} alt="" />
                    </span>
                    Frecuencia de ahorro
                  </span>
                </legend>
                {Object.values(SAVING_FREQUENCY).map((frequency) => (
                  <label key={frequency}>
                    <input
                      type="radio"
                      name="frecuencia_ahorro"
                      value={frequency}
                      checked={financialForm.frecuencia_ahorro === frequency}
                      onChange={() => updateFinancialField('frecuencia_ahorro', frequency)}
                    />
                    <span>{frequency}</span>
                  </label>
                ))}
              </fieldset>
            </div>
          </article>

          <article className="card movements-card">
            <div className="section-heading compact-heading">
              <div>
                <h2>Tus movimientos</h2>
                <p>Agregá ingresos y egresos del mes actual.</p>
              </div>
              <span className="movement-count">{movements.length} movimientos</span>
            </div>

            <form className="movement-form" onSubmit={handleMovementSubmit}>
              <div className="movement-form-grid">
                <label className="field">
                  <span>Tipo</span>
                  <select
                    value={movementForm.tipo}
                    onChange={(event) => updateMovementField('tipo', event.currentTarget.value as TransactionType)}
                  >
                    <option value={TRANSACTION_TYPE.EXPENSE}>Egreso</option>
                    <option value={TRANSACTION_TYPE.INCOME}>Ingreso</option>
                  </select>
                </label>

                <label className="field">
                  <span>Fecha</span>
                  <input
                    type="date"
                    required
                    value={movementForm.fecha}
                    onChange={(event) => updateMovementField('fecha', event.currentTarget.value)}
                  />
                </label>

                <label className="field wide-field">
                  <span>Descripción</span>
                  <input
                    type="text"
                    required
                    maxLength={MAX_DESCRIPTION_LENGTH}
                    placeholder="Ej. supermercado, salario, gas"
                    value={movementForm.descripcion}
                    onChange={(event) => updateMovementField('descripcion', event.currentTarget.value)}
                  />
                  <small>
                    {movementForm.descripcion.length}/{MAX_DESCRIPTION_LENGTH} caracteres
                  </small>
                </label>

                {movementForm.tipo === TRANSACTION_TYPE.EXPENSE ? (
                  <label className="field">
                    <span>Tipo de pago</span>
                    <select
                      value={movementForm.tipo_pago}
                      onChange={(event) => updateMovementField('tipo_pago', event.currentTarget.value as PaymentType)}
                    >
                      <option value={PAYMENT_TYPE.DEBIT}>Débito</option>
                      <option value={PAYMENT_TYPE.CASH}>Efectivo</option>
                      <option value={PAYMENT_TYPE.CREDIT}>Crédito</option>
                    </select>
                  </label>
                ) : null}

                {movementForm.tipo === TRANSACTION_TYPE.EXPENSE && movementForm.tipo_pago === PAYMENT_TYPE.CREDIT ? (
                  <label className="field">
                    <span>Meses a deber</span>
                    <input
                      type="number"
                      min="1"
                      step="1"
                      required
                      inputMode="numeric"
                      value={movementForm.meses_a_deber}
                      onChange={(event) => updateMovementField('meses_a_deber', event.currentTarget.value)}
                    />
                  </label>
                ) : null}

                <label className="field">
                  <span>Monto</span>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    required
                    inputMode="decimal"
                    value={movementForm.monto}
                    onChange={(event) => updateMovementField('monto', event.currentTarget.value)}
                  />
                </label>
              </div>

              <div className="form-actions">
                <button className="secondary-button" type="submit" disabled={reachedMovementLimit}>
                  {editingMovementId ? 'Guardar cambios' : '+ Agregar movimiento'}
                </button>
                {reachedMovementLimit ? <p className="form-limit">Llegaste al máximo de {MAX_MOVEMENTS} movimientos.</p> : null}
                {editingMovementId ? (
                  <button className="ghost-button" type="button" onClick={resetMovementForm}>
                    Cancelar edición
                  </button>
                ) : null}
              </div>
            </form>

            <div className="movement-list" aria-live="polite">
              {movements.length === 0 ? (
                <p className="empty-list">Aún no hay movimientos. ¡Agregá el primero!</p>
              ) : (
                movements.map((movement) => (
                  <article className="movement-item" key={movement.id}>
                    <div>
                      <strong>{movement.descripcion}</strong>
                      <span>
                        {movement.fecha} · {movement.tipo}
                        {movement.tipo === TRANSACTION_TYPE.EXPENSE ? ` · ${movement.tipo_pago}` : ''}
                      </span>
                    </div>
                    <strong className={movement.tipo === TRANSACTION_TYPE.INCOME ? 'positive-amount' : 'negative-amount'}>
                      {movement.tipo === TRANSACTION_TYPE.INCOME ? '+' : '-'} {toMoney(movement.monto)}
                    </strong>
                    <div className="row-actions" aria-label={`Acciones para ${movement.descripcion}`}>
                      <button type="button" onClick={() => handleEditMovement(movement)}>
                        Editar
                      </button>
                      <button type="button" onClick={() => handleRemoveMovement(movement.id)}>
                        Eliminar
                      </button>
                    </div>
                  </article>
                ))
              )}
            </div>

            <button className="primary-button" type="button" disabled={isSubmitting} onClick={handleSubmitAnalysis}>
              {isSubmitting ? 'Analizando…' : 'Analizar mis finanzas'}
            </button>
            <p className="privacy-note">Tus datos están 100% seguros y privados.</p>
          </article>
        </section>

        <aside className="right-column" aria-label="Resultado del análisis">
          <article className="card analysis-card">
            {analysis.status === SUBMIT_STATUS.SUCCESS ? (
              <div className="analysis-result">
                <span className="analysis-icon" aria-hidden="true">
                  <img className="analysis-icon-main" src={financeIcon} alt="" />
                  <img className="analysis-icon-badge" src={ecoIcon} alt="" />
                </span>
                <h2>Análisis recibido</h2>
                {headline ? (
                  <p>
                    Perfil <strong>{headline.profile}</strong>
                    {headline.score !== null ? ` · Score ${headline.score}` : ''}
                  </p>
                ) : (
                  <p>El backend respondió correctamente.</p>
                )}
              </div>
            ) : (
              <div className="analysis-empty">
                <span className="analysis-icon" aria-hidden="true">
                  <img className="analysis-icon-main" src={financeIcon} alt="" />
                  <img className="analysis-icon-badge" src={ecoIcon} alt="" />
                </span>
                <h2>Aún no tenemos tu análisis.</h2>
                <p>Completá tus datos y presioná “Analizar mis finanzas” para descubrir tu salud financiera.</p>
              </div>
            )}

            {analysis.status === SUBMIT_STATUS.ERROR ? (
              <div className="error-box" role="alert" aria-live="polite">
                <strong>Revisá estos puntos antes de analizar.</strong>
                <ul>
                  {analysis.errors.map((error) => (
                    <li key={error}>{error}</li>
                  ))}
                </ul>
              </div>
            ) : null}

            <div className="json-panel">
              <div className="json-panel-header">
                <strong>Request JSON demo</strong>
                <span>POST {API_BASE_URL}/analisis-financiero</span>
              </div>
              <pre tabIndex={0}>{requestPreview}</pre>
            </div>

            {analysis.status === SUBMIT_STATUS.SUCCESS ? (
              <div className="json-panel response-panel">
                <div className="json-panel-header">
                  <strong>Respuesta del backend</strong>
                </div>
                <pre tabIndex={0}>{JSON.stringify(analysis.response, null, 2)}</pre>
              </div>
            ) : null}
          </article>
        </aside>
      </main>

      <footer className="footer">
        <span aria-hidden="true">
          <img className="footer-eco footer-eco-left" src={ecoIcon} alt="" />
          <img className="footer-eco footer-eco-center" src={ecoIcon} alt="" />
          <img className="footer-eco footer-eco-right" src={ecoIcon} alt="" />
        </span>
        <p>
          Pequeñas decisiones hoy, <strong>grandes cambios mañana.</strong>
        </p>
        <small>© 2026 MoneyBuddy</small>
      </footer>
    </div>
  )
}
