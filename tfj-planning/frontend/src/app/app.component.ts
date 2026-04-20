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

  constructor(private planningService: PlanningService) {}

  generatePlanning(): void {
    if (!this.startDate || !this.endDate) {
      this.error = 'Veuillez sélectionner les dates de début et de fin';
      return;
    }

    this.loading = true;
    this.error = '';
    this.schedules = [];

    this.planningService.generatePlanning(this.startDate, this.endDate).subscribe({
      next: (data) => {
        this.schedules = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors de la génération du planning: ' + err.message;
        this.loading = false;
      }
    });
  }
}
