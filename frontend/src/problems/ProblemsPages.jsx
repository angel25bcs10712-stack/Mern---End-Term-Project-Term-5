import { useDeferredValue, useEffect, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.js'
import { apiRequest } from '../api/client.js'
import { AppHeader, EmptyState, PageShell, SkeletonCardGrid, StatusBanner } from '../ui/AppChrome.jsx'

function useTopics(token) {
  const [topics, setTopics] = useState([])
  useEffect(() => {
    let active = true
    apiRequest('/api/topics', { token })
      .then((data) => { if (active) setTopics(data) })
      .catch(() => { if (active) setTopics([]) })
    return () => { active = false }
  }, [token])
  return topics
}

function useProgress(token) {
  const [progress, setProgress] = useState(null)
  useEffect(() => {
    let active = true
    apiRequest('/api/users/me/progress', { token })
      .then((data) => { if (active) setProgress(data) })
      .catch(() => { if (active) setProgress(null) })
    return () => { active = false }
  }, [token])
  return progress
}

export function RecommendationsSection({ token }) {
  const [recommendations, setRecommendations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadIndex, setReloadIndex] = useState(0)

  useEffect(() => {
    let active = true
    apiRequest('/api/recommendations', { token })
      .then((items) => { if (active) setRecommendations(items) })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [token, reloadIndex])

  return (
    <section className="recommendation-section" aria-labelledby="recommendation-title">
      <div className="recommendation-heading">
        <div>
          <p className="eyebrow"><span className="eyebrow-dot" /> ADAPTIVE NEXT STEPS</p>
          <h2 id="recommendation-title">Recommended For You</h2>
        </div>
        <span className="recommendation-count">BASED ON YOUR PRACTICE</span>
      </div>
      {loading && (
        <SkeletonCardGrid count={3} testId="recommendations-loading" label="Finding a useful next step…" />
      )}
      {error && (
        <StatusBanner
          tone="error"
          testId="recommendations-error"
          action={(
            <button type="button" onClick={() => { setLoading(true); setError(''); setReloadIndex((i) => i + 1) }} className="retry-button">
              Try again
            </button>
          )}
        >
          <p>{error}</p>
        </StatusBanner>
      )}
      {!loading && !error && recommendations.length === 0 && (
        <EmptyState
          testId="recommendations-empty"
          eyebrow="NO OPEN RECS"
          title="Nothing queued right now"
        >
          No open recommendations right now. Keep practicing to refresh your route.
        </EmptyState>
      )}
      {!loading && !error && recommendations.length > 0 && (
        <div className="recommendation-grid" data-testid="recommendations-grid">
          {recommendations.slice(0, 3).map((recommendation) => {
            const problem = recommendation.problem
            return (
              <article className="recommendation-card" key={problem.id} data-testid="recommendation-card">
                <div className="recommendation-meta">
                  <span>{recommendation.topic}</span>
                  <span className={`difficulty-tag ${recommendation.difficulty.toLowerCase()}`}>{recommendation.difficulty}</span>
                </div>
                <h3>{problem.title}</h3>
                <p className="recommendation-reason">{recommendation.reasonForRecommendation}</p>
                <Link className="recommendation-start" to={`/problems/${problem.id}`}>
                  Start problem <span aria-hidden="true">→</span>
                </Link>
              </article>
            )
          })}
        </div>
      )}
    </section>
  )
}

export function ProblemsPage() {
  const { token } = useAuth()
  const [searchParams] = useSearchParams()
  const topics = useTopics(token)
  const progress = useProgress(token)
  const [search, setSearch] = useState('')
  const deferredSearch = useDeferredValue(search)
  const [topicId, setTopicId] = useState(() => searchParams.get('topicId') || '')
  const [difficulty, setDifficulty] = useState('')
  const [solved, setSolved] = useState('')
  const [page, setPage] = useState(0)
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadTrigger, setReloadTrigger] = useState(0)

  useEffect(() => {
    const query = new URLSearchParams({ page: String(page), size: '12' })
    if (deferredSearch.trim()) query.set('search', deferredSearch.trim())
    if (topicId) query.set('topicId', topicId)
    if (difficulty) query.set('difficulty', difficulty)
    if (solved) query.set('solved', solved)

    let active = true
    setLoading(true)
    setError('')
    apiRequest(`/api/problems?${query}`, { token })
      .then((data) => { if (active) setResult(data) })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [token, deferredSearch, topicId, difficulty, solved, page, reloadTrigger])

  const pageCount = result?.totalPages || 0
  const listingLoading = loading || deferredSearch !== search
  const solvedPercent = progress?.totalProblems
    ? Math.round((progress.problemsSolved / progress.totalProblems) * 100)
    : 0

  function changeFilter(setter, value) {
    setter(value)
    setPage(0)
  }

  function clearFilters() {
    setSearch('')
    setTopicId('')
    setDifficulty('')
    setSolved('')
    setPage(0)
  }

  return (
    <PageShell className="problems-shell">
      <AppHeader variant="app" />
      <section className="problems-main">
        <div className="problems-title-row">
          <div>
            <p className="eyebrow"><span className="eyebrow-dot" /> PRACTICE LIBRARY</p>
            <h1>Find your next<br /><em>useful challenge.</em></h1>
          </div>
          <div className="overall-progress" aria-label={`${progress?.problemsSolved || 0} of ${progress?.totalProblems || 0} problems solved`}>
            <div className="overall-progress-label">
              <span>CATALOG PROGRESS</span>
              <strong>{progress?.problemsSolved || 0}<small> / {progress?.totalProblems || 0}</small></strong>
            </div>
            <div className="progress-track"><span style={{ width: `${solvedPercent}%` }} /></div>
            <p>{progress?.problemsAttempted || 0} distinct problems attempted</p>
          </div>
        </div>

        <RecommendationsSection token={token} />

        <div className="problem-filters" role="search">
          <label className="problem-search">
            <span aria-hidden="true">⌕</span>
            <input value={search} onChange={(event) => changeFilter(setSearch, event.target.value)} placeholder="Search problems or topics" aria-label="Search problems" />
          </label>
          <label>
            <span className="filter-label">TOPIC</span>
            <select value={topicId} onChange={(event) => changeFilter(setTopicId, event.target.value)} aria-label="Filter by topic">
              <option value="">All topics</option>
              {topics.map((topic) => <option key={topic.id} value={topic.id}>{topic.name}</option>)}
            </select>
          </label>
          <label>
            <span className="filter-label">DIFFICULTY</span>
            <select value={difficulty} onChange={(event) => changeFilter(setDifficulty, event.target.value)} aria-label="Filter by difficulty">
              <option value="">All levels</option>
              <option value="BEGINNER">Beginner</option>
              <option value="INTERMEDIATE">Intermediate</option>
              <option value="ADVANCED">Advanced</option>
            </select>
          </label>
          <label>
            <span className="filter-label">STATUS</span>
            <select value={solved} onChange={(event) => changeFilter(setSolved, event.target.value)} aria-label="Filter by status">
              <option value="">Any status</option>
              <option value="false">Unsolved</option>
              <option value="true">Solved</option>
            </select>
          </label>
        </div>

        <div className="problem-results-heading">
          <span>{result?.totalElements ?? '—'} PROBLEMS</span>
          {result && <span>PAGE {result.totalPages ? result.page + 1 : 0} OF {result.totalPages}</span>}
        </div>

        {error && (
          <StatusBanner
            tone="error"
            testId="problems-error"
            action={(
              <button type="button" onClick={() => { setLoading(true); setError(''); setReloadTrigger((t) => t + 1) }} className="retry-button">
                Retry loading problems
              </button>
            )}
          >
            <p>{error}</p>
          </StatusBanner>
        )}

        {listingLoading && !error && (
          <SkeletonCardGrid count={6} testId="problems-loading" label="Loading problems…" />
        )}

        {!listingLoading && !error && result?.content.length === 0 && (
          <EmptyState
            testId="problems-empty"
            eyebrow="NO MATCHES"
            title="No problems match these filters"
            action={<button type="button" className="retry-button" onClick={clearFilters}>Clear filters</button>}
          >
            Try a broader search or clear one of the filters.
          </EmptyState>
        )}

        {!listingLoading && !error && result?.content && result.content.length > 0 && (
          <div className="problem-grid" aria-busy={loading} data-testid="problems-grid">
            {result.content.map((problem) => (
              <Link className="problem-card" to={`/problems/${problem.id}`} key={problem.id}>
                <div className="problem-card-top">
                  <span>{problem.topicName}</span>
                  <span className={`difficulty-tag ${problem.difficulty.toLowerCase()}`}>{problem.difficulty}</span>
                </div>
                <h2>{problem.title}</h2>
                <p>{problem.description}</p>
                <div className="problem-card-bottom">
                  <span>{problem.estimatedTimeMinutes} MIN</span>
                  {problem.solved
                    ? <span className="solved-mark">SOLVED <b aria-hidden="true">✓</b></span>
                    : <span>OPEN <b aria-hidden="true">↗</b></span>}
                </div>
              </Link>
            ))}
          </div>
        )}

        {pageCount > 1 && (
          <nav className="pagination" aria-label="Problem pages">
            <button type="button" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={page === 0}>← Previous</button>
            <span>{page + 1} / {pageCount}</span>
            <button type="button" onClick={() => setPage((current) => Math.min(pageCount - 1, current + 1))} disabled={page + 1 >= pageCount}>Next →</button>
          </nav>
        )}
      </section>
    </PageShell>
  )
}

export function ProblemDetailPage() {
  const { token } = useAuth()
  const { id } = useParams()
  const navigate = useNavigate()
  const [problem, setProblem] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')
  const [notice, setNotice] = useState('')
  const [timeTakenSeconds, setTimeTakenSeconds] = useState('')
  const [attempts, setAttempts] = useState('1')
  const [saving, setSaving] = useState(false)
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    apiRequest(`/api/problems/${id}`, { token })
      .then((data) => { if (active) setProblem(data) })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id, token, revision])

  useEffect(() => {
    if (!notice) return undefined
    const timer = window.setTimeout(() => setNotice(''), 4000)
    return () => window.clearTimeout(timer)
  }, [notice])

  async function record(status) {
    if (timeTakenSeconds === '' || Number(timeTakenSeconds) < 0) {
      setActionError('Time taken must be zero or a positive number.')
      return
    }
    if (!attempts || Number(attempts) < 1) {
      setActionError('Number of attempts must be at least 1.')
      return
    }

    setSaving(true)
    setActionError('')
    setNotice('')
    try {
      await apiRequest(`/api/problems/${id}/${status}`, {
        token,
        method: 'POST',
        body: { timeTakenSeconds: Number(timeTakenSeconds), attempts: Number(attempts) },
      })
      setNotice(status === 'solve' ? 'Solved status recorded. Nice work.' : 'Attempt saved to your history.')
      setRevision((value) => value + 1)
    } catch (requestError) {
      setActionError(requestError.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <PageShell className="problems-shell">
      <AppHeader variant="app" />
      <section className="problem-detail-main">
        <button className="back-link" type="button" onClick={() => navigate('/problems')}>← All problems</button>
        {loading && (
          <div data-testid="problem-loading" aria-busy="true" aria-live="polite">
            <span className="sr-only">Loading problem…</span>
            <div className="skeleton-block" aria-hidden="true" style={{ minHeight: 220, marginBottom: 16 }}>
              <span className="skeleton-chip" />
              <span className="skeleton-line wide" />
              <span className="skeleton-line" />
              <span className="skeleton-line short" />
            </div>
          </div>
        )}
        {error && (
          <StatusBanner
            tone="error"
            testId="problem-error"
            action={(
              <button type="button" onClick={() => { setLoading(true); setError(''); setRevision((r) => r + 1) }} className="retry-button">
                Try again
              </button>
            )}
          >
            <p>{error}</p>
          </StatusBanner>
        )}
        {problem && !loading && (
          <div className="detail-layout">
            <article className="problem-statement">
              <div className="detail-meta">
                <span>{problem.topicName}</span>
                <span className={`difficulty-tag ${problem.difficulty.toLowerCase()}`}>{problem.difficulty}</span>
                <span>{problem.estimatedTimeMinutes} MIN</span>
              </div>
              <h1>{problem.title}</h1>
              <div className="detail-tags">{problem.tags.map((tag) => <span key={tag}>{tag}</span>)}</div>
              <p className="statement-label">PROBLEM BRIEF</p>
              <p className="problem-description">{problem.description}</p>
              {problem.externalUrl && (
                <a className="reference-link" href={problem.externalUrl} target="_blank" rel="noreferrer">
                  Open reference ↗
                </a>
              )}
            </article>
            <aside className="attempt-panel">
              <p className="eyebrow"><span className="eyebrow-dot" /> PRACTICE LOG</p>
              <h2>{problem.solved ? 'Solved' : 'Record your work'}</h2>
              <p className="attempt-intro">Add the time and total tries for this practice session.</p>
              <label>
                Time taken <span className="input-unit">SECONDS</span>
                <input type="number" min="0" step="1" value={timeTakenSeconds} onChange={(event) => setTimeTakenSeconds(event.target.value)} required aria-required="true" />
              </label>
              <label>
                Number of attempts
                <input type="number" min="1" step="1" value={attempts} onChange={(event) => setAttempts(event.target.value)} required aria-required="true" />
              </label>
              {actionError && <p className="form-error" role="alert" data-testid="attempt-action-error">{actionError}</p>}
              {notice && <p className="practice-notice" role="status" data-testid="attempt-notice">{notice}</p>}
              <button className="attempt-action secondary-action" type="button" onClick={() => record('attempt')} disabled={saving || timeTakenSeconds === '' || !attempts}>
                {saving ? 'Saving…' : 'Record attempt'}
              </button>
              <button className="attempt-action primary-action" type="button" onClick={() => record('solve')} disabled={saving || timeTakenSeconds === '' || !attempts}>
                {saving ? 'Saving…' : <>Mark solved <span aria-hidden="true">✓</span></>}
              </button>
            </aside>
          </div>
        )}
      </section>
    </PageShell>
  )
}
