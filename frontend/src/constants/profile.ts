import alertTriangleIcon from '../assets/icon-alert-triangle.svg'
import riesgoAltoIcon from '../assets/riesgo-alto.svg'
import shieldCheckIcon from '../assets/icon-shield-check.svg'

export const PROFILE_STATE = {
  HIGH_RISK: 'riesgo_alto',
  NEEDS_ATTENTION: 'requiere_atencion',
  STABLE: 'estable',
} as const

export const PROFILE_VISUALS = {
  [PROFILE_STATE.HIGH_RISK]: {
    color: '#ef4444',
    icon: riesgoAltoIcon,
  },
  [PROFILE_STATE.NEEDS_ATTENTION]: {
    color: '#f59e0b',
    icon: alertTriangleIcon,
  },
  [PROFILE_STATE.STABLE]: {
    color: '#47ce8b',
    icon: shieldCheckIcon,
  },
} as const
