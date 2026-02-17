import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RideDetailsResponseDTO, RideCreatedResponseDTO, RideRequestDTO } from '../../models/ride-dto.model';
import { RideStatsResponseDTO } from '../../models/ride-stats.model';

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

  getDriverRideHistory(driverEmail: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/driver/${driverEmail}/history`);
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

  startRide(rideId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${rideId}/start`, {});
  }

  rateRide(rideId: number, ratingData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/${rideId}/rate`, ratingData);
  }

  getUserStats(email: string, dateFrom?: string, dateTo?: string): Observable<RideStatsResponseDTO> {
    let params = '';
    if (dateFrom) params += `?dateFrom=${dateFrom}`;
    if (dateTo)   params += `${params ? '&' : '?'}dateTo=${dateTo}`;
    return this.http.get<RideStatsResponseDTO>(`${this.apiUrl}/stats/user/${email}${params}`);
  }

  getDriverStats(email: string, dateFrom?: string, dateTo?: string): Observable<RideStatsResponseDTO> {
    let params = '';
    if (dateFrom) params += `?dateFrom=${dateFrom}`;
    if (dateTo)   params += `${params ? '&' : '?'}dateTo=${dateTo}`;
    return this.http.get<RideStatsResponseDTO>(`${this.apiUrl}/stats/driver/${email}${params}`);
  }

  getAdminStats(role: string, filterEmail: string, dateFrom?: string, dateTo?: string): Observable<RideStatsResponseDTO> {
    let params = `?role=${role}`;
    if (filterEmail) params += `&filterEmail=${filterEmail}`;
    if (dateFrom)    params += `&dateFrom=${dateFrom}`;
    if (dateTo)      params += `&dateTo=${dateTo}`;
    return this.http.get<RideStatsResponseDTO>(`${this.apiUrl}/stats/admin${params}`);
  }

}