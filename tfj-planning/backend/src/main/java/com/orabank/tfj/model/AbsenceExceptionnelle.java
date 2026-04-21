package com.tfj.planning.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "absences_exceptionnelles")
public class AbsenceExceptionnelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employe_id", nullable = false)
    private Long employeId;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "raison", length = 500)
    private String raison;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    public AbsenceExceptionnelle() {}

    public AbsenceExceptionnelle(Long employeId, LocalDate dateDebut, LocalDate dateFin, String raison) {
        this.employeId = employeId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.raison = raison;
    }

    // Vérifie si une date donnée est comprise dans la période d'absence
    public boolean couvreDate(LocalDate date) {
        if (date == null || dateDebut == null || dateFin == null) return false;
        return !date.isBefore(dateDebut) && !date.isAfter(dateFin);
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeId() { return employeId; }
    public void setEmployeId(Long employeId) { this.employeId = employeId; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public String getRaison() { return raison; }
    public void setRaison(String raison) { this.raison = raison; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
