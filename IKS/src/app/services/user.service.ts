import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users/my-profile`;

  constructor(
    private http: HttpClient,
    private authService: AuthService 
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken(); 
    
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

    getMyProfile(): Observable<any> {
        return this.http.get(this.apiUrl, { headers: this.getHeaders() });
    }

    updateMyProfile(userData: any): Observable<any> {
        return this.http.put(this.apiUrl, userData, { headers: this.getHeaders() });
    }

    hasToken() { 
        return !!this.authService.getToken(); 
    }
}