import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Employee, DashboardStats } from '../models/index';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  
  private apiUrl = `${environment.apiUrl}/api/employes`;

  constructor(private http: HttpClient) {}

  getAllEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.apiUrl).pipe(
      catchError(this.handleError)
    );
  }

  getActiveEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.apiUrl}/actifs`).pipe(
      catchError(this.handleError)
    );
  }

  getEmployeeById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  createEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.apiUrl, employee).pipe(
      catchError(this.handleError)
    );
  }

  updateEmployee(id: number, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiUrl}/${id}`, employee).pipe(
      catchError(this.handleError)
    );
  }

  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Une erreur inconnue s\'est produite';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur client: ${error.error.message}`;
    } else {
      if (error.error?.message) {
        errorMessage = error.error.message;
      } else {
        errorMessage = `Erreur serveur (code ${error.status})`;
      }
    }
    
    console.error('Erreur API:', error);
    return throwError(() => new Error(errorMessage));
  }
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  
  private apiUrl = `${environment.apiUrl}/api/dashboard`;

  constructor(private http: HttpClient) {}

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/stats`).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Une erreur inconnue s\'est produite';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur client: ${error.error.message}`;
    } else if (error.error?.message) {
      errorMessage = error.error.message;
    }
    
    console.error('Erreur API:', error);
    return throwError(() => new Error(errorMessage));
  }
}
