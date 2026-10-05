import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.js'
import { apiRequest } from '../api/client.js'
import { AppHeader, EmptyState, PageShell, SkeletonCardGrid, StatusBanner } from '../ui/AppChrome.jsx'

const CARD_WIDTH = 260
const COLUMN_WIDTH = 320
const ROW_HEIGHT = 228
const BOARD_TOP = 50

function useCompactRoadmap() {
  const [compact, setCompact] = useState(false)

  useEffect(() => {
    if (typeof window.matchMedia !== 'function') return undefined
    const media = window.matchMedia('(max-width: 700px)')
    const sync = () => setCompact(media.matches)
    sync()
    media.addEventListener('change', sync)
    return () => media.removeEventListener('change', sync)
  }, [])

  return compact
}

function boardLayout(topics) {
  const maxLevel = Math.max(0, ...topics.map((topic) => topic.level))
  const columns = Array.from({ length: maxLevel + 1 }, (_, level) => topics.filter((topic) => topic.level === level))
  const positions = new Map()
  columns.forEach((column, level) => {
    column.forEach((topic, row) => {
      positions.set(topic.topicId, { x: level * COLUMN_WIDTH + 16, y: BOARD_TOP + row * ROW_HEIGHT })
    })
  })
  const rows = Math.max(1, ...columns.map((column) => column.length))
  return { columns, positions, width: (maxLevel + 1) * COLUMN_WIDTH, height: BOARD_TOP + rows * ROW_HEIGHT }
}

function TopicCardContent({ topic }) {
  const statusClass = topic.status.toLowerCase().replace('_', '-')
  return (
    <>
      <div className="roadmap-card-heading">
        <span className={`roadmap-status-pill ${statusClass}`}>{topic.status.replace('_', ' ')}</span>
        <span className={`difficulty-tag ${topic.difficulty.toLowerCase()}`}>{topic.difficulty}</span>
      </div>
      <h2>{topic.topicName}</h2>
      <p className="roadmap-topic-count">{topic.problemsSolved} / {topic.totalProblems} problems solved</p>
      <div className="progress-track"><span style={{ width: `${topic.progressPercent}%` }} /></div>
      {topic.prerequisites.length > 0 && (
        <p className="roadmap-prerequisites">
          <span>NEEDS</span> {topic.prerequisites.map((item) => item.topicName).join(' + ')}
        </p>
      )}
      <p className="roadmap-reason">{topic.nextStepReason}</p>
      {topic.status !== 'LOCKED' && (
        <Link className="roadmap-open-link" to={`/problems?topicId=${topic.topicId}`}>
          {topic.status === 'COMPLETED' ? 'Review problems' : 'Open problems'} <span aria-hidden="true">→</span>
        </Link>
      )}
    </>
  )
}

