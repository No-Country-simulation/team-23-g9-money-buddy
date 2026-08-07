import accountBalanceIcon from '../assets/icon-bank.svg'
import arrowRightIcon from '../assets/icon-arrow-right.svg'
import checkIcon from '../assets/icon-check.svg'
import ecoIcon from '../assets/icon-leaf.svg'
import financeIcon from '../assets/icon-bar-chart.svg'
import receiptIcon from '../assets/icon-receipt.svg'
import { SUBMIT_STATUS } from '../constants/financial'
import type { AnalysisViewModel, ParsedAnalysisResult } from '../types/analysis'
import type { AnalysisState } from '../types/financial'
import { formatApproxPercent, toMoney, toPercent } from '../utils/formatters'

interface ResultDashboardProps {
  analysis: AnalysisState
  parsedResult: ParsedAnalysisResult | null
  viewModel: AnalysisViewModel | null
  onEditAnalysis: () => void
  onNewAnalysis: () => void
}

export function ResultDashboard({ analysis, parsedResult, viewModel, onEditAnalysis, onNewAnalysis }: ResultDashboardProps) {
  const metrics = parsedResult?.metrics

  return (
    <section className="result-step" aria-label="Resultado del análisis financiero">
      <article className="card result-card">
        <div className="result-hero">
          <span className="analysis-icon" aria-hidden="true">
            <img className="analysis-icon-main" src={financeIcon} alt="" />
            <img className="analysis-icon-badge" src={ecoIcon} alt="" />
          </span>
          <div>
            <h1>Tu resultado financiero</h1>
          </div>

          <div className="result-actions">
            <button className="secondary-button" type="button" onClick={onEditAnalysis}>
              Volver y editar datos
            </button>
            <button className="ghost-button" type="button" onClick={onNewAnalysis}>
              Iniciar nuevo análisis
            </button>
          </div>
        </div>

        {parsedResult && viewModel ? (
          <div className="result-dashboard">
            <div className="result-column result-column-left">
              <section className="result-panel" aria-labelledby="monthly-summary-title">
                <div className="result-panel-heading">
                  <h2 id="monthly-summary-title">Resumen del mes</h2>
                </div>

                <div className="summary-kpis">
                  <article className="summary-kpi">
                    <div className="summary-kpi-top">
                      <span className="summary-kpi-icon summary-kpi-icon-green" aria-hidden="true">
                        <img src={receiptIcon} alt="" />
                      </span>
                      <span>Gasto total del mes</span>
                    </div>
                    <strong>{metrics?.gasto_total !== null ? toMoney(metrics?.gasto_total ?? 0) : 'No informado'}</strong>
                    <div className="progress-track" aria-hidden="true">
                      <span style={{ width: `${viewModel.monthlyExpensePercent}%` }} />
                    </div>
                    <small>{formatApproxPercent(viewModel.monthlyExpensePercent)} del ingreso mensual</small>
                  </article>

                  <article className="summary-kpi">
                    <div className="summary-kpi-top">
                      <span className="summary-kpi-icon summary-kpi-icon-amber" aria-hidden="true">
                        <img src={accountBalanceIcon} alt="" />
                      </span>
                      <span>Total de deuda</span>
                    </div>
                    <strong>{metrics?.deuda_total !== null ? toMoney(metrics?.deuda_total ?? 0) : 'No informado'}</strong>
                    <div className="progress-track progress-track-amber" aria-hidden="true">
                      <span style={{ width: `${viewModel.debtPercent}%` }} />
                    </div>
                    <small>{formatApproxPercent(viewModel.debtPercent)} del crédito total</small>
                  </article>
                </div>
              </section>

              <section className="result-panel" aria-labelledby="key-indicators-title">
                <div className="result-panel-heading">
                  <h2 id="key-indicators-title">Indicadores clave</h2>
                </div>

                <div className="indicator-grid">
                  <article className="indicator-card">
                    <span>Ratio deuda/ingreso</span>
                    <strong>{formatApproxPercent(viewModel.debtIncomePercent)}</strong>
                    <div className="progress-track" aria-hidden="true">
                      <span style={{ width: `${viewModel.debtIncomePercent}%` }} />
                    </div>
                  </article>

                  <article className="indicator-card">
                    <span>Pago de deudas</span>
                    <strong>{formatApproxPercent(viewModel.debtPaymentPercent)}</strong>
                    <div className="progress-track" aria-hidden="true">
                      <span style={{ width: `${viewModel.debtPaymentPercent}%` }} />
                    </div>
                  </article>

                  <article className="indicator-card">
                    <span>Meses para liquidar</span>
                    <strong>{viewModel.monthsToPayDebt === null ? '--' : viewModel.monthsToPayDebt}</strong>
                    <div className="progress-track progress-track-blue" aria-hidden="true">
                      <span style={{ width: `${viewModel.monthsVisualPercent}%` }} />
                    </div>
                    <small>Con pago mensual actual. Escala visual: 24 meses = 100%.</small>
                  </article>
                </div>
              </section>

              <section className="result-panel expense-distribution-panel" aria-labelledby="expense-distribution-title">
                <div className="result-panel-heading">
                  <h2 id="expense-distribution-title">Distribución de gastos</h2>
                </div>

                <div className="expense-distribution-layout">
                  <div className="expense-donut-card">
                    <div className="expense-donut-header">
                      <span>Categorías</span>
                    </div>
                    <div
                      className="expense-donut"
                      style={{ background: viewModel.donutBackground }}
                      role="img"
                      aria-label={`Distribución de gastos por categoría. Total: ${toMoney(metrics?.gasto_total ?? 0)}.`}
                    >
                      <div>
                        <small>Gasto</small>
                        <strong>{toMoney(metrics?.gasto_total ?? 0)}</strong>
                      </div>
                    </div>

                    {viewModel.categoryDistribution.length > 0 ? (
                      <ul className="expense-legend" aria-label="Categorías de gasto">
                        {viewModel.categoryDistribution.map((category) => (
                          <li key={category.key}>
                            <span className="legend-dot" style={{ background: category.color }} aria-hidden="true" />
                            <span>{category.label} {toPercent(Math.round(category.percent))}%</span>
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <p className="empty-list">No hay categorías de gasto para mostrar.</p>
                    )}
                  </div>

                  <div className="expense-highlights">
                    <article>
                      <span className="expense-highlight-icon" aria-hidden="true">
                        <img src={financeIcon} alt="" />
                      </span>
                      <div>
                        <span>Gasto promedio por categoría</span>
                        <strong>{toMoney(viewModel.averageCategoryExpense)}</strong>
                      </div>
                    </article>
                    <article>
                      <span className="expense-highlight-icon expense-highlight-icon-category" aria-hidden="true">
                        <img src={viewModel.highestExpenseCategoryIcon} alt="" />
                      </span>
                      <div>
                        <span>Tu mayor gasto son:</span>
                        <strong>{viewModel.highestExpenseCategoryLabel}</strong>
                      </div>
                    </article>
                  </div>
                </div>
              </section>
            </div>

            <aside className="result-column result-column-right" aria-label="Perfil y recomendaciones">
              <section className="profile-panel" aria-labelledby="profile-title">
                <h2 id="profile-title">Tu perfil financiero</h2>
                <div className="score-ring" style={{ background: `conic-gradient(${viewModel.profileVisuals.color} ${viewModel.scorePercent}%, #e5edf3 0)` }}>
                  <div>
                    <small>Puntaje financiero</small>
                    <span>{parsedResult.score !== null ? parsedResult.score : '--'}</span>
                    <small>/100</small>
                  </div>
                </div>
                <span className="profile-badge">
                  <img src={viewModel.profileVisuals.icon} alt="" aria-hidden="true" />
                  {parsedResult.profile ?? 'No informado'}
                </span>

                <div className="recommendation-panel" aria-labelledby="recommendations-title">
                  <h3 id="recommendations-title">Recomendaciones para ti</h3>
                  {parsedResult.recommendations.length > 0 ? (
                    <ul>
                      {parsedResult.recommendations.map((recommendation, index) => (
                        <li key={recommendation}>
                          <span aria-hidden="true">
                            <img src={index === 0 ? checkIcon : arrowRightIcon} alt="" />
                          </span>
                          <p>{recommendation}</p>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="empty-list">El backend no envió recomendaciones para este análisis.</p>
                  )}
                </div>
              </section>

              <section className="result-panel consumption-patterns-panel" aria-labelledby="consumption-patterns-title">
                <div className="result-panel-heading">
                  <h2 id="consumption-patterns-title">Patrones de consumo</h2>
                </div>

                <div className="payment-pattern-grid">
                  {viewModel.paymentPatterns.map((pattern) => (
                    <article className={`payment-pattern ${pattern.className}`} key={pattern.key}>
                      <div className="payment-bar" aria-hidden="true">
                        <span style={{ height: `${pattern.percent}%` }} />
                      </div>
                      <span>{pattern.label}</span>
                      <strong>{toMoney(pattern.amount)}</strong>
                      <small>{formatApproxPercent(pattern.percent)} del gasto total</small>
                    </article>
                  ))}
                </div>
              </section>
            </aside>
          </div>
        ) : (
          <div className="empty-list">No se encontró el bloque `data` esperado en la respuesta.</div>
        )}

        {analysis.status === SUBMIT_STATUS.SUCCESS ? (
          <details className="technical-details">
            <summary>Ver JSON técnico</summary>
            <div className="json-panel response-panel">
              <div className="json-panel-header">
                <strong>Respuesta del backend</strong>
              </div>
              <pre tabIndex={0}>{JSON.stringify(analysis.response, null, 2)}</pre>
            </div>
          </details>
        ) : null}
      </article>
    </section>
  )
}
