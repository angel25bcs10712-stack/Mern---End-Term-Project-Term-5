import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { RecommendationsSection } from '../problems/ProblemsPages.jsx'
import * as client from '../api/client.js'

describe('RecommendationsSection Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('renders loading state initially', () => {
    vi.spyOn(client, 'apiRequest').mockImplementation(() => new Promise(() => {}))

    render(
      <MemoryRouter>
        <RecommendationsSection token="test-token" />
      </MemoryRouter>
    )

    expect(screen.getByTestId('recommendations-loading')).toBeInTheDocument()
    expect(screen.getByText('Finding a useful next step…')).toBeInTheDocument()
  })

  it('renders empty state when there are no recommendations', async () => {
    vi.spyOn(client, 'apiRequest').mockResolvedValue([])

    render(
      <MemoryRouter>
        <RecommendationsSection token="test-token" />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByTestId('recommendations-empty')).toBeInTheDocument()
    })
    expect(screen.getByText(/No open recommendations right now/i)).toBeInTheDocument()
  })

  it('renders error state on API failure with retry action', async () => {
    let attempts = 0
    vi.spyOn(client, 'apiRequest').mockImplementation(() => {
      attempts++
      if (attempts === 1) {
        return Promise.reject(new Error('Recommendation service down'))
      }
      return Promise.resolve([
        {
          problem: {
            id: 'prob-rec-1',
            title: 'Subarray Sum Equals K',
            difficulty: 'INTERMEDIATE',
          },
          reasonForRecommendation: 'Recommended because accuracy is 45%',
          recommendationScore: 82.5,
          topic: 'Arrays',
          difficulty: 'INTERMEDIATE',
        },
      ])
    })

    render(
      <MemoryRouter>
        <RecommendationsSection token="test-token" />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByTestId('recommendations-error')).toBeInTheDocument()
    })
    expect(screen.getByText('Recommendation service down')).toBeInTheDocument()

    const retryBtn = screen.getByRole('button', { name: /try again/i })
    fireEvent.click(retryBtn)

    await waitFor(() => {
      expect(screen.getByText('Subarray Sum Equals K')).toBeInTheDocument()
    })
    expect(screen.getByText('Recommended because accuracy is 45%')).toBeInTheDocument()
  })
})
