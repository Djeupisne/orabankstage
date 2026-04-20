package com.orabank.tfj.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRequestDTO {
    
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String scheduleType; // TFJ ou PERMANENCE
    private List<Long> employeeIds; // Pour la génération en masse
    private Boolean includeSoloMembers;
}
