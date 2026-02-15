import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FavoriteRouteDTO } from '../models/ride-dto.model'; // DODAJ

@Injectable({ providedIn: 'root' })
export class RouteService {
  private apiUrl = environment.apiUrl + '/route';

  constructor(private http: HttpClient) {}

  // STARE METODE (ostaju iste)
  estimateRoute(pickup: string, destination: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/estimate`, { pickup, destination });
  }

  estimateRouteFull(request: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/estimate`, request);
  }

  // NOVE METODE - FAVORITES
  addToFavorites(routeId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${routeId}/favorite`, {});
  }

  removeFromFavorites(routeId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${routeId}/favorite`);
  }

  getFavoriteRoutes(): Observable<FavoriteRouteDTO[]> {
    return this.http.get<FavoriteRouteDTO[]>(`${this.apiUrl}/favorites`);
  }

  isFavorite(routeId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/${routeId}/favorite/check`);
  }
}