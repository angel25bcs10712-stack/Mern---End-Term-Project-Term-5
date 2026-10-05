import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useAuth } from './AuthContext.js'
import { AppHeader, Brand, PageShell } from '../ui/AppChrome.jsx'

export function AuthPage({ mode }) {
  const isRegister = mode === 'register'
  const { user, login, register } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to="/account" replace />

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    const form = new FormData(event.currentTarget)
    const credentials = Object.fromEntries(form.entries())

    try {
      if (isRegister) await register(credentials)
      else await login(credentials)
      navigate(location.state?.from || '/account', { replace: true })
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <PageShell className="auth-shell">
      <header className="auth-header">
        <Brand />
        <Link className="header-link" to="/">Back to home <span aria-hidden="true">↗</span></Link>
      </header>
      <section className="auth-content" aria-labelledby="auth-title">
        <div className="auth-intro">
          <p className="eyebrow"><span className="eyebrow-dot" /> YOUR LEARNING ROUTE</p>
          <h1 id="auth-title">{isRegister ? <>Start with<br /><em>one good step.</em></> : <>Pick up where<br /><em>you left off.</em></>}</h1>
          <p>{isRegister ? 'Create your CodeRoute account and keep your practice connected.' : 'Sign in to return to your personal learning route.'}</p>
        </div>
        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="form-heading">
            <span>{isRegister ? 'NEW ACCOUNT' : 'WELCOME BACK'}</span>
            <span>01 / 01</span>
          </div>
          {isRegister && (
            <label>
              Name
              <input name="name" type="text" autoComplete="name" maxLength="100" required aria-required="true" />
            </label>
          )}
          <label>
            Email
            <input name="email" type="email" autoComplete="email" maxLength="254" required aria-required="true" />
          </label>
          <label>
            Password
            <input name="password" type="password" autoComplete={isRegister ? 'new-password' : 'current-password'} minLength="8" maxLength="72" required aria-required="true" />
            {isRegister && <small>Use at least 8 characters.</small>}
          </label>
          {error && <p className="form-error" role="alert">{error}</p>}
          <button className="auth-submit" type="submit" disabled={submitting} aria-busy={submitting}>
            {submitting ? 'Please wait…' : isRegister ? 'Create account' : 'Sign in'}
            <span aria-hidden="true">→</span>
          </button>
          <p className="auth-switch">
            {isRegister ? 'Already have an account?' : 'New to CodeRoute?'}{' '}
            <Link to={isRegister ? '/login' : '/register'}>{isRegister ? 'Sign in' : 'Create an account'}</Link>
          </p>
        </form>
      </section>
      <footer className="auth-footer"><span>CodeRoute</span><span>Progress is a route, not a streak.</span></footer>
    </PageShell>
  )
}

export function AccountPage() {
  const { user } = useAuth()
  const firstName = user.name.split(' ')[0]

  return (
    <PageShell className="account-shell">
      <AppHeader variant="app" />
      <section className="account-content">
        <p className="eyebrow"><span className="eyebrow-dot" /> YOUR HOME BASE</p>
        <h1>Good to have<br /><em>you back, {firstName}.</em></h1>
        <p className="account-lead">
          Pick up practice, follow your topic roadmap, or review your profile details. Everything stays on one learning route.
        </p>

        <div className="account-hub" aria-label="Primary destinations">
          <Link className="hub-card" to="/problems">
            <span className="hub-card-meta">01 — PRACTICE</span>
            <h2>Problem library</h2>
            <p>Browse, filter, and work through curated DSA problems with adaptive recommendations.</p>
            <span className="hub-card-cta">Open problems →</span>
          </Link>
          <Link className="hub-card" to="/roadmap">
            <span className="hub-card-meta">02 — PATH</span>
            <h2>Learning roadmap</h2>
            <p>See which topics are unlocked, in progress, or waiting on prerequisites.</p>
            <span className="hub-card-cta">View roadmap →</span>
          </Link>
          {user.role === 'ADMIN' ? (
            <Link className="hub-card" to="/admin">
              <span className="hub-card-meta">03 — ADMIN</span>
              <h2>Catalog control</h2>
              <p>Manage topics, problems, and prerequisite relationships for the platform.</p>
              <span className="hub-card-cta">Open admin →</span>
            </Link>
          ) : (
            <div className="hub-card" aria-hidden="true">
              <span className="hub-card-meta">03 — PROFILE</span>
              <h2>Stay consistent</h2>
              <p>Your attempts and solved problems feed recommendations and roadmap progress.</p>
              <span className="hub-card-cta">Keep practicing</span>
            </div>
          )}
        </div>

        <section className="account-panel" aria-labelledby="profile-title">
          <h2 id="profile-title">Profile</h2>
          <dl className="account-details">
            <div><dt>Name</dt><dd>{user.name}</dd></div>
            <div><dt>Email</dt><dd>{user.email}</dd></div>
            <div><dt>Role</dt><dd>{user.role}</dd></div>
          </dl>
        </section>
      </section>
    </PageShell>
  )
}

export function ProtectedRoute({ children }) {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <main className="session-loading" aria-live="polite">Checking your session…</main>
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return children
}
