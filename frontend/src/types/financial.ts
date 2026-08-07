import type { APP_STEP, PAYMENT_PATTERN_KEYS, PAYMENT_TYPE, SAVING_FREQUENCY, SUBMIT_STATUS, TRANSACTION_TYPE } from '../constants/financial'

export type TransactionType = (typeof TRANSACTION_TYPE)[keyof typeof TRANSACTION_TYPE]
export type PaymentType = (typeof PAYMENT_TYPE)[keyof typeof PAYMENT_TYPE]
export type SavingFrequency = (typeof SAVING_FREQUENCY)[keyof typeof SAVING_FREQUENCY]
export type SubmitStatus = (typeof SUBMIT_STATUS)[keyof typeof SUBMIT_STATUS]
export type AppStep = (typeof APP_STEP)[keyof typeof APP_STEP]
export type PaymentPatternKey = (typeof PAYMENT_PATTERN_KEYS)[keyof typeof PAYMENT_PATTERN_KEYS]

export interface FinancialFormState {
  credito_total: string
  ingreso_mensual: string
  frecuencia_ahorro: SavingFrequency
  pago_mensual_deudas: string
}

export interface MovementFormState {
  tipo: TransactionType
  fecha: string
  descripcion: string
  tipo_pago: PaymentType
  meses_a_deber: string
  monto: string
}

export interface Movement extends MovementFormState {
  id: string
}

export interface TransactionRequest {
  tipo: TransactionType
  fecha: string
  descripcion: string
  tipo_pago?: PaymentType
  meses_a_deber?: number
  monto: number
}

export interface FinancialAnalysisRequest {
  credito_total: number
  ingreso_mensual: number
  frecuencia_ahorro: SavingFrequency
  pago_mensual_deudas: number
  transacciones: TransactionRequest[]
}

export interface AnalysisState {
  status: SubmitStatus
  response: unknown
  errors: string[]
}
