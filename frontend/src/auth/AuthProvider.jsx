import { useEffect, useState } from 'react'
import { AuthContext } from './AuthContext.js'
import { apiRequest } from '../api/client.js'

const TOKEN_KEY = 'coderoute-token'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY))
  const [loading, setLoading] = useState(() => Boolean(localStorage.getItem(TOKEN_KEY)))

  useEffect(() => {
    let active = true

    if (!token) return () => { active = false }

    apiRequest('/api/auth/me', { token })
      .then((currentUser) => {
        if (active) setUser(currentUser)
      })
      .catch(() => {
        localStorage.removeItem(TOKEN_KEY)
        if (active) {
          setToken(null)
          setUser(null)
        }
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => { active = false }
  }, [token])

  async function authenticate(path, credentials) {
    const result = await apiRequest(path, { method: 'POST', body: credentials })
    localStorage.setItem(TOKEN_KEY, result.token)
    setToken(result.token)
    setUser(result.user)
    setLoading(false)
  }

  function login(credentials) {
    return authenticate('/api/auth/login', credentials)
  }

  function register(credentials) {
    return authenticate('/api/auth/register', credentials)
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY)
    setToken(null)
    setUser(null)
  }

  function updateUser(patch) {
    setUser((previous) => (previous ? { ...previous, ...patch } : previous))
  }

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  )
}