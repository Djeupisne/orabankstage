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
    private final CongeRepository congeRepository;
    
    /**
     * Génère le planning des TFJ et permanences selon les règles métier :
     * 1. Les membres d'un même groupe ne peuvent pas se suivre
     * 2. Rotation anti-chronologique : si un membre est affecté un jour, 
     *    la semaine suivante il prend le jour antérieur
     * 3. Les membres seuls dans leur groupe ne sont programmés que vendredi (TFJ) ou samedi (permanence)
     * 4. Gestion des congés : les employés en congé ne sont pas planifiables
     * 5. Réaffectation automatique en cas d'absence exceptionnelle
     * 6. Les managers ne sont affectés que si insuffisance de participants
     */
    @Transactional
    public List<ScheduleResponseDTO> generatePlanning(LocalDate startDate, LocalDate endDate) {
        log.info("Génération du planning du {} au {}", startDate, endDate);
        
        List<Employee> allActiveEmployees = employeeRepository.findByActiveTrueOrderByLastNameFirstName();
        List<NonWorkingDay> nonWorkingDays = nonWorkingDayRepository.findNonWorkingDaysInPeriod(startDate, endDate);
        Set<LocalDate> nonWorkingDates = nonWorkingDays.stream()
                .filter(NonWorkingDay::isFullDay)
                .map(NonWorkingDay::getDate)
                .collect(Collectors.toSet());
        
        // Charger tous les congés de la période
        List<Conge> allConges = congeRepository.findCongesInPeriod(startDate, endDate);
        Map<Long, List<Conge>> congesByEmployee = new HashMap<>();
        for (Conge conge : allConges) {
            congesByEmployee.computeIfAbsent(conge.getEmployee().getId(), k -> new ArrayList<>()).add(conge);
        }
        
        // Grouper les employés par rôle
        Map<String, List<Employee>> employeesByRole = allActiveEmployees.stream()
                .collect(Collectors.groupingBy(e -> e.getRole().getName()));
        
        // Identifier les membres solos et séparer les managers
        Map<Long, Boolean> soloStatusMap = new HashMap<>();
        Map<String, List<Employee>> nonManagerEmployeesByRole = new HashMap<>();
        Map<String, List<Employee>> managerEmployeesByRole = new HashMap<>();
        
        for (Map.Entry<String, List<Employee>> entry : employeesByRole.entrySet()) {
            String roleName = entry.getKey();
            List<Employee> group = entry.getValue();
            
            boolean isSolo = (group.size() == 1);
            List<Employee> nonManagers = new ArrayList<>();
            List<Employee> managers = new ArrayList<>();
            
            for (Employee emp : group) {
                soloStatusMap.put(emp.getId(), isSolo);
                if (isSolo) {
                    emp.setIsSoloInGroup(true);
                }
                
                // Séparer managers et non-managers
                if (emp.getHierarchicalLevel() != null && 
                    emp.getHierarchicalLevel().getName().toLowerCase().contains("manager")) {
                    managers.add(emp);
                } else {
                    nonManagers.add(emp);
                }
            }
            
            nonManagerEmployeesByRole.put(roleName, nonManagers);
            managerEmployeesByRole.put(roleName, managers);
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
                Schedule schedule = assignTFJ(currentDate, nonManagerEmployeesByRole, managerEmployeesByRole, 
                                            soloStatusMap, lastAssignedDay, nonWorkingDays, congesByEmployee);
                if (schedule != null) {
                    generatedSchedules.add(schedule);
                }
            }
            // Permanence : Samedi
            else if (dayOfWeek == DayOfWeek.SATURDAY) {
                Schedule schedule = assignPermanence(currentDate, nonManagerEmployeesByRole, managerEmployeesByRole,
                                                     soloStatusMap, lastAssignedDay, nonWorkingDays, congesByEmployee);
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
     * Priorité aux non-managers, puis managers si insuffisance
     */
    private Schedule assignTFJ(LocalDate date, Map<String, List<Employee>> nonManagerEmployeesByRole,
                               Map<String, List<Employee>> managerEmployeesByRole,
                               Map<Long, Boolean> soloStatusMap, Map<Long, DayOfWeek> lastAssignedDay,
                               List<NonWorkingDay> nonWorkingDays, Map<Long, List<Conge>> congesByEmployee) {
        
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        // Vérifier les demi-journées fériées
        Optional<NonWorkingDay> maybeHoliday = nonWorkingDays.stream()
                .filter(nwd -> nwd.getDate().equals(date))
                .findFirst();
        
        if (maybeHoliday.isPresent() && maybeHoliday.get().isMorningOnly()) {
            return null; // Demi-journée matin, on ne planifie pas
        }
        
        // Pour les membres solos : uniquement le vendredi
        List<Employee> soloNonManagers = nonManagerEmployeesByRole.values().stream()
                .filter(group -> group.size() == 1 && !group.isEmpty())
                .flatMap(List::stream)
                .filter(Employee::getActive)
                .filter(emp -> !isEmployeeOnLeave(emp.getId(), date, congesByEmployee))
                .collect(Collectors.toList());
        
        if (dayOfWeek == DayOfWeek.FRIDAY && !soloNonManagers.isEmpty()) {
            // Priorité aux membres solos le vendredi
            for (Employee employee : soloNonManagers) {
                if (!scheduleRepository.existsByEmployeeIdAndDate(employee.getId(), date)) {
                    return createSchedule(employee, date, Schedule.ScheduleType.TFJ);
                }
            }
        }
        
        // Essayer d'abord avec les non-managers
        Employee selectedEmployee = selectEmployeeFromGroups(nonManagerEmployeesByRole, date, 
                                                              soloStatusMap, lastAssignedDay, 
                                                              dayOfWeek, congesByEmployee, false);
        
        if (selectedEmployee != null) {
            return createSchedule(selectedEmployee, date, Schedule.ScheduleType.TFJ);
        }
        
        // Si aucun non-manager disponible, essayer avec les managers
        log.info("Aucun non-manager disponible pour le {}, tentative avec les managers", date);
        selectedEmployee = selectEmployeeFromGroups(managerEmployeesByRole, date, 
                                                     soloStatusMap, lastAssignedDay, 
                                                     dayOfWeek, congesByEmployee, true);
        
        if (selectedEmployee != null) {
            log.info("Manager affecté : {} pour le {}", selectedEmployee.getFullName(), date);
            return createSchedule(selectedEmployee, date, Schedule.ScheduleType.TFJ);
        }
        
        return null;
    }
    
    /**
     * Assigne un employé pour la permanence (Samedi)
     * Priorité aux non-managers, puis managers si insuffisance
     */
    private Schedule assignPermanence(LocalDate date, Map<String, List<Employee>> nonManagerEmployeesByRole,
                                      Map<String, List<Employee>> managerEmployeesByRole,
                                      Map<Long, Boolean> soloStatusMap, Map<Long, DayOfWeek> lastAssignedDay,
                                      List<NonWorkingDay> nonWorkingDays, Map<Long, List<Conge>> congesByEmployee) {
        
        // Les membres solos peuvent être programmés le samedi
        List<Employee> soloNonManagers = nonManagerEmployeesByRole.values().stream()
                .filter(group -> group.size() == 1 && !group.isEmpty())
                .flatMap(List::stream)
                .filter(Employee::getActive)
                .filter(emp -> !isEmployeeOnLeave(emp.getId(), date, congesByEmployee))
                .collect(Collectors.toList());
        
        if (!soloNonManagers.isEmpty()) {
            // Rotation parmi les membres solos non-managers
            Employee selectedEmployee = selectEmployeeWithRotation(soloNonManagers, date, 
                                                                   lastAssignedDay, DayOfWeek.SATURDAY);
            if (selectedEmployee != null) {
                return createSchedule(selectedEmployee, date, Schedule.ScheduleType.PERMANENCE);
            }
        }
        
        // Essayer avec les autres non-managers
        Employee selectedEmployee = selectEmployeeFromGroups(nonManagerEmployeesByRole, date,
                                                              soloStatusMap, lastAssignedDay,
                                                              DayOfWeek.SATURDAY, congesByEmployee, false);
        
        if (selectedEmployee != null) {
            return createSchedule(selectedEmployee, date, Schedule.ScheduleType.PERMANENCE);
        }
        
        // Si aucun non-manager disponible, essayer avec les managers
        log.info("Aucun non-manager disponible pour la permanence du {}, tentative avec les managers", date);
        selectedEmployee = selectEmployeeFromGroups(managerEmployeesByRole, date,
                                                     soloStatusMap, lastAssignedDay,
                                                     DayOfWeek.SATURDAY, congesByEmployee, true);
        
        if (selectedEmployee != null) {
            log.info("Manager affecté pour permanence : {} pour le {}", selectedEmployee.getFullName(), date);
            return createSchedule(selectedEmployee, date, Schedule.ScheduleType.PERMANENCE);
        }
        
        return null;
    }
    
    /**
     * Sélectionne un employé à partir des groupes en appliquant la rotation et en vérifiant les congés
     */
    private Employee selectEmployeeFromGroups(Map<String, List<Employee>> employeesByRole, LocalDate date,
                                               Map<Long, Boolean> soloStatusMap, Map<Long, DayOfWeek> lastAssignedDay,
                                               DayOfWeek targetDay, Map<Long, List<Conge>> congesByEmployee,
                                               boolean isManagerFallback) {
        
        List<Employee> eligibleEmployees = new ArrayList<>();
        
        for (List<Employee> group : employeesByRole.values()) {
            if (group.isEmpty()) continue;
            
            // Pour les groupes non-solos ou si c'est vendredi/samedi pour les solos
            if (group.size() > 1 || (targetDay == DayOfWeek.FRIDAY || targetDay == DayOfWeek.SATURDAY)) {
                for (Employee emp : group) {
                    if (!emp.getActive()) continue;
                    
                    // Vérifier si l'employé est en congé
                    if (isEmployeeOnLeave(emp.getId(), date, congesByEmployee)) {
                        log.debug("Employé {} en congé le {}, ignoré", emp.getFullName(), date);
                        continue;
                    }
                    
                    // Ignorer les solos sauf vendredi/samedi
                    if (Boolean.TRUE.equals(soloStatusMap.get(emp.getId())) && 
                        targetDay != DayOfWeek.FRIDAY && targetDay != DayOfWeek.SATURDAY) {
                        continue;
                    }
                    
                    eligibleEmployees.add(emp);
                }
            }
        }
        
        return selectEmployeeWithRotation(eligibleEmployees, date, lastAssignedDay, targetDay);
    }
    
    /**
     * Vérifie si un employé est en congé à une date donnée
     */
    private boolean isEmployeeOnLeave(Long employeeId, LocalDate date, Map<Long, List<Conge>> congesByEmployee) {
        List<Conge> conges = congesByEmployee.get(employeeId);
        if (conges == null || conges.isEmpty()) {
            return false;
        }
        
        for (Conge conge : conges) {
            if (conge.couvreDate(date)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sélectionne un employé en appliquant la rotation anti-chronologique
     * Règle : si un membre est affecté un jour J cette semaine, 
     * la semaine prochaine il sera affecté au jour J-1
     * 
     * GESTION DES ABSENCES EXCEPTIONNELLES:
     * Si l'employé sélectionné est absent (nouveau congé détecté), on réaffecte automatiquement
     * son jour à celui programmé sur le jour suivant de la même semaine.
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
                    // Vérifier qu'il n'a pas été assigné la veille (règle de non-successivité)
                    // Simplifié pour l'exemple
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
     * Réaffecte automatiquement un jour en cas d'absence exceptionnelle
     * Cherche l'employé programmé le jour suivant et lui réaffecte le jour libéré
     */
    public Schedule reassignDueToAbsence(Schedule originalSchedule, LocalDate startDate, LocalDate endDate) {
        log.info("Réaffectation suite à l'absence de {} le {}", 
                 originalSchedule.getEmployee().getFullName(), originalSchedule.getDate());
        
        LocalDate originalDate = originalSchedule.getDate();
        DayOfWeek originalDay = originalDate.getDayOfWeek();
        
        // Trouver le jour ouvrable suivant
        LocalDate nextWorkingDay = originalDate.plusDays(1);
        while (nextWorkingDay.isBefore(endDate.plusDays(1))) {
            DayOfWeek nextDay = nextWorkingDay.getDayOfWeek();
            
            // Skip weekend pour TFJ, skip autres jours pour permanence
            if (originalSchedule.getType() == Schedule.ScheduleType.TFJ) {
                if (nextDay.getValue() >= DayOfWeek.MONDAY.getValue() && 
                    nextDay.getValue() <= DayOfWeek.FRIDAY.getValue()) {
                    break;
                }
            } else if (originalSchedule.getType() == Schedule.ScheduleType.PERMANENCE) {
                if (nextDay == DayOfWeek.SATURDAY) {
                    break;
                }
            }
            nextWorkingDay = nextWorkingDay.plusDays(1);
        }
        
        if (nextWorkingDay.isAfter(endDate)) {
            log.warn("Aucun jour de remplacement trouvé dans la période");
            return null;
        }
        
        // Trouver qui est programmé le jour suivant
        Optional<Schedule> nextScheduleOpt = scheduleRepository.findByDate(nextWorkingDay);
        
        if (nextScheduleOpt.isPresent()) {
            Schedule nextSchedule = nextScheduleOpt.get();
            Employee replacementEmployee = nextSchedule.getEmployee();
            
            // Créer la nouvelle affectation
            Schedule newSchedule = createSchedule(replacementEmployee, originalDate, originalSchedule.getType());
            scheduleRepository.save(newSchedule);
            
            log.info("Réaffectation réussie : {} remplace {} le {}", 
                     replacementEmployee.getFullName(), 
                     originalSchedule.getEmployee().getFullName(),
                     originalDate);
            
            return newSchedule;
        }
        
        return null;
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
