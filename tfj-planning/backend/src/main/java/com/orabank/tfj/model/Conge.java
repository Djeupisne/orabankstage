package com.orabank.tfj.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "conges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conge {

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

    @Column(name = "type_conge", length = 50)
    private String typeConge; // CONGES_PAYES, MALADIE, SANS_SOLDE, etc.

    /**
     * Vérifie si le congé couvre une date donnée
     * Gère les demi-journées de début et de fin
     */
    public boolean couvreDate(LocalDate date) {
        if (date.isBefore(dateDebut) || date.isAfter(dateFin)) {
            return false;
        }
        
        // Si c'est la première journée et que c'est une demi-journée
        if (date.equals(dateDebut) && estDemiJourneeDebut) {
            // On considère l'employé absent pour simplifier
            // Pourrait être affiné pour gérer matin/après-midi
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
     * Vérifie si le congé couvre complètement une date (pas de demi-journée ce jour-là)
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
