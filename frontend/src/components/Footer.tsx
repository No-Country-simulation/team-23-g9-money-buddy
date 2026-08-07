import ecoIcon from '../assets/icon-leaf.svg'

export function Footer() {
  return (
    <footer className="footer">
      <span aria-hidden="true">
        <img className="footer-eco footer-eco-left" src={ecoIcon} alt="" />
        <img className="footer-eco footer-eco-center" src={ecoIcon} alt="" />
        <img className="footer-eco footer-eco-right" src={ecoIcon} alt="" />
      </span>
      <p>
        Pequeñas decisiones hoy, <strong>grandes cambios mañana.</strong>
      </p>
      <small>© 2026 MoneyBuddy</small>
    </footer>
  )
}
