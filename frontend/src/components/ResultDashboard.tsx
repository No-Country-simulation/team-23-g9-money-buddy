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

function formatTransactionDate(value: string | null) {
  if (!value) {
    return 'No informada'
  }

  const date = new Date(`${value}T00:00:00`)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(date)
}

function formatSavingFrequency(value: string | null) {
  if (!value) {
    return 'No informada'
  }

  return value.toLowerCase().replace(/^./, (letter) => letter.toUpperCase())
}

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
              {parsedResult.message ? (
                <section className="result-panel analysis-message-panel" aria-labelledby="analysis-message-title">
                  <div className="result-panel-heading">
                    <h2 id="analysis-message-title">Resumen del análisis</h2>
                  </div>
                  <p>{parsedResult.message}</p>
                </section>
              ) : null}

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
                    <span>Ingreso mensual</span>
                    <strong>{metrics?.ingreso_mensual !== null ? toMoney(metrics?.ingreso_mensual ?? 0) : 'No informado'}</strong>
                  </article>

                  <article className="indicator-card">
                    <span>Crédito total</span>
                    <strong>{metrics?.credito_total !== null ? toMoney(metrics?.credito_total ?? 0) : 'No informado'}</strong>
                  </article>

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
                  </article>

                  <article className="indicator-card">
                    <span>Frecuencia de ahorro</span>
                    <strong>{formatSavingFrequency(metrics?.frecuencia_ahorro ?? null)}</strong>
                  </article>
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
                          <span>{category.label}</span>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="empty-list">No hay categorías de gasto para mostrar.</p>
                  )}
                </div>

                <div className="expense-detail-column">
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
                        <span>Tu mayor gasto es:</span>
                        <strong>{viewModel.highestExpenseCategoryLabel}</strong>
                      </div>
                    </article>
                  </div>

                  {viewModel.categoryDetails.length > 0 ? (
                    <div className="category-detail-list" aria-label="Detalle de montos por categoría">
                      {viewModel.categoryDetails.map((category) => (
                        <article className="category-detail-item" key={category.key}>
                          <div>
                            <span className="legend-dot" style={{ background: category.color }} aria-hidden="true" />
                            <strong>{category.label}</strong>
                          </div>
                          <span>{toMoney(category.amount)}</span>
                          <small>{toPercent(Math.round(category.percent))}%</small>
                        </article>
                      ))}
                    </div>
                  ) : null}
                </div>
              </div>

              {viewModel.hiddenGroupedCategories.length > 0 ? (
                <p className="grouped-categories-note">
                  {viewModel.hiddenGroupedCategories.length} categorías se agruparon visualmente bajo “Otros”: {viewModel.hiddenGroupedCategories.map((category) => category.label).join(', ')}.
                </p>
              ) : null}
            </section>

            <section className="result-panel classified-transactions-panel" aria-labelledby="classified-transactions-title">
              <div className="result-panel-heading">
                <h2 id="classified-transactions-title">Transacciones clasificadas</h2>
              </div>

              {parsedResult.classifiedTransactions.length > 0 ? (
                <div className="classified-table-wrap">
                  <table className="classified-table">
                    <caption>Detalle de transacciones con fecha, descripción, categoría, tipo de pago, monto y meses a deber.</caption>
                    <thead>
                      <tr>
                        <th scope="col">Fecha</th>
                        <th scope="col">Descripción</th>
                        <th scope="col">Categoría</th>
                        <th scope="col">Tipo de pago</th>
                        <th scope="col">Monto</th>
                        <th scope="col">Meses a deber</th>
                      </tr>
                    </thead>
                    <tbody>
                      {parsedResult.classifiedTransactions.map((transaction, index) => (
                        <tr key={`${transaction.fecha ?? 'sin-fecha'}-${transaction.descripcion ?? 'sin-descripcion'}-${index}`}>
                          <td data-label="Fecha">{formatTransactionDate(transaction.fecha)}</td>
                          <td data-label="Descripción" className="classified-description">{transaction.descripcion ?? 'Sin descripción'}</td>
                          <td data-label="Categoría">{transaction.categoria ?? 'No clasificada'}</td>
                          <td data-label="Tipo de pago">{transaction.tipo_pago ?? 'No informado'}</td>
                          <td data-label="Monto">{transaction.monto === null ? 'No informado' : toMoney(transaction.monto)}</td>
                          <td data-label="Meses a deber">{transaction.meses_a_deber === null ? 'No aplica' : transaction.meses_a_deber}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <p className="empty-list">El backend no envió transacciones clasificadas para este análisis.</p>
              )}
            </section>
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
