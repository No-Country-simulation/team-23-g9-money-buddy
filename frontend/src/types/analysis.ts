import type { PaymentPatternKey } from './financial'

export interface AnalysisMetrics {
  ingreso_mensual: number | null
  credito_total: number | null
  pago_mensual_deudas: number | null
  gasto_total: number | null
  deuda_total: number | null
  nivel_endeudamiento: number | null
  ratio_pago_deudas: number | null
  ratio_deuda_ingreso: number | null
}

export interface ClassifiedTransaction {
  tipo: string | null
  tipo_pago: string | null
  monto: number | null
  categoria: string | null
}

export interface CategoryDistributionItem {
  key: string
  label: string
  amount: number
  percent: number
  color: string
}

export interface PaymentPattern {
  key: PaymentPatternKey
  label: string
  amount: number
  percent: number
  className: string
}

export interface ParsedAnalysisResult {
  profile: string | null
  score: number | null
  recommendations: string[]
  metrics: AnalysisMetrics
  expenseSummary: Record<string, number>
  categoryPercentages: Record<string, number>
  classifiedTransactions: ClassifiedTransaction[]
}

export interface ProfileVisuals {
  color: string
  icon: string
}

export interface AnalysisViewModel {
  monthlyExpensePercent: number
  debtPercent: number
  debtIncomePercent: number
  debtPaymentPercent: number
  monthsToPayDebt: number | null
  monthsVisualPercent: number
  scorePercent: number
  profileVisuals: ProfileVisuals
  categoryDistribution: CategoryDistributionItem[]
  donutBackground: string
  averageCategoryExpense: number
  highestExpenseCategoryLabel: string
  highestExpenseCategoryIcon: string
  paymentPatterns: PaymentPattern[]
}
