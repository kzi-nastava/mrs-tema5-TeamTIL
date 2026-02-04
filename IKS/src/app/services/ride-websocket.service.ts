import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { RideLiveUpdateDTO } from '../models/ride-live-update-dto.model';

@Injectable({
  providedIn: 'root'
})
export class RideWebSocketService implements OnDestroy {

  private socket?: WebSocket;
  private updates$ = new Subject<RideLiveUpdateDTO>();

  connect(rideId: number): Observable<RideLiveUpdateDTO> {
    if (this.socket) {
      this.disconnect();
    }

    // TODO: promeniti URL po backendu
    const wsUrl = `ws://localhost:8080/ws/rides/${rideId}`;

    this.socket = new WebSocket(wsUrl);

    this.socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        this.updates$.next(data);
      } catch (e) {
        console.error('WS parse error', e);
      }
    };

    this.socket.onerror = (err) => {
      console.error('WebSocket error', err);
    };

    this.socket.onclose = () => {
      console.log('Ride WS closed');
    };

    return this.updates$.asObservable();
  }

  disconnect() {
    this.socket?.close();
    this.socket = undefined;
    this.updates$.complete();
    this.updates$ = new Subject<RideLiveUpdateDTO>();
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
