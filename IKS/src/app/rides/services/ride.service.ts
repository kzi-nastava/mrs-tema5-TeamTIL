import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RideDetailsResponseDTO } from '../../models/ride-dto.model';

@Injectable({ providedIn: 'root' })
export class RideService {
  private apiUrl = 'http://localhost:8080/api/rides';

  constructor(private http: HttpClient) {}

  getAdminRideHistory(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8080/api/rides/admin/history');
  }

  stopRide(rideId: number, stopRequest: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${rideId}/stop`, stopRequest);
  }

  // Fallback - sve voznje vozača (za history stranice i fallback logiku)
  getAssignedRides(driverEmail: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/driver/history?driverEmail=${driverEmail}`);
  }

  // Koristi /assigned endpoint koji već vraća samo aktivne voznje (IN_PROGRESS, REQUESTED)
  getActiveAssignedRides(driverEmail: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/assigned?driverEmail=${driverEmail}`);
  }

  // Fallback - sve voznje korisnika (za history stranice i fallback logiku)
  getUserRides(userEmail: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${userEmail}/history`);
  }

  // Koristi /user/{email} endpoint koji već vraća samo aktivne voznje (IN_PROGRESS, REQUESTED)
  getActiveUserRides(userEmail: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/user/${userEmail}`);
  }

  cancelRide(rideId: number, cancellationReason: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${rideId}/cancel`, { cancellationReason });
  }

  endRide(rideId: number, endRideData: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${rideId}/end`, endRideData);
  }

  getRideDetails(rideId: number): Observable<RideDetailsResponseDTO> {
    const url = `${this.apiUrl}/${rideId}/details`;
    console.log('[RideService] Calling getRideDetails with URL:', url);
    return this.http.get<RideDetailsResponseDTO>(url);
  }
}
