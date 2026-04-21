package com.orabank.tfj.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private Boolean active;
    private LocalDateTime createdAt;
    private Long employeeId;
    private String employeeFullName;
}
