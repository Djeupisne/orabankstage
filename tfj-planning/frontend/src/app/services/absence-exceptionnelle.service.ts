import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AbsenceExceptionnelle } from '../models/absence-exceptionnelle';
import { Employee } from '../models/absence-exceptionnelle';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AbsenceExceptionnelleService {
  
  private apiUrl = `${environment.apiUrl}/api/absences-exceptionnelles`;
  private employeesUrl = `${environment.apiUrl}/api/employes`;

  constructor(private http: HttpClient) {}

  /**
   * Crée une nouvelle absence exceptionnelle
   */
  createAbsence(absence: AbsenceExceptionnelle): Observable<AbsenceExceptionnelle> {
    return this.http.post<AbsenceExceptionnelle>(this.apiUrl, absence)
      .pipe(catchError(this.handleError));
  }

  /**
   * Met à jour une absence exceptionnelle existante
   */
  updateAbsence(id: number, absence: AbsenceExceptionnelle): Observable<AbsenceExceptionnelle> {
    return this.http.put<AbsenceExceptionnelle>(`${this.apiUrl}/${id}`, absence)
      .pipe(catchError(this.handleError));
  }

  /**
   * Supprime une absence exceptionnelle
   */
  deleteAbsence(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Récupère une absence exceptionnelle par son ID
   */
  getAbsenceById(id: number): Observable<AbsenceExceptionnelle> {
    return this.http.get<AbsenceExceptionnelle>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Récupère les absences dans une période
   */
  getAbsencesInPeriod(startDate: string, endDate: string): Observable<AbsenceExceptionnelle[]> {
    let params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<AbsenceExceptionnelle[]>(`${this.apiUrl}/periode`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Récupère les absences d'un employé dans une période
   */
  getAbsencesByEmployee(employeeId: number, startDate: string, endDate: string): Observable<AbsenceExceptionnelle[]> {
    let params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<AbsenceExceptionnelle[]>(`${this.apiUrl}/employe/${employeeId}`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Récupère tous les employés actifs
   */
  getActiveEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.employeesUrl)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Une erreur inconnue s\'est produite';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur client: ${error.error.message}`;
    } else {
      switch (error.status) {
        case 0:
          errorMessage = 'Impossible de se connecter au serveur.';
          break;
        case 400:
          errorMessage = 'Requête invalide.';
          break;
        case 404:
          errorMessage = 'Ressource non trouvée.';
          break;
        case 500:
          errorMessage = 'Erreur interne du serveur.';
          break;
        default:
          if (error.error && error.error.message) {
            errorMessage = error.error.message;
          }
          break;
      }
    }
    
    console.error('Erreur API:', error);
    return throwError(() => new Error(errorMessage));
  }
}
