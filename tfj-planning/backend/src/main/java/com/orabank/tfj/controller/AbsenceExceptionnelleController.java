package com.orabank.tfj.controller;

import com.orabank.tfj.dto.AbsenceExceptionnelleDTO;
import com.orabank.tfj.service.AbsenceExceptionnelleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/absences-exceptionnelles")
@RequiredArgsConstructor
@Tag(name = "Absences Exceptionnelles", description = "API de gestion des absences exceptionnelles (maladie, imprévu)")
public class AbsenceExceptionnelleController {
    
    private final AbsenceExceptionnelleService absenceService;
    
    @PostMapping
    @Operation(summary = "Créer une absence exceptionnelle", 
               description = "Saisit une nouvelle absence exceptionnelle pour un employé et déclenche la réaffectation automatique")
    public ResponseEntity<AbsenceExceptionnelleDTO> createAbsence(@RequestBody AbsenceExceptionnelleDTO dto) {
        AbsenceExceptionnelleDTO createdAbsence = absenceService.createAbsence(dto);
        return ResponseEntity.ok(createdAbsence);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une absence exceptionnelle", 
               description = "Met à jour les informations d'une absence exceptionnelle existante")
    public ResponseEntity<AbsenceExceptionnelleDTO> updateAbsence(
            @PathVariable Long id, 
            @RequestBody AbsenceExceptionnelleDTO dto) {
        AbsenceExceptionnelleDTO updatedAbsence = absenceService.updateAbsence(id, dto);
        return ResponseEntity.ok(updatedAbsence);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une absence exceptionnelle", 
               description = "Supprime une absence exceptionnelle")
    public ResponseEntity<Void> deleteAbsence(@PathVariable Long id) {
        absenceService.deleteAbsence(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une absence exceptionnelle", 
               description = "Récupère les détails d'une absence exceptionnelle par son ID")
    public ResponseEntity<AbsenceExceptionnelleDTO> getAbsenceById(@PathVariable Long id) {
        AbsenceExceptionnelleDTO absence = absenceService.getAbsenceById(id);
        return ResponseEntity.ok(absence);
    }
    
    @GetMapping("/periode")
    @Operation(summary = "Récupérer les absences d'une période", 
               description = "Récupère toutes les absences exceptionnelles dans une période donnée")
    public ResponseEntity<List<AbsenceExceptionnelleDTO>> getAbsencesInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AbsenceExceptionnelleDTO> absences = absenceService.getAbsencesInPeriod(startDate, endDate);
        return ResponseEntity.ok(absences);
    }
    
    @GetMapping("/employe/{employeeId}")
    @Operation(summary = "Récupérer les absences d'un employé", 
               description = "Récupère les absences exceptionnelles d'un employé dans une période donnée")
    public ResponseEntity<List<AbsenceExceptionnelleDTO>> getAbsencesByEmployee(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AbsenceExceptionnelleDTO> absences = absenceService.getAbsencesByEmployeeAndPeriod(
                employeeId, startDate, endDate);
        return ResponseEntity.ok(absences);
    }
}
