package com.orabank.tfj.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private Long totalEmployees;
    private Long activeEmployees;
    private Long inactiveEmployees;
    private Long totalUsers;
    private Long adminUsers;
    private Long gestionnaireUsers;
    private Long operateurUsers;
    private Long totalSchedules;
    private Long tfjSchedules;
    private Long permanenceSchedules;
}
