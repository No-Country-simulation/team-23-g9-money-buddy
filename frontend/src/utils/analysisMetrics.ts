import { getCategoryColor, getCategoryIcon, getCategoryLabel } from '../constants/categories'
import { PAYMENT_PATTERN_KEYS, PAYMENT_PATTERN_LABELS, TRANSACTION_TYPE } from '../constants/financial'
import { PROFILE_STATE, PROFILE_VISUALS } from '../constants/profile'
import type { AnalysisViewModel, CategoryDistributionItem, ClassifiedTransaction, ParsedAnalysisResult, PaymentPattern } from '../types/analysis'

export function divideOrNull(numerator: number | null, denominator: number | null) {
  if (numerator === null || denominator === null || denominator <= 0) {
    return null
  }

  return numerator / denominator
}

export function clampPercent(value: number | null) {
  if (value === null || !Number.isFinite(value)) {
    return 0
  }

  return Math.min(100, Math.max(0, value))
}

function getRatioPercent(primaryRatio: number | null, fallbackRatio: number | null) {
  const ratio = primaryRatio ?? fallbackRatio

  return clampPercent(ratio === null ? null : ratio * 100)
}

export function getPercentFromAmount(amount: number | null, base: number | null) {
  const ratio = divideOrNull(amount, base)
  return clampPercent(ratio === null ? null : ratio * 100)
}

function getMonthsToPayDebt(debt: number | null, monthlyPayment: number | null) {
  if (debt === null || monthlyPayment === null || debt <= 0 || monthlyPayment <= 0) {
    return null
  }

  return Math.ceil(debt / monthlyPayment)
}

function normalizeProfileState(profile: string | null) {
  return profile?.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase() ?? null
}

function getProfileVisuals(profile: string | null, score: number | null) {
  const normalizedProfile = normalizeProfileState(profile)

  if (normalizedProfile === PROFILE_STATE.HIGH_RISK) {
    return PROFILE_VISUALS[PROFILE_STATE.HIGH_RISK]
  }

  if (normalizedProfile === PROFILE_STATE.NEEDS_ATTENTION) {
    return PROFILE_VISUALS[PROFILE_STATE.NEEDS_ATTENTION]
  }

  if (normalizedProfile === PROFILE_STATE.STABLE) {
    return PROFILE_VISUALS[PROFILE_STATE.STABLE]
  }

  if (score !== null && score < 50) {
    return PROFILE_VISUALS[PROFILE_STATE.HIGH_RISK]
  }

  if (score !== null && score < 70) {
    return PROFILE_VISUALS[PROFILE_STATE.NEEDS_ATTENTION]
  }

  return PROFILE_VISUALS[PROFILE_STATE.STABLE]
}

function getCategoryDistribution(summary: Record<string, number>, percentages: Record<string, number>, totalExpense: number | null) {
  const total = totalExpense && totalExpense > 0
    ? totalExpense
    : Object.values(summary).reduce((sum, amount) => sum + Math.max(0, amount), 0)

  const positiveCategories = Object.entries(summary)
    .filter(([, amount]) => amount > 0)
    .sort(([, firstAmount], [, secondAmount]) => secondAmount - firstAmount)

  const categories = positiveCategories.map(([key, amount]) => ({
    key,
    label: getCategoryLabel(key),
    amount,
    percent: clampPercent(percentages[key] ?? (total > 0 ? (amount / total) * 100 : 0)),
  }))

  const nonOtherCategories = categories.filter((category) => category.key !== 'otros')
  const otherCategory = categories.find((category) => category.key === 'otros')
  const groupedCategories = categories.length > 5
    ? nonOtherCategories.slice(0, 4)
    : nonOtherCategories
  const otherCategories = categories.length > 5
    ? [...nonOtherCategories.slice(4), ...(otherCategory ? [otherCategory] : [])]
    : otherCategory ? [otherCategory] : []
  const groupedOther = otherCategories.length > 0
    ? {
      key: 'otros',
      label: 'Otros',
      amount: otherCategories.reduce((sum, category) => sum + category.amount, 0),
      percent: otherCategories.reduce((sum, category) => sum + category.percent, 0),
    }
    : null
  const visibleCategories = groupedOther ? [...groupedCategories, groupedOther] : groupedCategories

  return visibleCategories.map<CategoryDistributionItem>((category) => ({
    ...category,
    percent: clampPercent(category.percent),
    color: getCategoryColor(category.key),
  }))
}

