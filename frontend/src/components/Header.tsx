import headerNoteIcon from '../assets/icon-lightbulb.svg'

export function Header() {
  return (
    <header className="hero-header">
      <a className="brand" href="#top" aria-label="Ir al inicio de Money Buddy">
        <span className="brand-mark" aria-hidden="true">
          <img src="/logo-moneybuddy.png" alt="" />
        </span>
        <span>
          <small>Tu mejor aliado</small>
          <strong>
            <span className="brand-word-money">Money</span>{' '}
            <span className="brand-word-buddy">Buddy</span>
          </strong>
        </span>
      </a>
      <p className="header-note">
        <img src={headerNoteIcon} alt="" aria-hidden="true" />
        <span>
          Pequeñas decisiones hoy, <strong>grandes cambios mañana.</strong>
        </span>
      </p>
    </header>
  )
}
