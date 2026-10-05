import { describe, it, expect, vi, beforeEach } from 'vitest'
import { apiRequest, ApiError } from '../api/client.js'

describe('ApiClient', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('includes Authorization header when token is provided', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ success: true }),
    })
    globalThis.fetch = mockFetch

    const result = await apiRequest('/api/test', { token: 'mock-jwt-token' })
    expect(result).toEqual({ success: true })
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/test'),
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: 'Bearer mock-jwt-token',
        }),
      })
    )
  })

  it('handles 204 No Content returning null', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 204,
    })

    const result = await apiRequest('/api/admin/problems/1', { method: 'DELETE' })
    expect(result).toBeNull()
  })

  it('handles backend ApiError payload on 400 Bad Request', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      json: async () => ({ message: 'timeTakenSeconds: must be greater than or equal to 0' }),
    })

    await expect(apiRequest('/api/problems/1/attempt', { method: 'POST', body: {} }))
      .rejects.toThrow('timeTakenSeconds: must be greater than or equal to 0')
  })

  it('handles 401 Unauthorized with descriptive fallback message', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      json: async () => ({}),
    })

    try {
      await apiRequest('/api/problems')
      expect.unreachable('Should have thrown an error')
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError)
      expect(err.status).toBe(401)
      expect(err.message).toBe('Your session has expired or you are unauthorized. Please sign in again.')
    }
  })

  it('handles 403 Forbidden with descriptive message', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 403,
      json: async () => ({}),
    })

    await expect(apiRequest('/api/admin/stats')).rejects.toThrow(
      'You do not have permission to access this resource.'
    )
  })

  it('handles network failure with helpful API down message', async () => {
    globalThis.fetch = vi.fn().mockRejectedValue(new Error('Failed to fetch'))

    try {
      await apiRequest('/api/problems')
      expect.unreachable('Should have thrown an error')
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError)
      expect(err.status).toBe(0)
      expect(err.message).toContain('Cannot reach the CodeRoute API')
    }
  })
})
