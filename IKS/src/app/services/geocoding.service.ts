import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class GeocodingService {
  private nominatimUrl = 'https://nominatim.openstreetmap.org/search';

  constructor(private http: HttpClient) {}

  geocode(address: string): Observable<any> {
    // Uvek traži u Novom Sadu
    const params = {
      q: `${address}, Novi Sad`,
      format: 'json',
      addressdetails: '1',
      limit: '1'
    };
    return this.http.get<any>(this.nominatimUrl, { params });
  }
}
