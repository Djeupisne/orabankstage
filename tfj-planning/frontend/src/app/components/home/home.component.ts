import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PlanningService } from '../../services/planning.service';
import { AuthService } from '../../services/auth.service';
import { Schedule } from '../../models/schedule';
import { TranslateDayPipe } from '../../pipes/translate-day.pipe';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TranslateDayPipe],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  startDate: string = '';
  endDate: string = '';
  schedules: Schedule[] = [];
  loading: boolean = false;
  error: string = '';
  successMessage: string = '';
  showPlanningSection: boolean = false;
  
  constructor(
    private planningService: PlanningService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    // Set default dates (current week)
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
    this.error = '';
    this.successMessage = '';
    
    if (!this.startDate || !this.endDate) {
      this.error = 'Veuillez sélectionner les dates de début et de fin';
      return;
    }

    const start = new Date(this.startDate);
    const end = new Date(this.endDate);
    if (start > end) {
      this.error = 'La date de début doit être antérieure à la date de fin';
      return;
    }

    this.loading = true;
    this.schedules = [];

    this.planningService.generatePlanning(this.startDate, this.endDate).subscribe({
      next: (data) => {
        this.schedules = data;
        this.loading = false;
        this.showPlanningSection = true;
        if (data.length > 0) {
          this.successMessage = `Planning généré avec succès : ${data.length} affectation(s) trouvée(s)`;
        } else {
          this.successMessage = 'Aucune affectation nécessaire pour cette période';
        }
      },
      error: (err) => {
        this.error = err.message || 'Erreur lors de la génération du planning';
        this.loading = false;
        this.schedules = [];
      }
    });
  }

  downloadPDF(): void {
    if (!this.startDate || !this.endDate) {
      this.error = 'Veuillez d\'abord générer le planning';
      return;
    }
    
    const url = `${this.planningService['apiUrl']}/export/pdf?startDate=${this.startDate}&endDate=${this.endDate}`;
    window.open(url, '_blank');
  }

  downloadExcel(): void {
    if (!this.startDate || !this.endDate) {
      this.error = 'Veuillez d\'abord générer le planning';
      return;
    }
    
    const url = `${this.planningService['apiUrl']}/export/excel?startDate=${this.startDate}&endDate=${this.endDate}`;
    window.open(url, '_blank');
  }

  clearPlanning(): void {
    this.startDate = '';
    this.endDate = '';
    this.schedules = [];
    this.error = '';
    this.successMessage = '';
    this.showPlanningSection = false;
  }
}
