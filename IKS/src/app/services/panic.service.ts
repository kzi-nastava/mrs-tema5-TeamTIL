import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';
import { PanicAlertResponse, BackendPanicRequest, BackendPanicResponse } from '../models/panic.model';

@Injectable({ providedIn: 'root' })
export class PanicService {
  private apiUrl = `${environment.apiUrl}/panic`;

  constructor(private http: HttpClient, private authService: AuthService) {}

  /**
   * Trigger/Report a panic alert using backend API
   * Backend expects: rideId, locationId, userType, accountEmail
   */
  triggerPanic(payload: BackendPanicRequest): Observable<BackendPanicResponse> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.post<BackendPanicResponse>(this.apiUrl, payload, { headers });
  }

  /**
   * Mark panic alert as handled
   */
  markPanicAsHandled(panicId: string): Observable<void> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.put<void>(`${this.apiUrl}/${panicId}/handle`, {}, { headers });
  }

  /**
   * Get all panic alerts
   */
  getPanicAlerts(): Observable<PanicAlertResponse[]> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<PanicAlertResponse[]>(this.apiUrl, { headers });
  }

  /**
   * Get unhandled panic alerts
   */
  getUnhandledPanicAlerts(): Observable<PanicAlertResponse[]> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<PanicAlertResponse[]>(`${this.apiUrl}/unhandled`, { headers });
  }

  /**
   * Get panic alert details by ID
   */
  getPanicAlert(panicId: string): Observable<PanicAlertResponse> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<PanicAlertResponse>(`${this.apiUrl}/${panicId}`, { headers });
  }
}
