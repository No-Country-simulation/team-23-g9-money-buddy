import type { AnalysisState, FinancialFormState, Movement, MovementFormState } from '../types/financial'

export const TRANSACTION_TYPE = {
  INCOME: 'Ingreso',
  EXPENSE: 'Egreso',
} as const

export const PAYMENT_TYPE = {
  CASH: 'Efectivo',
  DEBIT: 'Debito',
  CREDIT: 'Credito',
} as const

export const SAVING_FREQUENCY = {
  NONE: 'NULA',
  LOW: 'BAJA',
  MEDIUM: 'MEDIA',
  HIGH: 'ALTA',
} as const

export const SUBMIT_STATUS = {
  IDLE: 'idle',
  LOADING: 'loading',
  SUCCESS: 'success',
  ERROR: 'error',
} as const

export const APP_STEP = {
  FORM: 'form',
  RESULT: 'result',
} as const

export const PAYMENT_PATTERN_KEYS = {
  CASH: 'efectivo',
  DEBIT: 'debito',
  CREDIT: 'credito',
} as const

export const PAYMENT_PATTERN_LABELS = {
  [PAYMENT_PATTERN_KEYS.CASH]: 'Efectivo',
  [PAYMENT_PATTERN_KEYS.DEBIT]: 'Débito',
  [PAYMENT_PATTERN_KEYS.CREDIT]: 'Crédito',
} as const

export const MAX_MOVEMENTS = 100
export const MAX_DESCRIPTION_LENGTH = 200

export const initialFinancialForm: FinancialFormState = {
  credito_total: '1500',
  ingreso_mensual: '1000',
  frecuencia_ahorro: SAVING_FREQUENCY.MEDIUM,
  pago_mensual_deudas: '150',
}

export const initialMovementForm: MovementFormState = {
  tipo: TRANSACTION_TYPE.EXPENSE,
  fecha: new Date().toISOString().slice(0, 10),
  descripcion: '',
  tipo_pago: PAYMENT_TYPE.DEBIT,
  meses_a_deber: '',
  monto: '',
}

export const initialMovements: Movement[] = [
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

export const initialAnalysisState: AnalysisState = {
  status: SUBMIT_STATUS.IDLE,
  response: null,
  errors: [],
}
