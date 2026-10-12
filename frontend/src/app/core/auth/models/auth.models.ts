export interface LoginRequest {
    usernameOrEmail: string;
    password: string;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  id: number;
  username: string;
  email: string;
  rol: string; 
  expiresIn: number;
}

export interface UserSession {
  id: number;
  username: string;
  email: string;
  rol: string;
}