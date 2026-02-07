import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  CurrentUser,
  TokenValidationResponse
} from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiUrl;
  private currentUserSubject = new BehaviorSubject<CurrentUser | null>(this.getCurrentUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();
  private userProfileSource = new BehaviorSubject<any>(null);
userProfile$ = this.userProfileSource.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}


  sendPasswordResetEmail(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/forgot-password`, { email: email });
  }

  private oldPasswordTemp: string = '';

  setOldPassword(password: string) {
    this.oldPasswordTemp = password;
  }

  getOldPassword() {
    return this.oldPasswordTemp;
  }

  changePassword(oldPassword: string, newPassword: string): Observable<any> {
  const token = this.getToken();
  
  const headers = new HttpHeaders({
    'Authorization': `Bearer ${token}`
  });

  return this.http.put(`${this.apiUrl}/users/change-password`, {
    oldPassword: oldPassword,
    newPassword: newPassword
  }, { 
    headers: headers, 
    responseType: 'text' as 'json' 
  });
}

  private getCurrentUserFromStorage(): CurrentUser | null {
    const userJson = localStorage.getItem('currentUser');
    return userJson ? JSON.parse(userJson) : null;
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          console.log('Backend login response:', response);
          const user: CurrentUser = {
            token: response.token,
            userType: response.userType,
            email: response.email,
            name: response.name || response.email?.split('@')[0] || 'User',
            profilePictureUrl: response.profilePictureUrl
          };
          console.log('Saving user to localStorage:', user);
          localStorage.setItem('currentUser', JSON.stringify(user));
          localStorage.setItem('token', response.token); // Dodaj token posebno
          this.currentUserSubject.next(user);
        })
      );
  }

  register(data: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/auth/register`, data);
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token'); // Ukloni token iz localStorage
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return this.currentUserSubject.value !== null;
  }

  getToken(): string | null {
    return this.currentUserSubject.value?.token || null;
  }

  getUserType(): string | null {
    return this.currentUserSubject.value?.userType || null;
  }

  getEmail(): string | null {
    return this.currentUserSubject.value?.email || null;
  }

  getName(): string | null {
    return this.currentUserSubject.value?.name || null;
  }

  getProfilePictureUrl(): string | null {
    return this.currentUserSubject.value?.profilePictureUrl || null;
  }

  validateToken(): Observable<TokenValidationResponse> {
    const token = this.getToken();
    if (!token) {
      console.log('[AuthService] No token found');
      return of({ valid: false, message: 'No token found' });
    }

    console.log('[AuthService] Validating token...');
    return this.http.get<TokenValidationResponse>(`${this.apiUrl}/auth/validate`)
      .pipe(
        tap(response => {
          console.log('[AuthService] Token validation response:', response);
          if (!response.valid) {
            console.log('[AuthService] Token is invalid, logging out...');
            this.logout();
          }
        }),
        catchError(error => {
          console.error('[AuthService] Token validation failed:', error);
          this.logout();
          return of({ valid: false, message: 'Token validation failed' });
        })
      );
  }

  updateUserProfile(user: any) {
  this.userProfileSource.next(user);
}

updateUser(user: any) {
  const currentData = this.currentUserSubject.value;
  
  if (currentData) {
    // pravimo novi objekat gde je sve isto, samo azuriramo sliku
    const updatedUser: CurrentUser = {
      ...currentData,
      profilePictureUrl: user.profilePictureUrl
    };

    // javljamo Navbaru
    this.currentUserSubject.next(updatedUser);

    localStorage.setItem('currentUser', JSON.stringify(updatedUser));
  }
}

activateDriverAccount(token: string, newPassword: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/drivers/activate`, {
    token: token,
    newPassword: newPassword
  }, { responseType: 'text' as 'json' }); 
}
}
