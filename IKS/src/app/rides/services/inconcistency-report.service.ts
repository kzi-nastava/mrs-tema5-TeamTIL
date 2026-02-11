import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InconsistencyReportRequestDTO, InconsistencyReportResponseDTO } from '../../models/ride-dto.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class InconsistencyReportService {

  private baseUrl = environment.apiUrl + '/rides';

  constructor(private http: HttpClient) {}

  submitReport(rideId: number, report: InconsistencyReportRequestDTO): Observable<InconsistencyReportResponseDTO> {
    return this.http.post<InconsistencyReportResponseDTO>(`${this.baseUrl}/${rideId}/report`, report);
  }
}
