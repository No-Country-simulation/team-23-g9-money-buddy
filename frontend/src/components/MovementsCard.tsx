import type { FormEvent } from 'react'
import { MAX_DESCRIPTION_LENGTH, MAX_MOVEMENTS, PAYMENT_TYPE, TRANSACTION_TYPE } from '../constants/financial'
import type { Movement, MovementFormState, PaymentType, TransactionType } from '../types/financial'
import { toMoney } from '../utils/formatters'

interface MovementsCardProps {
  movements: Movement[]
  movementForm: MovementFormState
  editingMovementId: string | null
  reachedMovementLimit: boolean
  onMovementFieldChange: <Key extends keyof MovementFormState>(key: Key, value: MovementFormState[Key]) => void
  onMovementSubmit: (event: FormEvent<HTMLFormElement>) => void
  onResetMovementForm: () => void
  onEditMovement: (movement: Movement) => void
  onRemoveMovement: (id: string) => void
}

export function MovementsCard({
  movements,
  movementForm,
  editingMovementId,
  reachedMovementLimit,
  onMovementFieldChange,
  onMovementSubmit,
  onResetMovementForm,
  onEditMovement,
  onRemoveMovement,
}: MovementsCardProps) {
  return (
    <article className="card movements-card">
      <div className="section-heading compact-heading">
        <div>
          <h2>Tus movimientos</h2>
          <p>Agregá ingresos y egresos del mes actual.</p>
        </div>
        <span className="movement-count">{movements.length} movimientos</span>
      </div>

      <form className="movement-form" onSubmit={onMovementSubmit}>
        <div className="movement-form-grid">
          <label className="field">
            <span>Tipo</span>
            <select
              value={movementForm.tipo}
              onChange={(event) => onMovementFieldChange('tipo', event.currentTarget.value as TransactionType)}
            >
              <option value={TRANSACTION_TYPE.EXPENSE}>Egreso</option>
              <option value={TRANSACTION_TYPE.INCOME}>Ingreso</option>
            </select>
          </label>

          <label className="field">
            <span>Fecha</span>
            <input
              type="date"
              required
              value={movementForm.fecha}
              onChange={(event) => onMovementFieldChange('fecha', event.currentTarget.value)}
            />
          </label>

          <label className="field wide-field">
            <span>Descripción</span>
            <input
              type="text"
              required
              maxLength={MAX_DESCRIPTION_LENGTH}
              placeholder="Ej. supermercado, salario, gas"
              value={movementForm.descripcion}
              onChange={(event) => onMovementFieldChange('descripcion', event.currentTarget.value)}
            />
            <small>
              {movementForm.descripcion.length}/{MAX_DESCRIPTION_LENGTH} caracteres
            </small>
          </label>

          {movementForm.tipo === TRANSACTION_TYPE.EXPENSE ? (
            <label className="field">
              <span>Tipo de pago</span>
              <select
                value={movementForm.tipo_pago}
                onChange={(event) => onMovementFieldChange('tipo_pago', event.currentTarget.value as PaymentType)}
              >
                <option value={PAYMENT_TYPE.DEBIT}>Débito</option>
                <option value={PAYMENT_TYPE.CASH}>Efectivo</option>
                <option value={PAYMENT_TYPE.CREDIT}>Crédito</option>
              </select>
            </label>
          ) : null}

          {movementForm.tipo === TRANSACTION_TYPE.EXPENSE && movementForm.tipo_pago === PAYMENT_TYPE.CREDIT ? (
            <label className="field">
              <span>Meses a deber</span>
              <input
                type="number"
                min="1"
                step="1"
                required
                inputMode="numeric"
                value={movementForm.meses_a_deber}
                onChange={(event) => onMovementFieldChange('meses_a_deber', event.currentTarget.value)}
              />
            </label>
          ) : null}

          <label className="field">
            <span>Monto</span>
            <input
              type="number"
              min="0.01"
              step="0.01"
              required
              inputMode="decimal"
              value={movementForm.monto}
              onChange={(event) => onMovementFieldChange('monto', event.currentTarget.value)}
            />
          </label>
        </div>

        <div className="form-actions">
          <button className="secondary-button" type="submit" disabled={reachedMovementLimit}>
            {editingMovementId ? 'Guardar cambios' : '+ Agregar movimiento'}
          </button>
          {reachedMovementLimit ? <p className="form-limit">Llegaste al máximo de {MAX_MOVEMENTS} movimientos.</p> : null}
          {editingMovementId ? (
            <button className="ghost-button" type="button" onClick={onResetMovementForm}>
              Cancelar edición
            </button>
          ) : null}
        </div>
      </form>

      <div className="movement-list" aria-live="polite">
        {movements.length === 0 ? (
          <>
            <img className="movements-mascot" src="/mascot-capybara.png" alt="Mascota de MoneyBuddy" />
            <p className="empty-list">Aún no hay movimientos. ¡Agregá el primero!</p>
          </>
        ) : (
          movements.map((movement) => (
            <article className="movement-item" key={movement.id}>
              <div>
                <strong>{movement.descripcion}</strong>
                <span>
                  {movement.fecha} · {movement.tipo}
                  {movement.tipo === TRANSACTION_TYPE.EXPENSE ? ` · ${movement.tipo_pago}` : ''}
                </span>
              </div>
              <strong className={movement.tipo === TRANSACTION_TYPE.INCOME ? 'positive-amount' : 'negative-amount'}>
                {movement.tipo === TRANSACTION_TYPE.INCOME ? '+' : '-'} {toMoney(movement.monto)}
              </strong>
              <div className="row-actions" aria-label={`Acciones para ${movement.descripcion}`}>
                <button type="button" onClick={() => onEditMovement(movement)}>
                  Editar
                </button>
                <button type="button" onClick={() => onRemoveMovement(movement.id)}>
                  Eliminar
                </button>
              </div>
            </article>
          ))
        )}
      </div>
    </article>
  )
}
