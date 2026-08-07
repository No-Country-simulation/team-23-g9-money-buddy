import accountBalanceIcon from '../assets/icon-bank.svg'
import attachMoneyIcon from '../assets/icon-income.svg'
import creditCardIcon from '../assets/icon-credit-card.svg'
import savingsIcon from '../assets/icon-piggybank.svg'
import { SAVING_FREQUENCY } from '../constants/financial'
import type { FinancialFormState } from '../types/financial'

interface FinancialFormCardProps {
  financialForm: FinancialFormState
  onFinancialFieldChange: <Key extends keyof FinancialFormState>(key: Key, value: FinancialFormState[Key]) => void
}

export function FinancialFormCard({ financialForm, onFinancialFieldChange }: FinancialFormCardProps) {
  return (
    <article className="card finance-card">
      <div className="section-heading">
        <h1>Cuéntanos sobre tus finanzas</h1>
        <p>Completá los campos para preparar tu análisis.</p>
      </div>

      <div className="field-grid">
        <label className="field">
          <span className="field-label">
            <span className="field-icon field-icon-income" aria-hidden="true">
              <img src={attachMoneyIcon} alt="" />
            </span>
            Ingreso mensual
          </span>
          <input
            type="number"
            min="1"
            step="0.01"
            inputMode="decimal"
            value={financialForm.ingreso_mensual}
            onChange={(event) => onFinancialFieldChange('ingreso_mensual', event.currentTarget.value)}
          />
        </label>

        <label className="field">
          <span className="field-label">
            <span className="field-icon field-icon-credit" aria-hidden="true">
              <img src={creditCardIcon} alt="" />
            </span>
            Crédito total
          </span>
          <input
            type="number"
            min="0"
            step="0.01"
            inputMode="decimal"
            value={financialForm.credito_total}
            onChange={(event) => onFinancialFieldChange('credito_total', event.currentTarget.value)}
          />
          <small>Límite o monto total de tus tarjetas/líneas de crédito.</small>
        </label>

        <label className="field">
          <span className="field-label">
            <span className="field-icon field-icon-debt" aria-hidden="true">
              <img src={accountBalanceIcon} alt="" />
            </span>
            Pago mensual de deudas
          </span>
          <input
            type="number"
            min="0"
            step="0.01"
            inputMode="decimal"
            value={financialForm.pago_mensual_deudas}
            onChange={(event) => onFinancialFieldChange('pago_mensual_deudas', event.currentTarget.value)}
          />
        </label>

        <fieldset className="frequency-group">
          <legend>
            <span className="field-label">
              <span className="field-icon field-icon-savings" aria-hidden="true">
                <img src={savingsIcon} alt="" />
              </span>
              Frecuencia de ahorro
            </span>
          </legend>
          {Object.values(SAVING_FREQUENCY).map((frequency) => (
            <label key={frequency}>
              <input
                type="radio"
                name="frecuencia_ahorro"
                value={frequency}
                checked={financialForm.frecuencia_ahorro === frequency}
                onChange={() => onFinancialFieldChange('frecuencia_ahorro', frequency)}
              />
              <span>{frequency}</span>
            </label>
          ))}
        </fieldset>
      </div>
    </article>
  )
}
