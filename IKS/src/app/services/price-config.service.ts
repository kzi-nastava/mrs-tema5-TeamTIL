import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PriceConfigDTO } from '../models/price-config-dto.model';

@Injectable({
  providedIn: 'root'
})
export class PriceConfigService {
  private readonly base = `${environment.apiUrl}/price-config`;

  constructor(private http: HttpClient) {}

  getPriceConfig(vehicleType: string): Observable<PriceConfigDTO> {
    return this.http.get<PriceConfigDTO>(`${this.base}/${vehicleType}`);
  }

  updatePriceConfig(vehicleType: string, config: PriceConfigDTO): Observable<PriceConfigDTO> {
    return this.http.put<PriceConfigDTO>(`${this.base}/${vehicleType}`, config);
  }
}