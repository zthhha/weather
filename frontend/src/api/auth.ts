import client, { request } from './client'
import type { AuthenticationResponse, CurrentUser, Credentials } from '@/types/auth'

export const authApi = {
  register: (payload: Credentials) => request<AuthenticationResponse>(client.post('/api/v1/auth/register', payload)),
  login: (payload: Credentials) => request<AuthenticationResponse>(client.post('/api/v1/auth/login', payload)),
  me: () => request<CurrentUser>(client.get('/api/v1/auth/me')),
}
