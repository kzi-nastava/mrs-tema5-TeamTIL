import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { RideTrackingDTO } from '../../models/ride-tracking-dto.model';
import { environment } from '../../../environments/environment.development';

@Injectable({ providedIn: 'root' })
export class RideTrackingService {
  // Emits [lat, lng, remainingDurationMin, currentPrice] on each position update
  public liveRideInfo$ = new Subject<[number, number, number, number]>();
  // Emits when the ride has ended (driver called endRide/stopRide)
  public rideEnded$ = new Subject<void>();
  // Emits when trying to connect to a ride that isn't active
  public rideNotActive$ = new Subject<void>();

  private socket?: WebSocket;

  private readonly apiUrl = environment.apiUrl + '/rides';
  
  constructor(private http: HttpClient) { }

  getRideTracking(rideId: number): Observable<RideTrackingDTO> {
    return this.http.get<RideTrackingDTO>(`${this.apiUrl}/${rideId}/tracking`);
  }

  connectToRideTracking(rideId: number): void {
    // Close any existing connection first
    this.disconnect();

    this.socket = new WebSocket(`ws://localhost:8080/ws/ride-tracking`);

    this.socket.onopen = () => {
      console.log('[TrackingWS] Connected, subscribing to rideId:', rideId);
      this.socket?.send(JSON.stringify({ rideId }));
    };

    this.socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);

        if (data.type === 'RIDE_ENDED') {
          console.log('[TrackingWS] Ride ended signal received');
          this.rideEnded$.next();
          return;
        }

        if (data.type === 'RIDE_NOT_ACTIVE') {
          console.log('[TrackingWS] Ride not active');
          this.rideNotActive$.next();
          return;
        }

        this.liveRideInfo$.next([
          data.latitude,
          data.longitude,
          data.remainingDurationInMinutes,
          data.currentPrice
        ]);
      } catch (e) {
        console.error('[TrackingWS] Failed to parse message:', e);
      }
    };

    this.socket.onerror = (err) => {
      console.error('[TrackingWS] Error:', err);
    };

    this.socket.onclose = (event) => {
      console.log('[TrackingWS] Closed. Code:', event.code);
    };
  }
  
  disconnect(): void {
    if (this.socket && this.socket.readyState !== WebSocket.CLOSED) {
      this.socket.close();
    }
    this.socket = undefined;
  }
}
