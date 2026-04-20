package com.orabank.tfj.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ScheduleType type = ScheduleType.TFJ;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DayOfWeek dayOfWeek;
    
    @Column(length = 255)
    private String notes;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isConfirmed = false;
    
    public enum ScheduleType {
        TFJ,         // Travaux de Fin de Journée (Lundi-Vendredi)
        PERMANENCE   // Permanence (Samedi)
    }
    
    @PrePersist
    @PreUpdate
    private void setDayOfWeek() {
        if (date != null) {
            this.dayOfWeek = date.getDayOfWeek();
        }
    }
    
    public boolean isTFJ() {
        return type == ScheduleType.TFJ;
    }
    
    public boolean isPermanence() {
        return type == ScheduleType.PERMANENCE;
    }
}