function getDonutBackground(categories: CategoryDistributionItem[]) {
  if (categories.length === 0) {
    return 'conic-gradient(#e5edf3 0 100%)'
  }

  let accumulated = 0
  const segments = categories.map((category) => {
    const start = accumulated
    const end = Math.min(100, accumulated + category.percent)
    accumulated = end

    return `${category.color} ${start}% ${end}%`
  })

  if (accumulated < 100) {
    segments.push(`#e5edf3 ${accumulated}% 100%`)
  }

  return `conic-gradient(${segments.join(', ')})`
}

function normalizePaymentPatternKey(value: string | null) {
  const normalized = value?.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()

  if (normalized === PAYMENT_PATTERN_KEYS.CASH || normalized === PAYMENT_PATTERN_KEYS.DEBIT || normalized === PAYMENT_PATTERN_KEYS.CREDIT) {
    return normalized
  }

  return null
}

function getPaymentPatterns(transactions: ClassifiedTransaction[], totalExpense: number | null) {
  const totals: Record<PaymentPattern['key'], number> = {
    [PAYMENT_PATTERN_KEYS.CASH]: 0,
    [PAYMENT_PATTERN_KEYS.DEBIT]: 0,
    [PAYMENT_PATTERN_KEYS.CREDIT]: 0,
  }

  transactions.forEach((transaction) => {
    const paymentKey = normalizePaymentPatternKey(transaction.tipo_pago)

    if (transaction.tipo === TRANSACTION_TYPE.EXPENSE && paymentKey && transaction.monto !== null) {
      totals[paymentKey] += Math.max(0, transaction.monto)
    }
  })

  const baseExpense = totalExpense && totalExpense > 0
    ? totalExpense
    : Object.values(totals).reduce((sum, amount) => sum + amount, 0)

  return [
    { key: PAYMENT_PATTERN_KEYS.CASH, label: PAYMENT_PATTERN_LABELS[PAYMENT_PATTERN_KEYS.CASH], amount: totals.efectivo, percent: getPercentFromAmount(totals.efectivo, baseExpense), className: 'payment-pattern-cash' },
    { key: PAYMENT_PATTERN_KEYS.DEBIT, label: PAYMENT_PATTERN_LABELS[PAYMENT_PATTERN_KEYS.DEBIT], amount: totals.debito, percent: getPercentFromAmount(totals.debito, baseExpense), className: 'payment-pattern-debit' },
    { key: PAYMENT_PATTERN_KEYS.CREDIT, label: PAYMENT_PATTERN_LABELS[PAYMENT_PATTERN_KEYS.CREDIT], amount: totals.credito, percent: getPercentFromAmount(totals.credito, baseExpense), className: 'payment-pattern-credit' },
  ] satisfies PaymentPattern[]
}

export function buildAnalysisViewModel(parsedResult: ParsedAnalysisResult): AnalysisViewModel {
  const metrics = parsedResult.metrics
  const monthlyExpensePercent = getPercentFromAmount(metrics.gasto_total, metrics.ingreso_mensual)
  const debtPercent = clampPercent(metrics.nivel_endeudamiento ?? getPercentFromAmount(metrics.deuda_total, metrics.credito_total))
  const debtIncomePercent = getRatioPercent(metrics.ratio_deuda_ingreso, divideOrNull(metrics.deuda_total, metrics.ingreso_mensual))
  const debtPaymentPercent = getRatioPercent(metrics.ratio_pago_deudas, divideOrNull(metrics.pago_mensual_deudas, metrics.ingreso_mensual))
  const monthsToPayDebt = getMonthsToPayDebt(metrics.deuda_total, metrics.pago_mensual_deudas)
  const categoryDistribution = getCategoryDistribution(parsedResult.expenseSummary, parsedResult.categoryPercentages, metrics.gasto_total)
  const highestExpenseCategory = categoryDistribution[0]

  return {
    monthlyExpensePercent,
    debtPercent,
    debtIncomePercent,
    debtPaymentPercent,
    monthsToPayDebt,
    monthsVisualPercent: clampPercent(monthsToPayDebt === null ? null : (monthsToPayDebt / 24) * 100),
    scorePercent: clampPercent(parsedResult.score),
    profileVisuals: getProfileVisuals(parsedResult.profile, parsedResult.score),
    categoryDistribution,
    donutBackground: getDonutBackground(categoryDistribution),
    averageCategoryExpense: categoryDistribution.length > 0
      ? categoryDistribution.reduce((sum, category) => sum + category.amount, 0) / categoryDistribution.length
      : 0,
    highestExpenseCategoryLabel: highestExpenseCategory?.label ?? 'Sin datos',
    highestExpenseCategoryIcon: getCategoryIcon(highestExpenseCategory?.key ?? 'otros'),
    paymentPatterns: getPaymentPatterns(parsedResult.classifiedTransactions, metrics.gasto_total),
  }
}
