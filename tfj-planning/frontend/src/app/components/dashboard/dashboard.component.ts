import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { DashboardService, EmployeeService } from '../../services/employee.service';
import { DashboardStats, Employee, User } from '../../models/index';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
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

  constructor(
    private authService: AuthService,
    private dashboardService: DashboardService,
    private employeeService: EmployeeService,
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
      next: (users) => {
        this.users = users;
      },
      error: (err) => {
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
        error: (err) => {
          console.error('Erreur suppression:', err);
        }
      });
    }
  }
}
