package com.orabank.tfj.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Représente une absence exceptionnelle d'un employé (maladie, imprévu, etc.)
 * Contrairement aux congés planifiés, cette absence peut être saisie à posteriori
 * et déclenche une réaffectation automatique dans le planning.
 */
@Entity
@Table(name = "absences_exceptionnelles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbsenceExceptionnelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employee employee;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "est_demi_journee_debut", nullable = false)
    @Builder.Default
    private boolean estDemiJourneeDebut = false;

    @Column(name = "est_demi_journee_fin", nullable = false)
    @Builder.Default
    private boolean estDemiJourneeFin = false;

    @Column(name = "motif", length = 255)
    private String motif; // MALADIE, IMPREVU, AUTRE, etc.

    @Column(name = "commentaire", length = 500)
    private String commentaire;

    @Column(name = "saisi_par", length = 100)
    private String saisiPar; // Nom de l'administrateur qui a saisi l'absence

    @Column(name = "date_saisie", nullable = false)
    private LocalDate dateSaisie;

    @Column(name = "est_reaffectation_auto", nullable = false)
    @Builder.Default
    private boolean estReaffectationAuto = true;

    /**
     * Vérifie si l'absence couvre une date donnée
     * Gère les demi-journées de début et de fin
     */
    public boolean couvreDate(LocalDate date) {
        if (date.isBefore(dateDebut) || date.isAfter(dateFin)) {
            return false;
        }
        
        // Si c'est la première journée et que c'est une demi-journée
        if (date.equals(dateDebut) && estDemiJourneeDebut) {
            // On considère l'employé absent pour simplifier
            return true;
        }
        
        // Si c'est la dernière journée et que c'est une demi-journée
        if (date.equals(dateFin) && estDemiJourneeFin) {
            return true;
        }
        
        // Journées complètes entre début et fin
        return true;
    }
    
    /**
     * Vérifie si l'absence couvre complètement une date (pas de demi-journée ce jour-là)
     */
    public boolean couvreJourneeComplete(LocalDate date) {
        if (date.isBefore(dateDebut) || date.isAfter(dateFin)) {
            return false;
        }
        
        // Si demi-journée de début et on est le premier jour
        if (date.equals(dateDebut) && estDemiJourneeDebut) {
            return false; // Seulement demi-journée
        }
        
        // Si demi-journée de fin et on est le dernier jour
        if (date.equals(dateFin) && estDemiJourneeFin) {
            return false; // Seulement demi-journée
        }
        
        return true;
    }
}
