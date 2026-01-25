import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  CurrentUser
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
          this.currentUserSubject.next(user);
        })
      );
  }

  register(data: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/auth/register`, data);
  }

  logout(): void {
    localStorage.removeItem('currentUser');
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
}
