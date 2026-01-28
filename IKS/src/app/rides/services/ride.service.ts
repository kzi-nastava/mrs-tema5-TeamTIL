import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RideService {
      getAdminRideHistory(): Observable<any[]> {
        return this.http.get<any[]>('http://localhost:8080/api/rides/admin/history');
      }
    stopRide(rideId: number, stopRequest: any): Observable<any> {
      return this.http.put(`${this.apiUrl}/${rideId}/stop`, stopRequest);
    }
  private apiUrl = 'http://localhost:8080/api/rides';

  constructor(private http: HttpClient) {}

  getAssignedRides(driverEmail: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/assigned?driverEmail=${driverEmail}`);
  }

  // Nova metoda za korisnika
  getUserRides(userEmail: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/user/${userEmail}`);
  }
  cancelRide(rideId: number, cancellationReason: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${rideId}/cancel`, { cancellationReason });
  }
  endRide(rideId: number, endRideData: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${rideId}/end`, endRideData);
  }
}
