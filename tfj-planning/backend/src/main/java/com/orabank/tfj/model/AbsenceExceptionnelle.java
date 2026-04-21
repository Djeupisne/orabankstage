package com.orabank.tfj.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

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
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "raison", length = 500)
    private String raison;

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private Boolean actif = true;

    // Vérifie si une date donnée est comprise dans la période d'absence
    public boolean couvreDate(LocalDate date) {
        if (date == null || dateDebut == null || dateFin == null) return false;
        return !date.isBefore(dateDebut) && !date.isAfter(dateFin);
    }
}
