import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ActiveRideAdminDTO } from '../models/active-ride-dto.model';

@Injectable({
  providedIn: 'root'
})
export class ActiveRidesService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getActiveRides(): Observable<ActiveRideAdminDTO[]> {
    return this.http.get<ActiveRideAdminDTO[]>(`${this.apiUrl}/rides/admin/active`);
  }
}