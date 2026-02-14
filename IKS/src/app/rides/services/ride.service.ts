import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RideDetailsResponseDTO, RideCreatedResponseDTO, RideRequestDTO } from '../../models/ride-dto.model';

@Injectable({ providedIn: 'root' })
export class RideService {
  private apiUrl = 'http://localhost:8080/api/rides';

  constructor(private http: HttpClient) {}

  createRide(request: RideRequestDTO): Observable<RideCreatedResponseDTO> {
    return this.http.post<RideCreatedResponseDTO>(`${this.apiUrl}`, request);
  }

  getAdminRideHistory(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8080/api/rides/admin/history');
  }

  stopRide(rideId: number, stopRequest: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${rideId}/stop`, stopRequest);
  }

  getAssignedRides(driverEmail: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/driver/history?driverEmail=${driverEmail}`);
  }

  getActiveAssignedRides(driverEmail: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/assigned?driverEmail=${driverEmail}`);
  }

  getUserRides(userEmail: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${userEmail}/history`);
  }

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
    return this.http.get<RideDetailsResponseDTO>(`${this.apiUrl}/${rideId}/details`);
  }
}