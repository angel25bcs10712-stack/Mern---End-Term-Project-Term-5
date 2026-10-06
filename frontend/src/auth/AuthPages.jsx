import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useAuth } from './AuthContext.js'
import { apiRequest } from '../api/client.js'
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
  const { user, token, updateUser } = useAuth()
  const firstName = user.name.split(' ')[0]

  const [leetcodeUrl, setLeetcodeUrl] = useState(user.leetcodeProfileUrl || '')
  const [leetcodeDraft, setLeetcodeDraft] = useState(user.leetcodeProfileUrl || '')
  const [savingProfile, setSavingProfile] = useState(false)
  const [profileError, setProfileError] = useState('')
  const [stats, setStats] = useState(null)
  const [statsState, setStatsState] = useState('idle')
  const [statsError, setStatsError] = useState('')

  const [todos, setTodos] = useState([])
  const [summary, setSummary] = useState({ total: 0, completed: 0, remaining: 0, progressPercent: 0 })
  const [todoDraft, setTodoDraft] = useState('')
  const [todoBusy, setTodoBusy] = useState(false)
  const [todoError, setTodoError] = useState('')

  useEffect(() => {
    if (!leetcodeUrl) return undefined
    let active = true
    setStatsState('loading')
    apiRequest(`/api/leetcode/stats?profileUrl=${encodeURIComponent(leetcodeUrl)}`, { token })
      .then((data) => {
        if (active) {
          setStats(data)
          setStatsError('')
          setStatsState('ready')
        }
      })
      .catch((requestError) => {
        if (active) {
          setStats(null)
          setStatsError(requestError.message)
          setStatsState('error')
        }
      })
    return () => { active = false }
  }, [leetcodeUrl, token])

  useEffect(() => {
    let active = true
    apiRequest('/api/users/me/todos', { token })
      .then((data) => {
        if (active) {
          setTodos(data.items)
          setSummary(data)
        }
      })
      .catch((requestError) => {
        if (active) setTodoError(requestError.message)
      })
    return () => { active = false }
  }, [token])

  async function refreshTodos() {
    const data = await apiRequest('/api/users/me/todos', { token })
    setTodos(data.items)
    setSummary(data)
  }

  async function handleSaveLeetcode(event) {
    event.preventDefault()
    setProfileError('')
    setSavingProfile(true)
    try {
      const updated = await apiRequest('/api/auth/me', {
        token,
        method: 'PUT',
        body: { leetcodeProfileUrl: leetcodeDraft.trim() },
      })
      updateUser(updated)
      const nextUrl = updated.leetcodeProfileUrl || ''
      setLeetcodeUrl(nextUrl)
      if (!nextUrl) {
        setStats(null)
        setStatsError('')
        setStatsState('idle')
      }
    } catch (requestError) {
      setProfileError(requestError.message)
    } finally {
      setSavingProfile(false)
    }
  }

  async function handleAddTodo(event) {
    event.preventDefault()
    const title = todoDraft.trim()
    if (!title || todoBusy) return
    setTodoError('')
    setTodoBusy(true)
    try {
      await apiRequest('/api/users/me/todos', { token, method: 'POST', body: { title } })
      setTodoDraft('')
      await refreshTodos()
    } catch (requestError) {
      setTodoError(requestError.message)
    } finally {
      setTodoBusy(false)
    }
  }

  async function handleToggleTodo(item) {
    if (todoBusy) return
    setTodoError('')
    setTodoBusy(true)
    try {
      await apiRequest(`/api/users/me/todos/${item.id}`, {
        token,
        method: 'PATCH',
        body: { completed: !item.completed },
      })
      await refreshTodos()
    } catch (requestError) {
      setTodoError(requestError.message)
    } finally {
      setTodoBusy(false)
    }
  }

  async function handleDeleteTodo(item) {
    if (todoBusy) return
    setTodoError('')
    setTodoBusy(true)
    try {
      await apiRequest(`/api/users/me/todos/${item.id}`, { token, method: 'DELETE' })
      await refreshTodos()
    } catch (requestError) {
      setTodoError(requestError.message)
    } finally {
      setTodoBusy(false)
    }
  }

  const remainingTodos = todos.filter((item) => !item.completed)
  const completedTodos = todos.filter((item) => item.completed)

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
            <div>
              <dt>LeetCode</dt>
              <dd>
                {leetcodeUrl ? (
                  <a className="account-link" href={leetcodeUrl} target="_blank" rel="noreferrer">
                    {leetcodeUrl} <span aria-hidden="true">↗</span>
                  </a>
                ) : (
                  <span className="account-link-muted">Not linked yet — you can add one below.</span>
                )}
              </dd>
            </div>
          </dl>

          <form className="account-link-form" onSubmit={handleSaveLeetcode} noValidate>
            <label htmlFor="leetcode-profile-url">
              LeetCode profile link (optional)
              <input
                id="leetcode-profile-url"
                name="leetcodeProfileUrl"
                type="url"
                maxLength={255}
                value={leetcodeDraft}
                onChange={(event) => setLeetcodeDraft(event.target.value)}
                placeholder="https://leetcode.com/u/username/"
              />
              <small>Add a link to your public LeetCode profile. No password is ever required.</small>
            </label>
            <button className="admin-submit" type="submit" disabled={savingProfile}>
              {savingProfile ? 'Saving…' : 'Save link'}
            </button>
          </form>
          {profileError && <p className="form-error" role="alert">{profileError}</p>}

          {leetcodeUrl && statsState === 'loading' && (
            <p className="account-note">Loading public LeetCode stats…</p>
          )}
          {leetcodeUrl && statsState === 'error' && (
            <p className="form-error" role="alert">{statsError}</p>
          )}
          {statsState === 'ready' && stats && (
            <div className="leetcode-metrics" aria-label={`LeetCode stats for ${stats.username}`}>
              <div className="leetcode-metric"><span>TOTAL SOLVED</span><strong>{stats.totalSolved}</strong></div>
              <div className="leetcode-metric"><span>EASY</span><strong>{stats.easy}</strong></div>
              <div className="leetcode-metric"><span>MEDIUM</span><strong>{stats.medium}</strong></div>
              <div className="leetcode-metric"><span>HARD</span><strong>{stats.hard}</strong></div>
              <div className="leetcode-metric">
                <span>CONTEST RATING</span>
                <strong>{stats.contestRating == null ? '—' : Math.round(stats.contestRating)}</strong>
              </div>
            </div>
          )}
        </section>

        <section className="account-panel" aria-labelledby="todo-title">
          <h2 id="todo-title">DSA to-do</h2>

          <div className="todo-progress" aria-label={`${summary.completed} of ${summary.total} tasks completed`}>
            <div className="overall-progress-label">
              <span>DSA TO-DO PROGRESS</span>
              <strong>{summary.completed}<small> / {summary.total}</small></strong>
            </div>
            <div
              className="progress-track"
              role="progressbar"
              aria-valuemin={0}
              aria-valuemax={summary.total}
              aria-valuenow={summary.completed}
            >
              <span style={{ width: `${summary.progressPercent}%` }} />
            </div>
            <p>{summary.completed}/{summary.total} completed · {summary.remaining} remaining</p>
          </div>

          <form className="todo-form" onSubmit={handleAddTodo}>
            <label className="sr-only" htmlFor="todo-title-input">New DSA task</label>
            <input
              id="todo-title-input"
              name="title"
              type="text"
              maxLength={200}
              value={todoDraft}
              onChange={(event) => setTodoDraft(event.target.value)}
              placeholder="e.g. Merge Two Sorted Lists"
            />
            <button className="admin-submit" type="submit" disabled={todoBusy}>
              {todoBusy ? 'Working…' : 'Add task'}
            </button>
          </form>
          {todoError && <p className="form-error" role="alert">{todoError}</p>}

          {todos.length === 0 && !todoError && (
            <p className="account-note">No tasks yet — add a DSA problem above to start your list.</p>
          )}

          {remainingTodos.length > 0 && (
            <div className="todo-group">
              <h3>REMAINING ({remainingTodos.length})</h3>
              <ul className="todo-list">
                {remainingTodos.map((item) => (
                  <li key={item.id} className="todo-item">
                    <label>
                      <input
                        type="checkbox"
                        checked={item.completed}
                        disabled={todoBusy}
                        onChange={() => handleToggleTodo(item)}
                      />
                      <span>{item.title}</span>
                    </label>
                    <button
                      className="todo-delete"
                      type="button"
                      disabled={todoBusy}
                      onClick={() => handleDeleteTodo(item)}
                      aria-label={`Delete task ${item.title}`}
                    >
                      Delete
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {completedTodos.length > 0 && (
            <div className="todo-group">
              <h3>COMPLETED ({completedTodos.length})</h3>
              <ul className="todo-list">
                {completedTodos.map((item) => (
                  <li key={item.id} className="todo-item is-done">
                    <label>
                      <input
                        type="checkbox"
                        checked={item.completed}
                        disabled={todoBusy}
                        onChange={() => handleToggleTodo(item)}
                      />
                      <span>{item.title}</span>
                    </label>
                    <button
                      className="todo-delete"
                      type="button"
                      disabled={todoBusy}
                      onClick={() => handleDeleteTodo(item)}
                      aria-label={`Delete task ${item.title}`}
                    >
                      Delete
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          )}
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
