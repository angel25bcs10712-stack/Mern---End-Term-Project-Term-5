import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { AuthPage } from '../auth/AuthPages.jsx'
import { AuthContext } from '../auth/AuthContext.js'

function renderAuth(mode = 'login', authOverrides = {}) {
  const mockAuth = {
    user: null,
    token: null,
    loading: false,
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    ...authOverrides,
  }
  return {
    mockAuth,
    ...render(
      <AuthContext.Provider value={mockAuth}>
        <MemoryRouter>
          <AuthPage mode={mode} />
        </MemoryRouter>
      </AuthContext.Provider>
    ),
  }
}

describe('AuthPages Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('renders sign in form with required fields', () => {
    renderAuth('login')
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument()
  })

  it('handles invalid login and displays error message', async () => {
    const loginMock = vi.fn().mockRejectedValue(new Error('Email or password is incorrect'))
    renderAuth('login', { login: loginMock })

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'wrong@example.com' } })
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'WrongPass123' } })

    fireEvent.click(screen.getByRole('button', { name: /sign in/i }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Email or password is incorrect')
    })
  })

  it('handles duplicate registration and displays conflict error', async () => {
    const registerMock = vi.fn().mockRejectedValue(new Error('An account with this email already exists.'))
    renderAuth('register', { register: registerMock })

    fireEvent.change(screen.getByLabelText(/name/i), { target: { value: 'Ada Lovelace' } })
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'existing@example.com' } })
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'RoutePass123' } })

    fireEvent.click(screen.getByRole('button', { name: /create account/i }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('An account with this email already exists.')
    })
  })
})
