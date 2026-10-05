import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { ProblemDetailPage } from '../problems/ProblemsPages.jsx'
import { AuthContext } from '../auth/AuthContext.js'
import * as client from '../api/client.js'

function renderProblemDetail(problemId = 'test-prob-1') {
  const mockAuth = {
    user: { id: 'u-1', name: 'Tester', email: 'test@example.com' },
    token: 'jwt-123',
    loading: false,
    logout: vi.fn(),
  }
  return render(
    <AuthContext.Provider value={mockAuth}>
      <MemoryRouter initialEntries={[`/problems/${problemId}`]}>
        <Routes>
          <Route path="/problems/:id" element={<ProblemDetailPage />} />
          <Route path="/problems" element={<div>Problems Library</div>} />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>
  )
}

describe('ProblemDetailPage Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('renders loading state when problem is being fetched', () => {
    vi.spyOn(client, 'apiRequest').mockImplementation(() => new Promise(() => {}))

    renderProblemDetail()
    expect(screen.getByTestId('problem-loading')).toBeInTheDocument()
    expect(screen.getByText('Loading problem…')).toBeInTheDocument()
  })

  it('renders error state when problem is not found (404)', async () => {
    vi.spyOn(client, 'apiRequest').mockRejectedValue(new Error('Problem not found'))

    renderProblemDetail('invalid-uuid')

    await waitFor(() => {
      expect(screen.getByTestId('problem-error')).toBeInTheDocument()
    })
    expect(screen.getByText('Problem not found')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /try again/i })).toBeInTheDocument()
  })

  it('validates attempt inputs client-side before sending API request', async () => {
    const mockProblem = {
      id: 'test-prob-1',
      title: 'Invert Binary Tree',
      description: 'Invert the tree left-to-right.',
      difficulty: 'BEGINNER',
      topicName: 'Trees',
      tags: ['trees', 'recursion'],
      estimatedTimeMinutes: 20,
      solved: false,
    }
    const apiSpy = vi.spyOn(client, 'apiRequest').mockResolvedValue(mockProblem)

    renderProblemDetail('test-prob-1')

    await waitFor(() => {
      expect(screen.getByText('Invert Binary Tree')).toBeInTheDocument()
    })

    // Enter negative time
    const timeInput = screen.getByLabelText(/time taken/i)
    fireEvent.change(timeInput, { target: { value: '-10' } })

    const solveBtn = screen.getByRole('button', { name: /mark solved/i })
    fireEvent.click(solveBtn)

    expect(screen.getByTestId('attempt-action-error')).toHaveTextContent(
      'Time taken must be zero or a positive number.'
    )
    // Ensure no attempt API call was made
    expect(apiSpy).toHaveBeenCalledTimes(1) // only initial fetch
  })

  it('records attempt and displays confirmation notice', async () => {
    const mockProblem = {
      id: 'test-prob-1',
      title: 'Invert Binary Tree',
      description: 'Invert the tree left-to-right.',
      difficulty: 'BEGINNER',
      topicName: 'Trees',
      tags: ['trees'],
      estimatedTimeMinutes: 20,
      solved: false,
    }
    vi.spyOn(client, 'apiRequest').mockImplementation((path, options) => {
      if (options?.method === 'POST') {
        return Promise.resolve({
          id: 'attempt-1',
          status: 'SOLVED',
          timeTakenSeconds: 300,
          attempts: 1,
        })
      }
      return Promise.resolve(mockProblem)
    })

    renderProblemDetail('test-prob-1')

    await waitFor(() => {
      expect(screen.getByText('Invert Binary Tree')).toBeInTheDocument()
    })

    const timeInput = screen.getByLabelText(/time taken/i)
    fireEvent.change(timeInput, { target: { value: '300' } })

    const solveBtn = screen.getByRole('button', { name: /mark solved/i })
    fireEvent.click(solveBtn)

    await waitFor(() => {
      expect(screen.getByTestId('attempt-notice')).toHaveTextContent(
        'Solved status recorded. Nice work.'
      )
    })
  })
})
