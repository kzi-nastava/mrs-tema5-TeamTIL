import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DriverService {
  private apiUrl = `${environment.apiUrl}/drivers`;

  constructor(private http: HttpClient, private authService: AuthService) {}

  getDriverProfile(): Observable<any> {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.getToken()}`);
    return this.http.get(`${this.apiUrl}/my-profile`, { headers });
  }

  updateDriverProfile(data: any): Observable<any> {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.getToken()}`);
    return this.http.put(`${this.apiUrl}/my-profile`, data, { headers });
  }
}