import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import RoadmapPage from '../roadmap/RoadmapPage.jsx'
import { AuthContext } from '../auth/AuthContext.js'
import * as client from '../api/client.js'

function renderRoadmap() {
  const mockAuth = {
    user: { id: 'u-1', name: 'Tester', role: 'USER' },
    token: 'jwt-roadmap',
    loading: false,
    logout: vi.fn(),
  }
  return render(
    <AuthContext.Provider value={mockAuth}>
      <MemoryRouter>
        <RoadmapPage />
      </MemoryRouter>
    </AuthContext.Provider>
  )
}

describe('RoadmapPage Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('renders loading indicator while roadmap is loading', () => {
    vi.spyOn(client, 'apiRequest').mockImplementation(() => new Promise(() => {}))

    renderRoadmap()
    expect(screen.getByTestId('roadmap-loading')).toBeInTheDocument()
    expect(screen.getByText('Mapping your current progress…')).toBeInTheDocument()
  })

  it('renders empty message when no roadmap topics exist', async () => {
    vi.spyOn(client, 'apiRequest').mockResolvedValue({ topics: [] })

    renderRoadmap()

    await waitFor(() => {
      expect(screen.getByTestId('roadmap-empty')).toBeInTheDocument()
    })
    expect(screen.getByText('No topics are available in the roadmap yet.')).toBeInTheDocument()
  })

  it('renders error state and retries on failure', async () => {
    let callCount = 0
    vi.spyOn(client, 'apiRequest').mockImplementation(() => {
      callCount++
      if (callCount === 1) {
        return Promise.reject(new Error('Roadmap service failure'))
      }
      return Promise.resolve({
        topics: [
          {
            topicId: 'top-1',
            topicName: 'Arrays & Hashing',
            difficulty: 'BEGINNER',
            prerequisites: [],
            level: 0,
            totalProblems: 5,
            problemsSolved: 2,
            problemsAttempted: 3,
            accuracy: 66.7,
            progressPercent: 40,
            status: 'IN_PROGRESS',
            recommended: true,
            nextStepReason: 'Continue practicing beginner problems.',
          },
        ],
      })
    })

    renderRoadmap()

    await waitFor(() => {
      expect(screen.getByTestId('roadmap-error')).toBeInTheDocument()
    })
    expect(screen.getByText('Roadmap service failure')).toBeInTheDocument()

    const retryBtn = screen.getByRole('button', { name: /retry roadmap/i })
    fireEvent.click(retryBtn)

    await waitFor(() => {
      expect(screen.getByText('Arrays & Hashing')).toBeInTheDocument()
    })
    expect(screen.getByTestId('roadmap-board')).toBeInTheDocument()
  })
})