export default function RoadmapPage() {
  const { token } = useAuth()
  const compact = useCompactRoadmap()
  const [roadmap, setRoadmap] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadTrigger, setReloadTrigger] = useState(0)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    apiRequest('/api/users/me/roadmap', { token })
      .then((data) => { if (active) setRoadmap(data) })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [token, reloadTrigger])

  const completedCount = roadmap?.topics.filter((topic) => topic.status === 'COMPLETED').length || 0
  const board = roadmap ? boardLayout(roadmap.topics) : null

  return (
    <PageShell className="problems-shell roadmap-shell">
      <AppHeader variant="app" />

      <section className="roadmap-main">
        <div className="roadmap-title-row">
          <div>
            <p className="eyebrow"><span className="eyebrow-dot" /> YOUR LEARNING PATH</p>
            <h1>Build the foundations.<br /><em>Then go further.</em></h1>
            <p className="roadmap-intro">Your next topics unlock as you complete their prerequisites.</p>
          </div>
          {roadmap && (
            <div className="roadmap-summary">
              <strong>{completedCount}<small> / {roadmap.topics.length}</small></strong>
              <span>TOPICS COMPLETE</span>
            </div>
          )}
        </div>

        <div className="roadmap-legend" aria-label="Roadmap statuses">
          {['LOCKED', 'AVAILABLE', 'IN_PROGRESS', 'COMPLETED'].map((status) => (
            <span key={status}>
              <i className={`roadmap-status-dot ${status.toLowerCase()}`} />
              {status.replace('_', ' ')}
            </span>
          ))}
        </div>

        {loading && (
          <SkeletonCardGrid count={3} testId="roadmap-loading" label="Mapping your current progress…" />
        )}
        {error && (
          <StatusBanner
            tone="error"
            testId="roadmap-error"
            action={(
              <button type="button" onClick={() => { setLoading(true); setError(''); setReloadTrigger((t) => t + 1) }} className="retry-button">
                Retry roadmap
              </button>
            )}
          >
            <p>{error}</p>
          </StatusBanner>
        )}
        {!loading && !error && roadmap?.topics.length === 0 && (
          <EmptyState testId="roadmap-empty" eyebrow="EMPTY PATH" title="No topics yet">
            No topics are available in the roadmap yet.
          </EmptyState>
        )}
        {roadmap && board && !loading && !error && (
          compact ? (
            <div className="roadmap-mobile-list is-visible" aria-label="Roadmap topics" data-testid="roadmap-board">
              {roadmap.topics
                .slice()
                .sort((a, b) => a.level - b.level || a.topicName.localeCompare(b.topicName))
                .map((topic) => (
                  <article
                    className={`roadmap-topic-card roadmap-mobile-card ${topic.status.toLowerCase().replace('_', '-')}`}
                    key={topic.topicId}
                  >
                    <p className="roadmap-topic-count" style={{ marginBottom: 8 }}>Stage {String(topic.level + 1).padStart(2, '0')}</p>
                    <TopicCardContent topic={topic} />
                  </article>
                ))}
            </div>
          ) : (
            <div className="roadmap-viewport" aria-busy={loading} data-testid="roadmap-board">
              <div className="roadmap-board" style={{ width: board.width, height: board.height }}>
                <svg className="roadmap-edges" width={board.width} height={board.height} viewBox={`0 0 ${board.width} ${board.height}`} aria-hidden="true">
                  <defs>
                    <marker id="roadmap-arrow" markerWidth="8" markerHeight="8" refX="7" refY="4" orient="auto">
                      <path d="M0,0 L8,4 L0,8 z" fill="#91a18f" />
                    </marker>
                  </defs>
                  {roadmap.topics.flatMap((topic) => topic.prerequisites.map((prerequisite) => {
                    const from = board.positions.get(prerequisite.topicId)
                    const to = board.positions.get(topic.topicId)
                    if (!from || !to) return null
                    const startX = from.x + CARD_WIDTH
                    const startY = from.y + 99
                    const endX = to.x
                    const endY = to.y + 99
                    const curve = Math.max(30, (endX - startX) / 2)
                    return (
                      <path
                        key={`${prerequisite.topicId}-${topic.topicId}`}
                        className="roadmap-edge"
                        d={`M ${startX} ${startY} C ${startX + curve} ${startY}, ${endX - curve} ${endY}, ${endX} ${endY}`}
                        markerEnd="url(#roadmap-arrow)"
                      />
                    )
                  }))}
                </svg>

                {board.columns.map((column, level) => (
                  <div className="roadmap-level-label" key={level} style={{ left: level * COLUMN_WIDTH + 16 }}>
                    STAGE {String(level + 1).padStart(2, '0')}
                  </div>
                ))}

                {roadmap.topics.map((topic) => {
                  const position = board.positions.get(topic.topicId)
                  return (
                    <article
                      className={`roadmap-topic-card ${topic.status.toLowerCase().replace('_', '-')}`}
                      key={topic.topicId}
                      style={{ left: position.x, top: position.y, width: CARD_WIDTH }}
                    >
                      <TopicCardContent topic={topic} />
                    </article>
                  )
                })}
              </div>
            </div>
          )
        )}
      </section>
    </PageShell>
  )
}
