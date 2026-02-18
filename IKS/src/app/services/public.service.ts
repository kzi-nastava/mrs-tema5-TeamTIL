import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { VehicleDTO } from '../models/vehicle-dto.model';

@Injectable({ providedIn: 'root' })
export class PublicService {
    private apiUrl = environment.apiUrl + '/public';

    constructor(private http: HttpClient) { }
    
    getAvailableVehicles(): Observable<VehicleDTO[]> {
        return this.http.get<VehicleDTO[]>(`${this.apiUrl}/vehicles`);
    }
}