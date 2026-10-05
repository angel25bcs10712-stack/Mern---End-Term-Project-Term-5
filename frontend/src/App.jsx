import { Link, Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/AuthContext.js'
import { AuthProvider } from './auth/AuthProvider.jsx'
import { AccountPage, AuthPage, ProtectedRoute } from './auth/AuthPages.jsx'
import AdminPage from './admin/AdminPage.jsx'
import { ProblemDetailPage, ProblemsPage } from './problems/ProblemsPages.jsx'
import RoadmapPage from './roadmap/RoadmapPage.jsx'
import { AppHeader } from './ui/AppChrome.jsx'

function LandingPage() {
  const { user } = useAuth()
  return (
    <main>
      <AppHeader variant="marketing" />

      <section className="hero" aria-labelledby="hero-title">
        <div className="hero-copy">
          <p className="eyebrow"><span className="eyebrow-dot" /> A more considered way to prepare</p>
          <h1 id="hero-title">Make every<br />practice session<br /><span>move you forward.</span></h1>
          <p className="hero-description">
            CodeRoute helps interview candidates turn DSA practice into a clearer personal path—with progress, prerequisites, and a useful next problem.
          </p>
          <div className="hero-actions">
            {user ? (
              <>
                <Link className="primary-link" to="/problems">Continue practicing <span aria-hidden="true">→</span></Link>
                <Link className="secondary-link" to="/roadmap">View roadmap</Link>
              </>
            ) : (
              <>
                <Link className="primary-link" to="/register">Start practicing <span aria-hidden="true">→</span></Link>
                <Link className="secondary-link" to="/login">Sign in</Link>
              </>
            )}
          </div>
          <p className="quiet-note">Adaptive DSA practice for developers preparing for interviews.</p>
        </div>

        <div className="route-art" aria-label="Illustration of a learning route connecting practice, reflection, and a next step" role="img">
          <div className="art-topline"><span>THE LEARNING LOOP</span><span>01 — 03</span></div>
          <div className="route-line" aria-hidden="true"><i /><i /><i /></div>
          <div className="route-node node-one"><span className="node-index">01</span><span className="node-mark mark-practice" aria-hidden="true">&lt;/&gt;</span><strong>Practice</strong><small>Work through a problem</small></div>
          <div className="route-node node-two"><span className="node-index">02</span><span className="node-mark mark-reflect" aria-hidden="true">↗</span><strong>Reflect</strong><small>Notice what got easier</small></div>
          <div className="route-node node-three"><span className="node-index">03</span><span className="node-mark mark-continue" aria-hidden="true">→</span><strong>Continue</strong><small>Choose a useful next step</small></div>
          <div className="art-caption"><span className="caption-rule" /> Progress is a route, not a streak.</div>
          <span className="art-coordinate coordinate-one" aria-hidden="true">DSA / FIELD NOTES</span>
          <span className="art-coordinate coordinate-two" aria-hidden="true">BUILD 001</span>
        </div>
      </section>

      <section className="approach-section" id="approach" aria-labelledby="approach-title">
        <div className="section-heading">
          <p className="eyebrow">THE IDEA, IN THREE PARTS</p>
          <h2 id="approach-title">A better next step starts<br />with a better signal.</h2>
        </div>
        <div className="principles" id="principles">
          <article className="principle">
            <span className="principle-number">01</span>
            <h3>Keep the context</h3>
            <p>Practice is more useful when your attempts, topics, and takeaways build a picture over time.</p>
          </article>
          <article className="principle">
            <span className="principle-number">02</span>
            <h3>Find the real gaps</h3>
            <p>A thoughtful study plan gives attention to the concepts that need another pass, not just the next item on a list.</p>
          </article>
          <article className="principle">
            <span className="principle-number">03</span>
            <h3>Make progress visible</h3>
            <p>Clear feedback can help turn scattered practice into a learning path you can actually follow.</p>
          </article>
        </div>
      </section>

      <footer className="site-footer">
        <span>CodeRoute</span>
        <span>Built for the long route to a confident solve.</span>
        <a href="#hero-title">Back to top ↑</a>
      </footer>
    </main>
  )
}

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<AuthPage mode="login" />} />
        <Route path="/register" element={<AuthPage mode="register" />} />
        <Route path="/account" element={<ProtectedRoute><AccountPage /></ProtectedRoute>} />
        <Route path="/admin" element={<ProtectedRoute><AdminPage /></ProtectedRoute>} />
        <Route path="/problems" element={<ProtectedRoute><ProblemsPage /></ProtectedRoute>} />
        <Route path="/problems/:id" element={<ProtectedRoute><ProblemDetailPage /></ProtectedRoute>} />
        <Route path="/roadmap" element={<ProtectedRoute><RoadmapPage /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}

export default App
