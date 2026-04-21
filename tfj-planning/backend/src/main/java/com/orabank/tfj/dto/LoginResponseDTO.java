package com.orabank.tfj.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
    private String username;
    private String email;
    private String role;
    private Long employeeId;
    private String fullName;
}
