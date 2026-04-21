import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { Schedule } from '../models/schedule';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PlanningService {
  
  private apiUrl = `${environment.apiUrl}/api/planning`;
  private readonly REQUEST_TIMEOUT_MS = 30000; // 30 secondes

  constructor(private http: HttpClient) {}

  generatePlanning(startDate: string, endDate: string): Observable<Schedule[]> {
    let params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<Schedule[]>(`${this.apiUrl}/generate`, { params })
      .pipe(
        timeout(this.REQUEST_TIMEOUT_MS),
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Une erreur inconnue s\'est produite';
    
    if (error.error instanceof ErrorEvent) {
      // Erreur côté client
      errorMessage = `Erreur client: ${error.error.message}`;
    } else {
      // Erreur côté serveur
      switch (error.status) {
        case 0:
          errorMessage = 'Impossible de se connecter au serveur. Vérifiez que le backend est en cours d\'exécution.';
          break;
        case 400:
          errorMessage = 'Requête invalide. Veuillez vérifier les dates saisies.';
          break;
        case 401:
          errorMessage = 'Non autorisé. Veuillez vous authentifier.';
          break;
        case 403:
          errorMessage = 'Accès refusé. Vous n\'avez pas les permissions nécessaires.';
          break;
        case 404:
          errorMessage = 'Ressource non trouvée.';
          break;
        case 500:
          errorMessage = 'Erreur interne du serveur. Veuillez réessayer plus tard.';
          break;
        case 502:
        case 503:
        case 504:
          errorMessage = 'Le service est temporairement indisponible. Veuillez réessayer plus tard.';
          break;
        default:
          if (error.error && error.error.message) {
            errorMessage = error.error.message;
          } else if (error.message) {
            errorMessage = error.message;
          }
          break;
      }
    }
    
    console.error('Erreur API:', error);
    return throwError(() => new Error(errorMessage));
  }
}
