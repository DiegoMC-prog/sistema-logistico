export interface LoginRequest {
  username: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  passwordTemporal: boolean;
}

export interface Perfil {
  id: number;
  username: string;
  nombre: string;
  email: string;
  passwordTemporal: boolean;
  roles: string[];
  permisos: string[];
}

export interface CambiarPasswordRequest {
  passwordActual: string;
  passwordNueva: string;
}
