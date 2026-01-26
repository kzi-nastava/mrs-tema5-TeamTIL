import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DriverDataService {
  private driverData: any = null;
  private vehicleData: any = null;
  private photoPreview: string | null = null;

  private readonly API_URL = 'http://localhost:8080/api/drivers';

  constructor(private http: HttpClient) {}

  setDriverData(data: any, photo: string | null) {
    this.driverData = data;
    this.photoPreview = photo;
  }

  getDriverData() { return this.driverData; }
  getPhotoPreview() { return this.photoPreview; }
  
  setVehicleData(data: any) { this.vehicleData = data; }
  getVehicleData() { return this.vehicleData; }

  clearData() {
    this.driverData = null;
    this.vehicleData = null;
    this.photoPreview = null;
  }

  sendRegistration(): Observable<any> {
    const payload = {
      firstName: this.driverData.name,
      lastName: this.driverData.surname,
      email: this.driverData.email,
      phoneNumber: this.driverData.phoneNumber,
      address: this.driverData.address,
      profilePictureUrl: this.photoPreview,

      vehicleModel: this.vehicleData.model,
      vehicleType: this.vehicleData.type,
      licensePlate: this.vehicleData.plate,
      passengerCapacity: this.vehicleData.seats,
      babyFriendly: this.vehicleData.babyFriendly,
      petFriendly: this.vehicleData.petFriendly
    };

    return this.http.post(this.API_URL, payload, { responseType: 'text' });
  }
}