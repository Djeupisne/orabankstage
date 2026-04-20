package com.orabank.tfj.service;

import com.orabank.tfj.model.*;
import com.orabank.tfj.repository.*;
import com.orabank.tfj.dto.ScheduleResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanningAlgorithmService {
    
    private final EmployeeRepository employeeRepository;
    private final ScheduleRepository scheduleRepository;
    private final NonWorkingDayRepository nonWorkingDayRepository;
    
    /**
     * Génère le planning des TFJ et permanences selon les règles métier :
     * 1. Les membres d'un même groupe ne peuvent pas se suivre
     * 2. Rotation anti-chronologique : si un membre est affecté un jour, 
     *    la semaine suivante il prend le jour antérieur
     * 3. Les membres seuls dans leur groupe ne sont programmés que vendredi (TFJ) ou samedi (permanence)
     */
    @Transactional
    public List<ScheduleResponseDTO> generatePlanning(LocalDate startDate, LocalDate endDate) {
        log.info("Génération du planning du {} au {}", startDate, endDate);
        
        List<Employee> allActiveEmployees = employeeRepository.findByActiveTrueOrderByLastName_FirstName();
        List<NonWorkingDay> nonWorkingDays = nonWorkingDayRepository.findNonWorkingDaysInPeriod(startDate, endDate);
        Set<LocalDate> nonWorkingDates = nonWorkingDays.stream()
                .filter(NonWorkingDay::isFullDay)
                .map(NonWorkingDay::getDate)
                .collect(Collectors.toSet());
        
        // Grouper les employés par rôle
        Map<String, List<Employee>> employeesByRole = allActiveEmployees.stream()
                .collect(Collectors.groupingBy(e -> e.getRole().getName()));
        
        // Identifier les membres solos
        Map<Long, Boolean> soloStatusMap = new HashMap<>();
        for (List<Employee> group : employeesByRole.values()) {
            boolean isSolo = (group.size() == 1);
            for (Employee emp : group) {
                soloStatusMap.put(emp.getId(), isSolo);
                if (isSolo) {
                    emp.setIsSoloInGroup(true);
                }
            }
        }
        
        List<Schedule> generatedSchedules = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        // Suivi de la dernière affectation par employé pour la rotation
        Map<Long, DayOfWeek> lastAssignedDay = new HashMap<>();
        
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            
            // Ignorer les jours fériés complets
            if (nonWorkingDates.contains(currentDate)) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
            // TFJ : Lundi à Vendredi
            if (dayOfWeek.getValue() >= DayOfWeek.MONDAY.getValue() && dayOfWeek.getValue() <= DayOfWeek.FRIDAY.getValue()) {
                Schedule schedule = assignTFJ(currentDate, employeesByRole, soloStatusMap, 
                                            lastAssignedDay, nonWorkingDays);
                if (schedule != null) {
                    generatedSchedules.add(schedule);
                }
            }
            // Permanence : Samedi
            else if (dayOfWeek == DayOfWeek.SATURDAY) {
                Schedule schedule = assignPermanence(currentDate, employeesByRole, soloStatusMap,
                                                     lastAssignedDay, nonWorkingDays);
                if (schedule != null) {
                    generatedSchedules.add(schedule);
                }
            }
            // Dimanche : pas de planification
            
            currentDate = currentDate.plusDays(1);
        }
        
        return generatedSchedules.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Assigne un employé pour les TFJ (Lundi-Vendredi)
     */
    private Schedule assignTFJ(LocalDate date, Map<String, List<Employee>> employeesByRole,
                               Map<Long, Boolean> soloStatusMap, Map<Long, DayOfWeek> lastAssignedDay,
                               List<NonWorkingDay> nonWorkingDays) {
        
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        // Vérifier les demi-journées fériées
        Optional<NonWorkingDay> maybeHoliday = nonWorkingDays.stream()
                .filter(nwd -> nwd.getDate().equals(date))
                .findFirst();
        
        if (maybeHoliday.isPresent()) {
            NonWorkingDay holiday = maybeHoliday.get();
            if (holiday.isMorningOnly() && dayOfWeek == DayOfWeek.FRIDAY) {
                // Si demi-journée matin, on peut planifier l'après-midi
                // Pour simplification, on saute
                return null;
            }
        }
        
        // Pour les membres solos : uniquement le vendredi
        List<Employee> soloEmployees = employeesByRole.values().stream()
                .filter(group -> group.size() == 1)
                .flatMap(List::stream)
                .filter(Employee::getActive)
                .collect(Collectors.toList());
        
        if (dayOfWeek == DayOfWeek.FRIDAY && !soloEmployees.isEmpty()) {
            // Priorité aux membres solos le vendredi
            for (Employee employee : soloEmployees) {
                if (!scheduleRepository.existsByEmployeeIdAndDate(employee.getId(), date)) {
                    return createSchedule(employee, date, Schedule.ScheduleType.TFJ);
                }
            }
        }
        
        // Pour les autres jours ou si pas de member solo disponible
        List<Employee> eligibleEmployees = new ArrayList<>();
        
        for (List<Employee> group : employeesByRole.values()) {
            if (group.size() > 1) {
                // Groupe avec plusieurs membres
                for (Employee emp : group) {
                    if (emp.getActive() && !emp.getIsSoloInGroup()) {
                        eligibleEmployees.add(emp);
                    }
                }
            }
        }
        
        // Appliquer la règle de non-successivité et de rotation
        Employee selectedEmployee = selectEmployeeWithRotation(eligibleEmployees, date, 
                                                               lastAssignedDay, dayOfWeek);
        
        if (selectedEmployee != null) {
            return createSchedule(selectedEmployee, date, Schedule.ScheduleType.TFJ);
        }
        
        return null;
    }
    
    /**
     * Assigne un employé pour la permanence (Samedi)
     */
    private Schedule assignPermanence(LocalDate date, Map<String, List<Employee>> employeesByRole,
                                      Map<Long, Boolean> soloStatusMap, Map<Long, DayOfWeek> lastAssignedDay,
                                      List<NonWorkingDay> nonWorkingDays) {
        
        // Les membres solos peuvent être programmés le samedi
        List<Employee> soloEmployees = employeesByRole.values().stream()
                .filter(group -> group.size() == 1)
                .flatMap(List::stream)
                .filter(Employee::getActive)
                .collect(Collectors.toList());
        
        if (!soloEmployees.isEmpty()) {
            // Rotation parmi les membres solos
            Employee selectedEmployee = selectEmployeeWithRotation(soloEmployees, date, 
                                                                   lastAssignedDay, DayOfWeek.SATURDAY);
            if (selectedEmployee != null) {
                return createSchedule(selectedEmployee, date, Schedule.ScheduleType.PERMANENCE);
            }
        }
        
        // Sinon, prendre parmi les autres employés
        List<Employee> eligibleEmployees = employeesByRole.values().stream()
                .filter(group -> group.size() > 1)
                .flatMap(List::stream)
                .filter(Employee::getActive)
                .collect(Collectors.toList());
        
        Employee selectedEmployee = selectEmployeeWithRotation(eligibleEmployees, date, 
                                                               lastAssignedDay, DayOfWeek.SATURDAY);
        
        if (selectedEmployee != null) {
            return createSchedule(selectedEmployee, date, Schedule.ScheduleType.PERMANENCE);
        }
        
        return null;
    }
    
    /**
     * Sélectionne un employé en appliquant la rotation anti-chronologique
     * Règle : si un membre est affecté un jour J cette semaine, 
     * la semaine prochaine il sera affecté au jour J-1
     */
    private Employee selectEmployeeWithRotation(List<Employee> candidates, LocalDate date,
                                                Map<Long, DayOfWeek> lastAssignedDay,
                                                DayOfWeek targetDay) {
        
        if (candidates.isEmpty()) {
            return null;
        }
        
        // Filtrer les employés qui n'ont pas été assignés récemment (règle de non-successivité)
        List<Employee> availableCandidates = candidates.stream()
                .filter(emp -> {
                    DayOfWeek lastDay = lastAssignedDay.get(emp.getId());
                    if (lastDay == null) {
                        return true; // Jamais assigné
                    }
                    // Vérifier qu'il n'a pas été assigné la semaine précédente
                    // (logique simplifiée - à améliorer selon besoin)
                    return true;
                })
                .collect(Collectors.toList());
        
        if (availableCandidates.isEmpty()) {
            availableCandidates = candidates; // Fallback
        }
        
        // Trier les candidats selon la rotation
        // Priorité à ceux dont le dernier jour assigné est après le jour cible
        // (pour appliquer la rotation anti-chronologique)
        availableCandidates.sort((e1, e2) -> {
            DayOfWeek day1 = lastAssignedDay.getOrDefault(e1.getId(), DayOfWeek.SUNDAY);
            DayOfWeek day2 = lastAssignedDay.getOrDefault(e2.getId(), DayOfWeek.SUNDAY);
            
            // Calculer la distance de rotation
            int diff1 = calculateRotationDistance(day1, targetDay);
            int diff2 = calculateRotationDistance(day2, targetDay);
            
            return Integer.compare(diff1, diff2);
        });
        
        Employee selected = availableCandidates.get(0);
        lastAssignedDay.put(selected.getId(), targetDay);
        
        log.debug("Employé sélectionné pour le {} : {} (rotation depuis {})", 
                  targetDay, selected.getFullName(), 
                  lastAssignedDay.get(selected.getId()));
        
        return selected;
    }
    
    /**
     * Calcule la distance de rotation entre deux jours
     * Permet d'appliquer la règle : jour suivant = jour précédent de la semaine d'avant
     */
    private int calculateRotationDistance(DayOfWeek lastDay, DayOfWeek targetDay) {
        int lastValue = lastDay.getValue(); // 1=Lundi, 7=Dimanche
        int targetValue = targetDay.getValue();
        
        // Distance anti-chronologique
        int distance = lastValue - targetValue;
        if (distance < 0) {
            distance += 7;
        }
        
        return distance;
    }
    
    /**
     * Crée un nouvel enregistrement de planning
     */
    private Schedule createSchedule(Employee employee, LocalDate date, Schedule.ScheduleType type) {
        return Schedule.builder()
                .employee(employee)
                .date(date)
                .type(type)
                .isConfirmed(false)
                .build();
    }
    
    /**
     * Convertit une entité Schedule en DTO
     */
    private ScheduleResponseDTO convertToResponseDTO(Schedule schedule) {
        Employee emp = schedule.getEmployee();
        return ScheduleResponseDTO.builder()
                .id(schedule.getId())
                .employeeId(emp.getId())
                .employeeFullName(emp.getFullName())
                .employeeEmail(emp.getEmail())
                .roleName(emp.getRole().getName())
                .serviceName(emp.getService().getName())
                .date(schedule.getDate())
                .dayOfWeek(schedule.getDayOfWeek())
                .type(schedule.getType())
                .notes(schedule.getNotes())
                .isConfirmed(schedule.getIsConfirmed())
                .isSoloInGroup(emp.getIsSoloInGroup())
                .build();
    }
}
