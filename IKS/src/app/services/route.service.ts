import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RouteService {
  private apiUrl = environment.apiUrl + '/route/estimate';

  constructor(private http: HttpClient) {}

  // Stari API (može se ukloniti kasnije)
  estimateRoute(pickup: string, destination: string): Observable<any> {
    return this.http.post<any>(this.apiUrl, { pickup, destination });
  }

  // Novi API: šalje ceo DTO
  estimateRouteFull(request: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, request);
  }
}
