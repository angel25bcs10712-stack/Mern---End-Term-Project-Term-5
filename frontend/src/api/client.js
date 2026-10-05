export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export class ApiError extends Error {
  constructor(message, status = 500, details = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

export async function apiRequest(path, { token, method = 'GET', body, headers = {} } = {}) {
  let response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers: {
        ...(body ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...headers,
      },
      ...(body ? { body: JSON.stringify(body) } : {}),
    })
  } catch (networkError) {
    throw new ApiError(
      'Cannot reach the CodeRoute API. Start the backend and check its database and JWT settings.',
      0,
      networkError
    )
  }

  if (response.status === 204) {
    return null
  }

  const result = await response.json().catch(() => ({}))

  if (!response.ok) {
    const message = result?.message || (
      response.status === 401
        ? 'Your session has expired or you are unauthorized. Please sign in again.'
        : response.status === 403
        ? 'You do not have permission to access this resource.'
        : response.status === 404
        ? 'Requested resource was not found.'
        : response.status === 503
        ? 'Service is temporarily unavailable. Please try again shortly.'
        : 'Something went wrong. Please try again.'
    )
    throw new ApiError(message, response.status, result)
  }

  return result
}
