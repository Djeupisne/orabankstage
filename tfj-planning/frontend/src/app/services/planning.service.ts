import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Schedule } from '../models/schedule';

@Injectable({
  providedIn: 'root'
})
export class PlanningService {
  
  private apiUrl = '/api/planning';

  constructor(private http: HttpClient) {}

  generatePlanning(startDate: string, endDate: string): Observable<Schedule[]> {
    let params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<Schedule[]>(`${this.apiUrl}/generate`, { params });
  }
}
