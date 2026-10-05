import { useEffect, useId, useState } from 'react'
import { Link, NavLink, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.js'

export function Brand({ to = '/' }) {
  return (
    <Link className="wordmark" to={to} aria-label="CodeRoute home">
      <span className="wordmark-icon" aria-hidden="true">C</span>
      <span>code<span className="wordmark-light">route</span></span>
    </Link>
  )
}

function NavItems({ user, onNavigate, variant = 'app' }) {
  const linkClass = ({ isActive }) => (isActive ? 'active' : undefined)

  if (variant === 'marketing') {
    return (
      <>
        <a href="#approach" onClick={onNavigate}>The approach</a>
        <a href="#principles" onClick={onNavigate}>Why CodeRoute</a>
        {user && (
          <>
            <Link to="/problems" onClick={onNavigate}>Practice</Link>
            <Link to="/roadmap" onClick={onNavigate}>Roadmap</Link>
            <Link to="/account" onClick={onNavigate}>Account</Link>
            {user.role === 'ADMIN' && <Link to="/admin" onClick={onNavigate}>Admin</Link>}
          </>
        )}
      </>
    )
  }

  return (
    <>
      <NavLink to="/problems" className={linkClass} onClick={onNavigate} end={false}>Problems</NavLink>
      <NavLink to="/roadmap" className={linkClass} onClick={onNavigate}>Roadmap</NavLink>
      <NavLink to="/account" className={linkClass} onClick={onNavigate}>Account</NavLink>
      {user?.role === 'ADMIN' && <NavLink to="/admin" className={linkClass} onClick={onNavigate}>Admin</NavLink>}
    </>
  )
}

export function AppHeader({ variant = 'app', showAuthActions = true }) {
  const { user, logout } = useAuth()
  const location = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)
  const menuId = useId()

  useEffect(() => {
    setMenuOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (!menuOpen) return undefined
    function onKey(event) {
      if (event.key === 'Escape') setMenuOpen(false)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [menuOpen])

  const shellClass = [
    'site-header',
    variant === 'admin' && 'admin-header',
    variant === 'app' && 'app-header',
    variant === 'auth' && 'auth-header',
  ].filter(Boolean).join(' ')

  return (
    <header className={shellClass}>
      <Brand />
      <button
        className={`nav-toggle${menuOpen ? ' is-open' : ''}`}
        type="button"
        aria-expanded={menuOpen}
        aria-controls={menuId}
        onClick={() => setMenuOpen((open) => !open)}
      >
        <span className="sr-only">{menuOpen ? 'Close menu' : 'Open menu'}</span>
        <span aria-hidden="true" />
        <span aria-hidden="true" />
        <span aria-hidden="true" />
      </button>
      <div className={`header-panel${menuOpen ? ' is-open' : ''}`} id={menuId}>
        <nav className={variant === 'marketing' ? 'site-nav' : 'app-nav'} aria-label="Main navigation">
          <NavItems user={user} variant={variant === 'marketing' ? 'marketing' : 'app'} onNavigate={() => setMenuOpen(false)} />
        </nav>
        {showAuthActions && (
          <div className="header-actions">
            {user ? (
              <>
                {variant === 'admin' && <span className="admin-role">ADMIN</span>}
                <span className="header-user" title={user.email}>{user.name.split(' ')[0]}</span>
                <button className="header-link" onClick={() => { setMenuOpen(false); logout() }} type="button">Sign out</button>
              </>
            ) : (
              <>
                <Link className="header-link" to="/login" onClick={() => setMenuOpen(false)}>Sign in</Link>
                <Link className="header-link signup-link" to="/register" onClick={() => setMenuOpen(false)}>
                  Create account <span aria-hidden="true">↗</span>
                </Link>
              </>
            )}
          </div>
        )}
      </div>
    </header>
  )
}

export function EmptyState({ eyebrow, title, children, action, testId }) {
  return (
    <div className="empty-state" data-testid={testId}>
      {eyebrow && <span className="empty-state-eyebrow">{eyebrow}</span>}
      {title && <p className="empty-state-title">{title}</p>}
      {children && <p className="empty-state-body">{children}</p>}
      {action}
    </div>
  )
}

export function StatusBanner({ tone = 'error', children, action, testId, role }) {
  return (
    <div className={`status-banner ${tone}`} role={role || (tone === 'error' ? 'alert' : 'status')} data-testid={testId}>
      <div className="status-banner-body">{children}</div>
      {action}
    </div>
  )
}

export function SkeletonBlock({ className = '', lines = 0 }) {
  return (
    <div className={`skeleton-block ${className}`.trim()} aria-hidden="true">
      {Array.from({ length: lines }, (_, index) => (
        <span key={index} className="skeleton-line" style={{ width: `${88 - index * 12}%` }} />
      ))}
    </div>
  )
}

export function SkeletonCardGrid({ count = 3, testId, label }) {
  return (
    <div className="skeleton-grid" data-testid={testId} aria-busy="true" aria-live="polite">
      <span className="sr-only">{label}</span>
      {Array.from({ length: count }, (_, index) => (
        <div className="skeleton-card" key={index} aria-hidden="true">
          <span className="skeleton-chip" />
          <span className="skeleton-line wide" />
          <span className="skeleton-line" />
          <span className="skeleton-line short" />
          <span className="skeleton-footer" />
        </div>
      ))}
    </div>
  )
}

export function PageShell({ children, className = '' }) {
  return <main className={`page-shell ${className}`.trim()}>{children}</main>
}
