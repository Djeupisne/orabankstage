import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PlanningService } from './services/planning.service';
import { Schedule } from './models/schedule';
import { TranslateDayPipe } from './pipes/translate-day.pipe';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateDayPipe],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'TFJ Planning - Orabank Togo';
  
  startDate: string = '';
  endDate: string = '';
  schedules: Schedule[] = [];
  loading: boolean = false;
  error: string = '';
  successMessage: string = '';

  constructor(private planningService: PlanningService) {}

  generatePlanning(): void {
    // Réinitialiser les messages
    this.error = '';
    this.successMessage = '';
    
    if (!this.startDate || !this.endDate) {
      this.error = 'Veuillez sélectionner les dates de début et de fin';
      return;
    }

    // Vérifier que la date de début est antérieure à la date de fin
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

  clearPlanning(): void {
    this.startDate = '';
    this.endDate = '';
    this.schedules = [];
    this.error = '';
    this.successMessage = '';
  }
}
