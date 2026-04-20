package com.orabank.tfj.dto;

import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import com.orabank.tfj.model.Schedule.ScheduleType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponseDTO {
    
    private Long id;
    private Long employeeId;
    private String employeeFullName;
    private String employeeEmail;
    private String roleName;
    private String serviceName;
    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private ScheduleType type;
    private String notes;
    private Boolean isConfirmed;
    private Boolean isSoloInGroup;
}
