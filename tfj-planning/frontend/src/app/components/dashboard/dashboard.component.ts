import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { DashboardService, EmployeeService } from '../../services/employee.service';
import { PlanningService } from '../../services/planning.service';
import { DashboardStats, Employee, User, Schedule } from '../../models/index';
import { TranslateDayPipe } from '../../pipes/translate-day.pipe';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, TranslateDayPipe],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  stats: DashboardStats | null = null;
  employees: Employee[] = [];
  users: User[] = [];
  loading: boolean = false;
  activeTab: string = 'overview';
  showEmployeeModal: boolean = false;
  showUserModal: boolean = false;
  
  // Planning generation
  startDate: string = '';
  endDate: string = '';
  schedules: Schedule[] = [];
  planningLoading: boolean = false;
  planningError: string = '';
  planningSuccess: string = '';
  showPlanningSection: boolean = false;

  constructor(
    public authService: AuthService,
    private dashboardService: DashboardService,
    private employeeService: EmployeeService,
    private planningService: PlanningService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/home']);
      return;
    }
    this.loadDashboardStats();
    this.loadEmployees();
    this.loadUsers();
    this.initPlanningDates();
  }

  loadDashboardStats(): void {
    this.loading = true;
    this.dashboardService.getDashboardStats().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement stats:', err);
        this.loading = false;
      }
    });
  }

  loadEmployees(): void {
    this.employeeService.getAllEmployees().subscribe({
      next: (employees) => {
        this.employees = employees;
      },
      error: (err) => {
        console.error('Erreur chargement employés:', err);
      }
    });
  }

  loadUsers(): void {
    this.authService.getAllUsers().subscribe({
      next: (users: User[]) => {
        this.users = users;
      },
      error: (err: any) => {
        console.error('Erreur chargement utilisateurs:', err);
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/home']);
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  toggleEmployeeStatus(id: number, currentStatus: boolean): void {
    const employee = this.employees.find(e => e.id === id);
    if (employee) {
      employee.active = !currentStatus;
      this.employeeService.updateEmployee(id, employee).subscribe({
        next: () => {
          this.loadEmployees();
          this.loadDashboardStats();
        },
        error: (err) => {
          console.error('Erreur mise à jour:', err);
          employee.active = currentStatus;
        }
      });
    }
  }

  deleteUser(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
      this.authService.deleteUser(id).subscribe({
        next: () => {
          this.loadUsers();
          this.loadDashboardStats();
        },
        error: (err: any) => {
          console.error('Erreur suppression:', err);
        }
      });
    }
  }

  // Planning methods
  initPlanningDates(): void {
    const today = new Date();
    const monday = this.getMonday(today);
    const friday = new Date(monday);
    friday.setDate(monday.getDate() + 4);

    this.startDate = this.formatDate(monday);
    this.endDate = this.formatDate(friday);
  }

  getMonday(d: Date): Date {
    d = new Date(d);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1);
    return new Date(d.setDate(diff));
  }

  formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  generatePlanning(): void {
    this.planningError = '';
    this.planningSuccess = '';
    
    if (!this.startDate || !this.endDate) {
      this.planningError = 'Veuillez sélectionner les dates de début et de fin';
      return;
    }

    const start = new Date(this.startDate);
    const end = new Date(this.endDate);
    if (start > end) {
      this.planningError = 'La date de début doit être antérieure à la date de fin';
      return;
    }

    this.planningLoading = true;
    this.schedules = [];

    this.planningService.generatePlanning(this.startDate, this.endDate).subscribe({
      next: (data) => {
        this.schedules = data;
        this.planningLoading = false;
        this.showPlanningSection = true;
        if (data.length > 0) {
          this.planningSuccess = `Planning généré avec succès : ${data.length} affectation(s) trouvée(s). Le planning est maintenant disponible sur l'accueil pour téléchargement.`;
        } else {
          this.planningSuccess = 'Aucune affectation nécessaire pour cette période';
        }
      },
      error: (err) => {
        this.planningError = err.message || 'Erreur lors de la génération du planning';
        this.planningLoading = false;
        this.schedules = [];
      }
    });
  }

  downloadPDF(): void {
    if (!this.startDate || !this.endDate) {
      this.planningError = "Veuillez d'abord générer le planning";
      return;
    }
    
    const url = `${this.planningService['apiUrl']}/export/pdf?startDate=${this.startDate}&endDate=${this.endDate}`;
    window.open(url, '_blank');
  }

  downloadExcel(): void {
    if (!this.startDate || !this.endDate) {
      this.planningError = "Veuillez d'abord générer le planning";
      return;
    }
    
    const url = `${this.planningService['apiUrl']}/export/excel?startDate=${this.startDate}&endDate=${this.endDate}`;
    window.open(url, '_blank');
  }

  clearPlanning(): void {
    this.startDate = '';
    this.endDate = '';
    this.schedules = [];
    this.planningError = '';
    this.planningSuccess = '';
    this.showPlanningSection = false;
  }
}
