package com.orabank.tfj.service;

import com.orabank.tfj.model.*;
import com.orabank.tfj.repository.*;
import com.orabank.tfj.dto.AbsenceExceptionnelleDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbsenceExceptionnelleService {
    
    private final AbsenceExceptionnelleRepository absenceRepository;
    private final EmployeeRepository employeeRepository;
    private final ScheduleRepository scheduleRepository;
    private final PlanningAlgorithmService planningAlgorithmService;
    
    /**
     * Crée une nouvelle absence exceptionnelle
     */
    @Transactional
    public AbsenceExceptionnelleDTO createAbsence(AbsenceExceptionnelleDTO dto) {
        log.info("Création d'une absence exceptionnelle pour l'employé {} du {} au {}", 
                 dto.getEmployeeId(), dto.getDateDebut(), dto.getDateFin());
        
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'ID: " + dto.getEmployeeId()));
        
        AbsenceExceptionnelle absence = AbsenceExceptionnelle.builder()
                .employee(employee)
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .estDemiJourneeDebut(dto.isEstDemiJourneeDebut())
                .estDemiJourneeFin(dto.isEstDemiJourneeFin())
                .motif(dto.getMotif())
                .commentaire(dto.getCommentaire())
                .saisiPar(dto.getSaisiPar())
                .dateSaisie(LocalDate.now())
                .estReaffectationAuto(dto.isEstReaffectationAuto())
                .build();
        
        AbsenceExceptionnelle savedAbsence = absenceRepository.save(absence);
        
        // Si réaffectation automatique est activée, déclencher la réaffectation
        if (dto.isEstReaffectationAuto()) {
            reassignSchedulesForAbsentEmployee(employee, dto.getDateDebut(), dto.getDateFin());
        }
        
        return convertToDTO(savedAbsence);
    }
    
    /**
     * Met à jour une absence exceptionnelle existante
     */
    @Transactional
    public AbsenceExceptionnelleDTO updateAbsence(Long id, AbsenceExceptionnelleDTO dto) {
        log.info("Mise à jour de l'absence exceptionnelle {}", id);
        
        AbsenceExceptionnelle existingAbsence = absenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Absence non trouvée avec l'ID: " + id));
        
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'ID: " + dto.getEmployeeId()));
        
        existingAbsence.setEmployee(employee);
        existingAbsence.setDateDebut(dto.getDateDebut());
        existingAbsence.setDateFin(dto.getDateFin());
        existingAbsence.setEstDemiJourneeDebut(dto.isEstDemiJourneeDebut());
        existingAbsence.setEstDemiJourneeFin(dto.isEstDemiJourneeFin());
        existingAbsence.setMotif(dto.getMotif());
        existingAbsence.setCommentaire(dto.getCommentaire());
        existingAbsence.setEstReaffectationAuto(dto.isEstReaffectationAuto());
        
        AbsenceExceptionnelle updatedAbsence = absenceRepository.save(existingAbsence);
        
        // Si réaffectation automatique est activée, déclencher la réaffectation
        if (dto.isEstReaffectationAuto()) {
            reassignSchedulesForAbsentEmployee(employee, dto.getDateDebut(), dto.getDateFin());
        }
        
        return convertToDTO(updatedAbsence);
    }
    
    /**
     * Supprime une absence exceptionnelle
     */
    @Transactional
    public void deleteAbsence(Long id) {
        log.info("Suppression de l'absence exceptionnelle {}", id);
        absenceRepository.deleteById(id);
    }
    
    /**
     * Récupère une absence exceptionnelle par son ID
     */
    @Transactional(readOnly = true)
    public AbsenceExceptionnelleDTO getAbsenceById(Long id) {
        AbsenceExceptionnelle absence = absenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Absence non trouvée avec l'ID: " + id));
        return convertToDTO(absence);
    }
    
    /**
     * Récupère toutes les absences exceptionnelles dans une période
     */
    @Transactional(readOnly = true)
    public List<AbsenceExceptionnelleDTO> getAbsencesInPeriod(LocalDate startDate, LocalDate endDate) {
        List<AbsenceExceptionnelle> absences = absenceRepository.findAbsencesInPeriod(startDate, endDate);
        return absences.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les absences d'un employé dans une période
     */
    @Transactional(readOnly = true)
    public List<AbsenceExceptionnelleDTO> getAbsencesByEmployeeAndPeriod(Long employeeId, 
                                                                          LocalDate startDate, 
                                                                          LocalDate endDate) {
        List<AbsenceExceptionnelle> absences = absenceRepository.findAbsencesByEmployeeAndPeriod(
                employeeId, startDate, endDate);
        return absences.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie si un employé est en absence exceptionnelle à une date donnée
     */
    @Transactional(readOnly = true)
    public boolean isEmployeeOnExceptionalLeave(Long employeeId, LocalDate date) {
        return absenceRepository.isEmployeeOnExceptionalLeave(employeeId, date);
    }
    
    /**
     * Réaffecte automatiquement les plannings d'un employé absent
     * Cette méthode supprime les schedules de l'employé absent dans la période
     * et régénère le planning pour ces dates
     */
    @Transactional
    public void reassignSchedulesForAbsentEmployee(Employee employee, LocalDate startDate, LocalDate endDate) {
        log.info("Réaffectation automatique des plannings pour l'employé {} absent du {} au {}", 
                 employee.getFullName(), startDate, endDate);
        
        // Trouver tous les schedules de l'employé dans la période
        List<Schedule> schedulesToRemove = scheduleRepository.findByEmployeeIdAndDateBetween(
                employee.getId(), startDate, endDate);
        
        if (!schedulesToRemove.isEmpty()) {
            log.info("Found {} schedules to reassign for employee {}", schedulesToRemove.size(), employee.getId());
            
            // Supprimer les schedules de l'employé absent
            scheduleRepository.deleteAll(schedulesToRemove);
            
            // Régénérer le planning pour la période
            // Note: Cela va recalculer tout le planning pour la période
            planningAlgorithmService.generatePlanning(startDate, endDate);
            
            log.info("Réaffectation terminée pour l'employé {}", employee.getFullName());
        } else {
            log.info("Aucun schedule à réaffecter pour l'employé {} dans cette période", employee.getFullName());
        }
    }
    
    /**
     * Convertit une entité en DTO
     */
    private AbsenceExceptionnelleDTO convertToDTO(AbsenceExceptionnelle absence) {
        return AbsenceExceptionnelleDTO.builder()
                .id(absence.getId())
                .employeeId(absence.getEmployee().getId())
                .employeeFullName(absence.getEmployee().getFullName())
                .dateDebut(absence.getDateDebut())
                .dateFin(absence.getDateFin())
                .estDemiJourneeDebut(absence.getEstDemiJourneeDebut())
                .estDemiJourneeFin(absence.getEstDemiJourneeFin())
                .motif(absence.getMotif())
                .commentaire(absence.getCommentaire())
                .saisiPar(absence.getSaisiPar())
                .estReaffectationAuto(absence.getEstReaffectationAuto())
                .build();
    }
}
