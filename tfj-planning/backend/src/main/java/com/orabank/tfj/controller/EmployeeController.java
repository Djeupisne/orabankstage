package com.orabank.tfj.controller;

import com.orabank.tfj.model.Employee;
import com.orabank.tfj.repository.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employes")
@RequiredArgsConstructor
@Tag(name = "Employés", description = "API de gestion des employés")
public class EmployeeController {
    
    private final EmployeeRepository employeeRepository;
    
    @GetMapping
    @Operation(summary = "Lister tous les employés", 
               description = "Récupère la liste de tous les employés (actifs et inactifs)")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAllByOrderByLastNameFirstName();
        return ResponseEntity.ok(employees.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }
    
    @GetMapping("/actifs")
    @Operation(summary = "Lister les employés actifs", 
               description = "Récupère la liste des employés actifs uniquement")
    public ResponseEntity<List<EmployeeDTO>> getActiveEmployees() {
        List<Employee> employees = employeeRepository.findByActiveTrueOrderByLastNameFirstName();
        return ResponseEntity.ok(employees.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un employé par ID", 
               description = "Récupère les détails d'un employé par son identifiant")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'ID: " + id));
        return ResponseEntity.ok(convertToDTO(employee));
    }
    
    private EmployeeDTO convertToDTO(Employee employee) {
        return new EmployeeDTO(
            employee.getId(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getFullName(),
            employee.getEmail(),
            employee.getActive(),
            employee.getRole() != null ? employee.getRole().getName() : null,
            employee.getService() != null ? employee.getService().getName() : null
        );
    }
    
    public record EmployeeDTO(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        Boolean active,
        String roleName,
        String serviceName
    ) {}
}
