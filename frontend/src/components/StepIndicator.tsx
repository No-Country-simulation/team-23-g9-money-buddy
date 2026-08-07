import { APP_STEP } from '../constants/financial'
import type { AppStep } from '../types/financial'

interface StepIndicatorProps {
  currentStep: AppStep
}

export function StepIndicator({ currentStep }: StepIndicatorProps) {
  return (
    <nav className="step-indicator" aria-label="Progreso del análisis">
      <span className={currentStep === APP_STEP.FORM ? 'is-active' : ''}>1. Datos financieros</span>
      <span className={currentStep === APP_STEP.RESULT ? 'is-active' : ''}>2. Resultado</span>
    </nav>
  )
}
