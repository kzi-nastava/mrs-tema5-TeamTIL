import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RideRequestDTO, RideHistoryDTO } from '../models/ride-dto.model';

@Injectable({
  providedIn: 'root'
})
export class RideService {
  // Spajamo base URL sa endpointom za voznje
  private apiUrl = `${environment.apiUrl}/rides`;

  constructor(private http: HttpClient) {}

  createRide(request: RideRequestDTO): Observable<RideHistoryDTO> {
    return this.http.post<RideHistoryDTO>(this.apiUrl, request);
  }
}