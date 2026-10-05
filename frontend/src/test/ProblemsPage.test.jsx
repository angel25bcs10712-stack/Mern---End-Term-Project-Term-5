import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { ProblemsPage } from '../problems/ProblemsPages.jsx'
import { AuthContext } from '../auth/AuthContext.js'
import * as client from '../api/client.js'

function renderWithAuth(ui) {
  const mockAuth = {
    user: { id: 'user-1', name: 'Tester Sen', email: 'tester@example.com', role: 'USER' },
    token: 'test-token',
    loading: false,
    logout: vi.fn(),
  }
  return render(
    <AuthContext.Provider value={mockAuth}>
      <MemoryRouter>
        {ui}
      </MemoryRouter>
    </AuthContext.Provider>
  )
}

describe('ProblemsPage Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('displays loading state while fetching problems', () => {
    vi.spyOn(client, 'apiRequest').mockImplementation(() => new Promise(() => {}))

    renderWithAuth(<ProblemsPage />)
    expect(screen.getByTestId('problems-loading')).toBeInTheDocument()
    expect(screen.getByText('Loading problems…')).toBeInTheDocument()
  })

  it('displays empty state when catalog search yields no matches', async () => {
    vi.spyOn(client, 'apiRequest').mockImplementation((path) => {
      if (path.startsWith('/api/topics')) return Promise.resolve([])
      if (path.startsWith('/api/users/me/progress')) return Promise.resolve({ totalProblems: 10, problemsSolved: 0 })
      if (path.startsWith('/api/recommendations')) return Promise.resolve([])
      if (path.startsWith('/api/problems')) {
        return Promise.resolve({
          content: [],
          page: 0,
          size: 12,
          totalElements: 0,
          totalPages: 0,
        })
      }
      return Promise.reject(new Error('Unknown path'))
    })

    renderWithAuth(<ProblemsPage />)

    await waitFor(() => {
      expect(screen.getByTestId('problems-empty')).toBeInTheDocument()
    })
    expect(screen.getByText('NO MATCHES')).toBeInTheDocument()
  })

  it('displays error state on API failure and retries successfully', async () => {
    let callCount = 0
    vi.spyOn(client, 'apiRequest').mockImplementation((path) => {
      if (path.startsWith('/api/topics')) return Promise.resolve([])
      if (path.startsWith('/api/users/me/progress')) return Promise.resolve(null)
      if (path.startsWith('/api/recommendations')) return Promise.resolve([])
      if (path.startsWith('/api/problems')) {
        callCount++
        if (callCount === 1) {
          return Promise.reject(new Error('Database service is temporarily unavailable'))
        }
        return Promise.resolve({
          content: [{
            id: 'prob-1',
            title: 'Two Sum Classic',
            description: 'Find two indices that sum to target.',
            difficulty: 'BEGINNER',
            topicId: 't-1',
            topicName: 'Arrays',
            tags: ['arrays'],
            estimatedTimeMinutes: 15,
            solved: false,
          }],
          page: 0,
          size: 12,
          totalElements: 1,
          totalPages: 1,
        })
      }
      return Promise.reject(new Error('Unknown path'))
    })

    renderWithAuth(<ProblemsPage />)

    await waitFor(() => {
      expect(screen.getByTestId('problems-error')).toBeInTheDocument()
    })
    expect(screen.getByText('Database service is temporarily unavailable')).toBeInTheDocument()

    const retryBtn = screen.getByRole('button', { name: /retry loading problems/i })
    fireEvent.click(retryBtn)

    await waitFor(() => {
      expect(screen.getByText('Two Sum Classic')).toBeInTheDocument()
    })
  })
})
