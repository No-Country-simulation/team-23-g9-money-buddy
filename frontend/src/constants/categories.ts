import alimentosIcon from '../assets/alimentos.svg'
import educacionIcon from '../assets/educacion.svg'
import ocioEntretenimientoIcon from '../assets/ocio-entretenimiento.svg'
import otrosIcon from '../assets/otros.svg'
import ropaCalzadoIcon from '../assets/ropa-calzado.svg'
import saludIcon from '../assets/salud.svg'
import serviciosIcon from '../assets/servicios.svg'
import tecnologiaIcon from '../assets/tecnologia.svg'
import transporteIcon from '../assets/transporte.svg'
import viviendaIcon from '../assets/vivienda.svg'

export const CATEGORY_LABELS = {
  alimentos: 'Alimentación',
  transporte: 'Transporte',
  salud: 'Salud',
  vivienda: 'Vivienda',
  educacion: 'Educación',
  ocio_entretenimiento: 'Entretenimiento',
  servicios: 'Servicios',
  ropa_calzado: 'Ropa y calzado',
  tecnologia: 'Tecnología',
  otros: 'Otros',
} as const

export const CATEGORY_COLORS = {
  alimentos: '#47ce8b',
  transporte: '#23395e',
  salud: '#ef4444',
  vivienda: '#14b8a6',
  educacion: '#6366f1',
  ocio_entretenimiento: '#3b82f6',
  servicios: '#8b5cf6',
  ropa_calzado: '#ec4899',
  tecnologia: '#0ea5e9',
  otros: '#f59e0b',
} as const

export const CATEGORY_ICONS = {
  alimentos: alimentosIcon,
  transporte: transporteIcon,
  salud: saludIcon,
  vivienda: viviendaIcon,
  educacion: educacionIcon,
  ocio_entretenimiento: ocioEntretenimientoIcon,
  servicios: serviciosIcon,
  ropa_calzado: ropaCalzadoIcon,
  tecnologia: tecnologiaIcon,
  otros: otrosIcon,
} as const

export const FALLBACK_CATEGORY_COLOR = '#94a3b8'

export function getCategoryLabel(key: string) {
  if (key in CATEGORY_LABELS) {
    return CATEGORY_LABELS[key as keyof typeof CATEGORY_LABELS]
  }

  return key
    .split('_')
    .filter(Boolean)
    .map((word) => `${word.charAt(0).toUpperCase()}${word.slice(1)}`)
    .join(' ')
}

export function getCategoryColor(key: string) {
  if (key in CATEGORY_COLORS) {
    return CATEGORY_COLORS[key as keyof typeof CATEGORY_COLORS]
  }

  return FALLBACK_CATEGORY_COLOR
}

export function getCategoryIcon(key: string) {
  if (key in CATEGORY_ICONS) {
    return CATEGORY_ICONS[key as keyof typeof CATEGORY_ICONS]
  }

  return CATEGORY_ICONS.otros
}
