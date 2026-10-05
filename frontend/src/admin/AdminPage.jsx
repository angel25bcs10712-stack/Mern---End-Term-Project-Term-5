import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.js'
import { apiRequest } from '../api/client.js'
import { AppHeader, EmptyState, PageShell, StatusBanner } from '../ui/AppChrome.jsx'

const blankProblem = {
  title: '',
  description: '',
  difficulty: 'BEGINNER',
  topicId: '',
  externalUrl: '',
  tags: '',
  estimatedTimeMinutes: '20',
}
const blankTopic = { name: '', description: '', difficulty: 'BEGINNER', parentTopicId: '' }

function adminRequest(path, token, options = {}) {
  return apiRequest(path, {
    token,
    method: options.method || 'GET',
    body: options.body,
  })
}

function Metric({ label, value, detail }) {
  return (
    <article className="admin-metric">
      <span>{label}</span>
      <strong>{value ?? '—'}</strong>
      <small>{detail}</small>
    </article>
  )
}

function MetricsSkeleton() {
  return (
    <div className="skeleton-metrics" aria-busy="true" aria-live="polite">
      <span className="sr-only">Loading platform statistics…</span>
      <span /><span /><span /><span />
    </div>
  )
}

export default function AdminPage() {
  const { user, token } = useAuth()
  const [stats, setStats] = useState(null)
  const [problems, setProblems] = useState([])
  const [topics, setTopics] = useState([])
  const [tab, setTab] = useState('problems')
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [refreshKey, setRefreshKey] = useState(0)
  const [problemId, setProblemId] = useState('')
  const [problemForm, setProblemForm] = useState(blankProblem)
  const [topicId, setTopicId] = useState('')
  const [topicForm, setTopicForm] = useState(blankTopic)
  const [prerequisiteTopicId, setPrerequisiteTopicId] = useState('')
  const [prerequisiteIds, setPrerequisiteIds] = useState([])

  useEffect(() => {
    let active = true
    setLoading(true)
    Promise.all([
      adminRequest('/api/admin/stats', token),
      adminRequest('/api/admin/problems', token),
      adminRequest('/api/admin/topics', token),
    ])
      .then(([nextStats, nextProblems, nextTopics]) => {
        if (!active) return
        setStats(nextStats)
        setProblems(nextProblems)
        setTopics(nextTopics)
        setError('')
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [token, refreshKey])

  useEffect(() => {
    if (!notice) return undefined
    const timer = window.setTimeout(() => setNotice(''), 3500)
    return () => window.clearTimeout(timer)
  }, [notice])

  if (user?.role !== 'ADMIN') return <Navigate to="/account" replace />

  async function runAction(action, successMessage) {
    setBusy(true)
    setError('')
    setNotice('')
    try {
      await action()
      setNotice(successMessage)
      setRefreshKey((value) => value + 1)
      return true
    } catch (requestError) {
      setError(requestError.message)
      return false
    } finally {
      setBusy(false)
    }
  }

  function beginProblemEdit(problem) {
    setProblemId(problem.id)
    setProblemForm({
      title: problem.title,
      description: problem.description,
      difficulty: problem.difficulty,
      topicId: problem.topicId,
      externalUrl: problem.externalUrl || '',
      tags: problem.tags.join(', '),
      estimatedTimeMinutes: String(problem.estimatedTimeMinutes),
    })
    setTab('problems')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function saveProblem(event) {
    event.preventDefault()
    const payload = {
      ...problemForm,
      topicId: problemForm.topicId,
      externalUrl: problemForm.externalUrl.trim() || null,
      tags: problemForm.tags.split(',').map((tag) => tag.trim()).filter(Boolean),
      estimatedTimeMinutes: Number(problemForm.estimatedTimeMinutes),
    }
    const saved = await runAction(() => adminRequest(
      problemId ? `/api/admin/problems/${problemId}` : '/api/admin/problems',
      token,
      { method: problemId ? 'PUT' : 'POST', body: payload },
    ), problemId ? 'Problem updated.' : 'Problem added.')
    if (saved) {
      setProblemId('')
      setProblemForm(blankProblem)
    }
  }

  async function deleteProblem(problem) {
    if (!window.confirm(`Delete “${problem.title}”? This cannot be undone.`)) return
    await runAction(() => adminRequest(`/api/admin/problems/${problem.id}`, token, { method: 'DELETE' }), 'Problem deleted.')
    if (problemId === problem.id) {
      setProblemId('')
      setProblemForm(blankProblem)
    }
  }

  function beginTopicEdit(topic) {
    setTopicId(topic.id)
    setTopicForm({
      name: topic.name,
      description: topic.description || '',
      difficulty: topic.difficulty,
      parentTopicId: topic.parentTopicId || '',
    })
    setTab('topics')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function saveTopic(event) {
    event.preventDefault()
    const payload = {
      ...topicForm,
      name: topicForm.name.trim(),
      description: topicForm.description.trim() || null,
      parentTopicId: topicForm.parentTopicId || null,
    }
    const saved = await runAction(() => adminRequest(
      topicId ? `/api/admin/topics/${topicId}` : '/api/admin/topics',
      token,
      { method: topicId ? 'PUT' : 'POST', body: payload },
    ), topicId ? 'Topic updated.' : 'Topic created.')
    if (saved) {
      setTopicId('')
      setTopicForm(blankTopic)
    }
  }

  async function deleteTopic(topic) {
    if (!window.confirm(`Delete “${topic.name}”? Linked problems, progress, or prerequisite relationships will prevent deletion.`)) return
    await runAction(() => adminRequest(`/api/admin/topics/${topic.id}`, token, { method: 'DELETE' }), 'Topic deleted.')
    if (topicId === topic.id) {
      setTopicId('')
      setTopicForm(blankTopic)
    }
    if (prerequisiteTopicId === topic.id) setPrerequisiteTopicId('')
  }

  function openPrerequisites(topic) {
    setPrerequisiteTopicId(topic.id)
    setPrerequisiteIds(topic.prerequisiteTopicIds)
    setTab('topics')
  }

  async function savePrerequisites(event) {
    event.preventDefault()
    await runAction(() => adminRequest(`/api/admin/topics/${prerequisiteTopicId}/prerequisites`, token, {
      method: 'PUT',
      body: { prerequisiteTopicIds: prerequisiteIds },
    }), 'Prerequisites saved.')
  }

  const selectedPrerequisiteTopic = topics.find((topic) => topic.id === prerequisiteTopicId)
  const topicById = new Map(topics.map((topic) => [topic.id, topic.name]))

  return (
    <PageShell className="admin-shell">
      <AppHeader variant="admin" />
      <section className="admin-main">
        <div className="admin-title-row">
          <div>
            <p className="eyebrow"><span className="eyebrow-dot" /> PLATFORM OPERATIONS</p>
            <h1>Admin <em>dashboard</em></h1>
          </div>
          <span className="admin-updated">CATALOG CONTROL</span>
        </div>

        {error && <StatusBanner tone="error"><p>{error}</p></StatusBanner>}
        {notice && <StatusBanner tone="success"><p>{notice}</p></StatusBanner>}

        {loading ? (
          <MetricsSkeleton />
        ) : (
          <section className="admin-metrics" aria-label="Platform statistics" aria-busy={loading}>
            <Metric label="LEARNERS" value={stats?.users} detail="Registered accounts" />
            <Metric label="TOPICS" value={stats?.topics} detail="Learning categories" />
            <Metric label="PROBLEMS" value={stats?.problems} detail="Catalog entries" />
            <Metric label="ATTEMPTS" value={stats?.attempts} detail={`${stats?.solvedAttempts ?? '—'} solved records`} />
          </section>
        )}

        <div className="admin-workspace">
          <nav className="admin-tabs" aria-label="Admin sections">
            <button type="button" aria-pressed={tab === 'problems'} onClick={() => setTab('problems')}>
              Problems <span>{problems.length}</span>
            </button>
            <button type="button" aria-pressed={tab === 'topics'} onClick={() => setTab('topics')}>
              Topics <span>{topics.length}</span>
            </button>
          </nav>

          {tab === 'problems' ? (
            <div className="admin-columns">
              <section className="admin-panel" aria-labelledby="problem-form-title">
                <div className="admin-panel-heading">
                  <div>
                    <p className="eyebrow">CATALOG ENTRY</p>
                    <h2 id="problem-form-title">{problemId ? 'Edit problem' : 'Add a problem'}</h2>
                  </div>
                  {problemId && (
                    <button className="admin-text-button" type="button" onClick={() => { setProblemId(''); setProblemForm(blankProblem) }}>
                      Cancel edit
                    </button>
                  )}
                </div>
                <form className="admin-form" onSubmit={saveProblem}>
                  <label>Title<input required maxLength="200" value={problemForm.title} onChange={(event) => setProblemForm({ ...problemForm, title: event.target.value })} /></label>
                  <label>Description<textarea required maxLength="10000" rows="5" value={problemForm.description} onChange={(event) => setProblemForm({ ...problemForm, description: event.target.value })} /></label>
                  <div className="admin-form-row">
                    <label>
                      Difficulty
                      <select value={problemForm.difficulty} onChange={(event) => setProblemForm({ ...problemForm, difficulty: event.target.value })}>
                        <option value="BEGINNER">Beginner</option>
                        <option value="INTERMEDIATE">Intermediate</option>
                        <option value="ADVANCED">Advanced</option>
                      </select>
                    </label>
                    <label>
                      Topic
                      <select required value={problemForm.topicId} onChange={(event) => setProblemForm({ ...problemForm, topicId: event.target.value })}>
                        <option value="">Select topic</option>
                        {topics.map((topic) => <option key={topic.id} value={topic.id}>{topic.name}</option>)}
                      </select>
                    </label>
                  </div>
                  <div className="admin-form-row">
                    <label>Time estimate (min)<input type="number" min="1" max="1440" required value={problemForm.estimatedTimeMinutes} onChange={(event) => setProblemForm({ ...problemForm, estimatedTimeMinutes: event.target.value })} /></label>
                    <label>Tags<input maxLength="400" value={problemForm.tags} onChange={(event) => setProblemForm({ ...problemForm, tags: event.target.value })} placeholder="arrays, hashing" /></label>
                  </div>
                  <label>Reference URL<input type="url" maxLength="2048" value={problemForm.externalUrl} onChange={(event) => setProblemForm({ ...problemForm, externalUrl: event.target.value })} placeholder="https://example.com/problem" /></label>
                  <button className="admin-submit" type="submit" disabled={busy || topics.length === 0}>
                    {busy ? 'Saving…' : problemId ? 'Save changes' : 'Add problem'}
                  </button>
                  {topics.length === 0 && <p className="admin-hint">Create a topic before adding problems.</p>}
                </form>
              </section>

              <section className="admin-panel admin-list-panel" aria-labelledby="problems-list-title">
                <div className="admin-panel-heading">
                  <div><p className="eyebrow">CURRENT CATALOG</p><h2 id="problems-list-title">Problems</h2></div>
                  <span className="admin-list-count">{problems.length} TOTAL</span>
                </div>
                {loading ? (
                  <p className="admin-empty">Loading problems…</p>
                ) : problems.length === 0 ? (
                  <EmptyState eyebrow="EMPTY CATALOG" title="No problems yet">No problems have been added.</EmptyState>
                ) : (
                  <div className="admin-record-list">
                    {problems.map((problem) => (
                      <article className="admin-record" key={problem.id}>
                        <div className="admin-record-content">
                          <div className="admin-record-meta"><span>{problem.topicName}</span><span>{problem.difficulty}</span></div>
                          <h3>{problem.title}</h3>
                          <p>{problem.description}</p>
                          <small>{problem.estimatedTimeMinutes} min · {problem.tags.join(', ') || 'No tags'}</small>
                        </div>
                        <div className="admin-record-actions">
                          <button type="button" onClick={() => beginProblemEdit(problem)}>Edit</button>
                          <button className="danger" type="button" disabled={busy} onClick={() => deleteProblem(problem)}>Delete</button>
                        </div>
                      </article>
                    ))}
                  </div>
                )}
              </section>
            </div>
          ) : (
            <div className="admin-columns">
              <section className="admin-panel" aria-labelledby="topic-form-title">
                <div className="admin-panel-heading">
                  <div>
                    <p className="eyebrow">LEARNING STRUCTURE</p>
                    <h2 id="topic-form-title">{topicId ? 'Edit topic' : 'Create a topic'}</h2>
                  </div>
                  {topicId && (
                    <button className="admin-text-button" type="button" onClick={() => { setTopicId(''); setTopicForm(blankTopic) }}>
                      Cancel edit
                    </button>
                  )}
                </div>
                <form className="admin-form" onSubmit={saveTopic}>
                  <label>Name<input required maxLength="120" value={topicForm.name} onChange={(event) => setTopicForm({ ...topicForm, name: event.target.value })} /></label>
                  <label>Description<textarea maxLength="2000" rows="4" value={topicForm.description} onChange={(event) => setTopicForm({ ...topicForm, description: event.target.value })} /></label>
                  <div className="admin-form-row">
                    <label>
                      Difficulty
                      <select value={topicForm.difficulty} onChange={(event) => setTopicForm({ ...topicForm, difficulty: event.target.value })}>
                        <option value="BEGINNER">Beginner</option>
                        <option value="INTERMEDIATE">Intermediate</option>
                        <option value="ADVANCED">Advanced</option>
                      </select>
                    </label>
                    <label>
                      Parent topic
                      <select value={topicForm.parentTopicId} onChange={(event) => setTopicForm({ ...topicForm, parentTopicId: event.target.value })}>
                        <option value="">None</option>
                        {topics.filter((topic) => topic.id !== topicId).map((topic) => (
                          <option key={topic.id} value={topic.id}>{topic.name}</option>
                        ))}
                      </select>
                    </label>
                  </div>
                  <button className="admin-submit" type="submit" disabled={busy}>
                    {busy ? 'Saving…' : topicId ? 'Save changes' : 'Create topic'}
                  </button>
                </form>
              </section>

              <section className="admin-panel admin-list-panel" aria-labelledby="topics-list-title">
                <div className="admin-panel-heading">
                  <div><p className="eyebrow">CURRICULUM</p><h2 id="topics-list-title">Topics</h2></div>
                  <span className="admin-list-count">{topics.length} TOTAL</span>
                </div>
                {loading ? (
                  <p className="admin-empty">Loading topics…</p>
                ) : topics.length === 0 ? (
                  <EmptyState eyebrow="EMPTY CURRICULUM" title="No topics yet">No topics have been created.</EmptyState>
                ) : (
                  <div className="admin-record-list">
                    {topics.map((topic) => (
                      <article className="admin-record topic-record" key={topic.id}>
                        <div className="admin-record-content">
                          <div className="admin-record-meta">
                            <span>{topic.difficulty}</span>
                            <span>{topic.parentTopicId ? `Child of ${topicById.get(topic.parentTopicId) || 'another topic'}` : 'Top level'}</span>
                          </div>
                          <h3>{topic.name}</h3>
                          <p>{topic.description || 'No description provided.'}</p>
                          <small>{topic.prerequisiteTopicIds.length} prerequisites</small>
                        </div>
                        <div className="admin-record-actions">
                          <button type="button" onClick={() => openPrerequisites(topic)}>Prerequisites</button>
                          <button type="button" onClick={() => beginTopicEdit(topic)}>Edit</button>
                          <button className="danger" type="button" disabled={busy} onClick={() => deleteTopic(topic)}>Delete</button>
                        </div>
                      </article>
                    ))}
                  </div>
                )}
                {selectedPrerequisiteTopic && (
                  <form className="prerequisite-editor" onSubmit={savePrerequisites}>
                    <div className="admin-panel-heading">
                      <div>
                        <p className="eyebrow">DEPENDENCY GRAPH</p>
                        <h3>Requirements for {selectedPrerequisiteTopic.name}</h3>
                      </div>
                      <button className="admin-text-button" type="button" onClick={() => setPrerequisiteTopicId('')}>Close</button>
                    </div>
                    <fieldset>
                      <legend>Required topics</legend>
                      {topics.filter((topic) => topic.id !== prerequisiteTopicId).map((topic) => (
                        <label className="prerequisite-option" key={topic.id}>
                          <input
                            type="checkbox"
                            checked={prerequisiteIds.includes(topic.id)}
                            onChange={(event) => setPrerequisiteIds((current) => (
                              event.target.checked
                                ? [...current, topic.id]
                                : current.filter((id) => id !== topic.id)
                            ))}
                          />
                          <span>{topic.name}</span>
                          <small>{topic.difficulty}</small>
                        </label>
                      ))}
                      {topics.length < 2 && <p className="admin-hint">Add another topic to create a prerequisite relationship.</p>}
                    </fieldset>
                    <button className="admin-submit" type="submit" disabled={busy}>
                      {busy ? 'Saving…' : 'Save prerequisites'}
                    </button>
                  </form>
                )}
              </section>
            </div>
          )}
        </div>
      </section>
    </PageShell>
  )
}
