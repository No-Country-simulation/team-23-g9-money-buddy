import ecoIcon from '../assets/icon-leaf.svg'
import financeIcon from '../assets/icon-bar-chart.svg'
import { SUBMIT_STATUS } from '../constants/financial'
import type { AnalysisState } from '../types/financial'
import { API_BASE_URL } from '../utils/request'

interface AnalysisStatusCardProps {
  analysis: AnalysisState
  isSubmitting: boolean
  requestPreview: string
  onSubmitAnalysis: () => void
}

export function AnalysisStatusCard({ analysis, isSubmitting, requestPreview, onSubmitAnalysis }: AnalysisStatusCardProps) {
  return (
    <article className="card analysis-card">
      <div className="analysis-empty">
        <span className="analysis-icon" aria-hidden="true">
          <img className="analysis-icon-main" src={financeIcon} alt="" />
          <img className="analysis-icon-badge" src={ecoIcon} alt="" />
        </span>
        <h2>Aún no tenemos tu análisis.</h2>
        <p>Completá tus datos y presioná “Analizar mis finanzas” para descubrir tu salud financiera.</p>
        <button className="primary-button" type="button" disabled={isSubmitting} onClick={onSubmitAnalysis}>
          {isSubmitting ? 'Analizando…' : 'Analizar mis finanzas'}
        </button>
        <p className="privacy-note">Tus datos están 100% seguros y privados.</p>
      </div>

      {analysis.status === SUBMIT_STATUS.ERROR ? (
        <div className="error-box" role="alert" aria-live="polite">
          <strong>Revisá estos puntos antes de analizar.</strong>
          <ul>
            {analysis.errors.map((error) => (
              <li key={error}>{error}</li>
            ))}
          </ul>
        </div>
      ) : null}

      <details className="technical-details request-details">
        <summary>Ver request JSON demo</summary>
        <div className="json-panel">
          <div className="json-panel-header">
            <strong>Request JSON demo</strong>
            <span>POST {API_BASE_URL}/analisis-financiero</span>
          </div>
          <pre tabIndex={0}>{requestPreview}</pre>
        </div>
      </details>
    </article>
  )
}
