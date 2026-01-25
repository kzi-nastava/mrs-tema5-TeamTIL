// Request DTOs
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  surname: string;
  email: string;
  password: string;
  phoneNumber: string;
  city: string;
  userType: string; // "REGISTERED_USER"
  profilePhoto?: string; // Base64 string, optional
}

// Response DTOs
export interface LoginResponse {
  token: string;
  userType: string; // "DRIVER" | "REGISTERED_USER" | "ADMINISTRATOR"
  email: string;
  message: string;
}

export interface RegisterResponse {
  email: string;
  userType: string;
  message: string;
}

// User info stored in localStorage
export interface CurrentUser {
  token: string;
  userType: string;
  email: string;
}
