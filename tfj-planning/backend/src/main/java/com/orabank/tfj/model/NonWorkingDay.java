package com.orabank.tfj.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity
@Table(name = "non_working_days")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NonWorkingDay {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NonWorkingDayType type = NonWorkingDayType.FULL_DAY;
    
    public enum NonWorkingDayType {
        FULL_DAY,      // Jour férié complet
        MORNING_ONLY,  // Demi-journée matin
        AFTERNOON_ONLY // Demi-journée après-midi
    }
    
    public boolean isFullDay() {
        return type == NonWorkingDayType.FULL_DAY;
    }
    
    public boolean isMorningOnly() {
        return type == NonWorkingDayType.MORNING_ONLY;
    }
    
    public boolean isAfternoonOnly() {
        return type == NonWorkingDayType.AFTERNOON_ONLY;
    }
}
