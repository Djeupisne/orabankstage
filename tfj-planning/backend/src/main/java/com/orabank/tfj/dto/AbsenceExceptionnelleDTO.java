package com.orabank.tfj.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * DTO pour la création/mise à jour d'une absence exceptionnelle
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbsenceExceptionnelleDTO {
    
    private Long id;
    
    private Long employeeId;
    private String employeeFullName;
    
    private LocalDate dateDebut;
    private LocalDate dateFin;
    
    @Builder.Default
    private boolean estDemiJourneeDebut = false;
    
    @Builder.Default
    private boolean estDemiJourneeFin = false;
    
    private String motif; // MALADIE, IMPREVU, AUTRE, etc.
    private String commentaire;
    private String saisiPar; // Nom de l'administrateur
    
    @Builder.Default
    private boolean estReaffectationAuto = true;
}
