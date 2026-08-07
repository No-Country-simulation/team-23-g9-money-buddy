import { MAX_DESCRIPTION_LENGTH, MAX_MOVEMENTS, PAYMENT_TYPE, TRANSACTION_TYPE } from '../constants/financial'
import type { FinancialAnalysisRequest, FinancialFormState, Movement, PaymentType, TransactionRequest } from '../types/financial'
import { toNumber } from './formatters'
import { isRecord } from './typeGuards'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

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

export function buildRequest(form: FinancialFormState, movements: Movement[]): FinancialAnalysisRequest {
  return {
    credito_total: toNumber(form.credito_total),
    ingreso_mensual: toNumber(form.ingreso_mensual),
    frecuencia_ahorro: form.frecuencia_ahorro,
    pago_mensual_deudas: toNumber(form.pago_mensual_deudas),
    transacciones: movements.map(buildTransaction),
  }
}

export function validateRequest(request: FinancialAnalysisRequest) {
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

export function getBackendErrorMessages(payload: unknown) {
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
