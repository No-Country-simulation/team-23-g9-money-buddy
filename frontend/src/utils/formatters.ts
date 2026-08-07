export function toMoney(value: string | number) {
  const amount = typeof value === 'number' ? value : Number(value)
  return new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 2,
  }).format(Number.isFinite(amount) ? amount : 0)
}

export function toNumber(value: string) {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}

export function toPercent(value: number) {
  return new Intl.NumberFormat('es-AR', {
    maximumFractionDigits: 2,
  }).format(value)
}

export function formatApproxPercent(value: number) {
  return `~${toPercent(Math.round(value))}%`
}
