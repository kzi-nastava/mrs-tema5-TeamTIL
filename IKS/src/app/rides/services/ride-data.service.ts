import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RideDataService {
  private selectedRideSubject = new BehaviorSubject<any>(null);
  selectedRide$ = this.selectedRideSubject.asObservable();

  setSelectedRide(ride: any) {
    this.selectedRideSubject.next(ride);
  }

  getSelectedRide() {
    return this.selectedRideSubject.value;
  }

  clearSelectedRide() {
    this.selectedRideSubject.next(null);
  }
}
