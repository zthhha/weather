export interface Credentials {
  username: string
  password: string
}

export interface AuthenticationResponse {
  accessToken: string
  tokenType: string
  roles: string[]
}

export interface CurrentUser {
  id: number
  username: string
  roles: string[]
}
