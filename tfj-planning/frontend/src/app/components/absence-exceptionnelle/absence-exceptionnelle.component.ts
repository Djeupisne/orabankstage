import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AbsenceExceptionnelleService } from '../../services/absence-exceptionnelle.service';
import { AbsenceExceptionnelle, Employee } from '../../models/absence-exceptionnelle';

@Component({
  selector: 'app-absence-exceptionnelle',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './absence-exceptionnelle.component.html',
  styleUrls: ['./absence-exceptionnelle.component.css']
})
export class AbsenceExceptionnelleComponent implements OnInit {
  
  formulaire!: FormGroup;
  employes: Employee[] = [];
  loading: boolean = false;
  error: string = '';
  successMessage: string = '';
  absencesRecentes: AbsenceExceptionnelle[] = [];

  motifs = [
    { value: 'MALADIE', label: 'Maladie' },
    { value: 'IMPREVU', label: 'Imprévu personnel' },
    { value: 'FAMILLE', label: 'Raison familiale' },
    { value: 'TRANSPORT', label: 'Problème de transport' },
    { value: 'AUTRE', label: 'Autre' }
  ];

  constructor(
    private fb: FormBuilder,
    private absenceService: AbsenceExceptionnelleService
  ) {}

  ngOnInit(): void {
    this.initFormulaire();
    this.chargerEmployes();
    this.chargerAbsencesRecentes();
  }

  initFormulaire(): void {
    this.formulaire = this.fb.group({
      employeeId: ['', Validators.required],
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      estDemiJourneeDebut: [false],
      estDemiJourneeFin: [false],
      motif: ['MALADIE', Validators.required],
      commentaire: [''],
      saisiPar: ['Administrateur'],
      estReaffectationAuto: [true]
    });
  }

  chargerEmployes(): void {
    this.loading = true;
    this.absenceService.getActiveEmployees().subscribe({
      next: (data) => {
        this.employes = data.filter(emp => emp.active);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des employés: ' + err.message;
        this.loading = false;
      }
    });
  }

  chargerAbsencesRecentes(): void {
    const today = new Date();
    const startDate = today.toISOString().split('T')[0];
    const endDate = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    
    this.absenceService.getAbsencesInPeriod(startDate, endDate).subscribe({
      next: (data) => {
        this.absencesRecentes = data;
      },
      error: (err) => {
        console.error('Erreur chargement absences:', err);
      }
    });
  }

  soumettre(): void {
    if (this.formulaire.invalid) {
      this.error = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    // Vérifier que la date de fin est postérieure à la date de début
    const dateDebut = new Date(this.formulaire.value.dateDebut);
    const dateFin = new Date(this.formulaire.value.dateFin);
    
    if (dateDebut > dateFin) {
      this.error = 'La date de fin doit être postérieure ou égale à la date de début';
      return;
    }

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    const absence: AbsenceExceptionnelle = this.formulaire.value;
    
    this.absenceService.createAbsence(absence).subscribe({
      next: (data) => {
        this.successMessage = `Absence enregistrée avec succès pour ${data.employeeFullName}. Le planning sera réaffecté automatiquement.`;
        this.formulaire.reset({
          motif: 'MALADIE',
          estDemiJourneeDebut: false,
          estDemiJourneeFin: false,
          saisiPar: 'Administrateur',
          estReaffectationAuto: true
        });
        this.chargerAbsencesRecentes();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors de l\'enregistrement: ' + err.message;
        this.loading = false;
      }
    });
  }

  supprimerAbsence(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette absence ?')) {
      this.absenceService.deleteAbsence(id).subscribe({
        next: () => {
          this.successMessage = 'Absence supprimée avec succès';
          this.chargerAbsencesRecentes();
        },
        error: (err) => {
          this.error = 'Erreur lors de la suppression: ' + err.message;
        }
      });
    }
  }

  get f() { return this.formulaire.controls; }
}
