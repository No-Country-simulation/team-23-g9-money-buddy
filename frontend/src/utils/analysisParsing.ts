import type { ClassifiedTransaction, ParsedAnalysisResult } from '../types/analysis'
import { isRecord } from './typeGuards'

function getNumberField(record: Record<string, unknown>, key: string) {
  return typeof record[key] === 'number' ? record[key] : null
}

function getAnalysisData(response: unknown) {
  if (!isRecord(response) || !isRecord(response.data)) {
    return null
  }

  return response.data
}

function getRecommendations(data: Record<string, unknown>) {
  if (!Array.isArray(data.recomendaciones)) {
    return []
  }

  return data.recomendaciones.filter((recommendation): recommendation is string => typeof recommendation === 'string')
}

function getNumericRecord(value: unknown) {
  if (!isRecord(value)) {
    return {}
  }

  return Object.entries(value).reduce<Record<string, number>>((record, [key, entry]) => {
    if (typeof entry === 'number' && Number.isFinite(entry)) {
      record[key] = entry
    }

    return record
  }, {})
}

function getClassifiedTransactions(value: unknown) {
  if (!Array.isArray(value)) {
    return []
  }

  return value.flatMap<ClassifiedTransaction>((transaction) => {
    if (!isRecord(transaction)) {
      return []
    }

    return [{
      tipo: typeof transaction.tipo === 'string' ? transaction.tipo : null,
      tipo_pago: typeof transaction.tipo_pago === 'string' ? transaction.tipo_pago : null,
      monto: getNumberField(transaction, 'monto'),
      categoria: typeof transaction.categoria === 'string' ? transaction.categoria : null,
    }]
  })
}

export function parseAnalysisResult(response: unknown): ParsedAnalysisResult | null {
  const data = getAnalysisData(response)

  if (!data) {
    return null
  }

  const indicators = isRecord(data.indicadores) ? data.indicadores : {}
  const profile = typeof data.perfil_financiero === 'string' ? data.perfil_financiero : null
  const score = typeof data.score_financiero === 'number' ? data.score_financiero : null

  return {
    profile,
    score,
    recommendations: getRecommendations(data),
    expenseSummary: getNumericRecord(data.resumen_gastos),
    categoryPercentages: getNumericRecord(indicators.porcentaje_categorias),
    classifiedTransactions: getClassifiedTransactions(data.transacciones_clasificadas),
    metrics: {
      ingreso_mensual: getNumberField(indicators, 'ingreso_mensual'),
      credito_total: getNumberField(indicators, 'credito_total'),
      pago_mensual_deudas: getNumberField(indicators, 'pago_mensual_deudas'),
      gasto_total: getNumberField(indicators, 'gasto_total'),
      deuda_total: getNumberField(indicators, 'deuda_total'),
      nivel_endeudamiento: getNumberField(indicators, 'nivel_endeudamiento'),
      ratio_pago_deudas: getNumberField(indicators, 'ratio_pago_deudas'),
      ratio_deuda_ingreso: getNumberField(indicators, 'ratio_deuda_ingreso'),
    },
  }
}
