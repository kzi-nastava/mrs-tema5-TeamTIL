import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { RideTrackingDTO } from '../../models/ride-tracking-dto.model';
import { environment } from '../../../environments/environment.development';

@Injectable({ providedIn: 'root' })
export class RideTrackingService {
  public liveRideInfo$ = new Subject<[number, number, number, number]>();
  private socket?: WebSocket;

  private apiUrl = environment.apiUrl + '/rides';
  

  constructor(private http: HttpClient) { }

  getRideTracking(rideId: number): Observable<RideTrackingDTO> {
    return this.http.get<RideTrackingDTO>(`${this.apiUrl}/${rideId}/tracking`);
  }

  connectToRideTracking(rideId: number) {
    this.socket = new WebSocket(`ws://localhost:8080/ws/ride-tracking`);

    this.socket.onopen = () => {
      console.log('WS connected');
      this.socket?.send(JSON.stringify({ rideId }));
    };

    this.socket.onmessage = (event) => {
      const data = JSON.parse(event.data);  
      this.liveRideInfo$.next([data.latitude, data.longitude, data.remainingDurationInMinutes, data.currentPrice]);
    };

    this.socket.onerror = (err) => {
      console.error('WS error', err);
    };

    this.socket.onclose = () => {
      console.log('WS closed');
    };
  }

  disconnect() {
    this.socket?.close();
  }
}
