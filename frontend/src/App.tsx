import { useState, type FormEvent } from 'react'
import { AnalysisStatusCard } from './components/AnalysisStatusCard'
import { FinancialFormCard } from './components/FinancialFormCard'
import { Footer } from './components/Footer'
import { Header } from './components/Header'
import { MovementsCard } from './components/MovementsCard'
import { ResultDashboard } from './components/ResultDashboard'
import { StepIndicator } from './components/StepIndicator'
import { APP_STEP, initialAnalysisState, initialFinancialForm, initialMovementForm, initialMovements, MAX_MOVEMENTS, SUBMIT_STATUS } from './constants/financial'
import type { AppStep, FinancialFormState, Movement, MovementFormState } from './types/financial'
import { buildAnalysisViewModel } from './utils/analysisMetrics'
import { parseAnalysisResult } from './utils/analysisParsing'
import { API_BASE_URL, buildRequest, getBackendErrorMessages, validateRequest } from './utils/request'

export default function App() {
  const [currentStep, setCurrentStep] = useState<AppStep>(APP_STEP.FORM)
  const [financialForm, setFinancialForm] = useState<FinancialFormState>(initialFinancialForm)
  const [movementForm, setMovementForm] = useState<MovementFormState>(initialMovementForm)
  const [movements, setMovements] = useState<Movement[]>(initialMovements)
  const [editingMovementId, setEditingMovementId] = useState<string | null>(null)
  const [analysis, setAnalysis] = useState(initialAnalysisState)

  const requestBody = buildRequest(financialForm, movements)
  const requestPreview = JSON.stringify(requestBody, null, 2)
  const parsedResult = parseAnalysisResult(analysis.response)
  const resultViewModel = parsedResult ? buildAnalysisViewModel(parsedResult) : null
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
      setCurrentStep(APP_STEP.FORM)
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
        setCurrentStep(APP_STEP.FORM)
        return
      }

      setAnalysis({ status: SUBMIT_STATUS.SUCCESS, response: payload, errors: [] })
      setCurrentStep(APP_STEP.RESULT)
    } catch (error) {
      const message = error instanceof Error ? error.message : 'No se pudo completar la solicitud.'
      setAnalysis({
        status: SUBMIT_STATUS.ERROR,
        response: null,
        errors: [`${message} Revisá si el backend está levantado en ${API_BASE_URL}.`],
      })
      setCurrentStep(APP_STEP.FORM)
    }
  }

  function handleEditAnalysis() {
    setCurrentStep(APP_STEP.FORM)
  }

  function handleNewAnalysis() {
    setFinancialForm(initialFinancialForm)
    setMovementForm(initialMovementForm)
    setMovements(initialMovements)
    setEditingMovementId(null)
    setAnalysis(initialAnalysisState)
    setCurrentStep(APP_STEP.FORM)
  }

  return (
    <div className="app-shell">
      <Header />

      <main id="top" className="flow" aria-label="Flujo de análisis financiero">
        <StepIndicator currentStep={currentStep} />

        {currentStep === APP_STEP.FORM ? (
          <section className="form-step" aria-label="Datos para analizar">
            <div className="left-column">
              <FinancialFormCard
                financialForm={financialForm}
                onFinancialFieldChange={updateFinancialField}
              />

              <MovementsCard
                movements={movements}
                movementForm={movementForm}
                editingMovementId={editingMovementId}
                reachedMovementLimit={reachedMovementLimit}
                onMovementFieldChange={updateMovementField}
                onMovementSubmit={handleMovementSubmit}
                onResetMovementForm={resetMovementForm}
                onEditMovement={handleEditMovement}
                onRemoveMovement={handleRemoveMovement}
              />
            </div>

            <aside className="right-column" aria-label="Estado de la solicitud">
              <AnalysisStatusCard
                analysis={analysis}
                isSubmitting={isSubmitting}
                requestPreview={requestPreview}
                onSubmitAnalysis={handleSubmitAnalysis}
              />
            </aside>
          </section>
        ) : (
          <ResultDashboard
            analysis={analysis}
            parsedResult={parsedResult}
            viewModel={resultViewModel}
            onEditAnalysis={handleEditAnalysis}
            onNewAnalysis={handleNewAnalysis}
          />
        )}
      </main>

      <Footer />
    </div>
  )
}
