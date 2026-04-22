package com.orabank.tfj.config;

import com.orabank.tfj.model.*;
import com.orabank.tfj.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration pour charger automatiquement les données de test au démarrage.
 * Crée un utilisateur unique avec des identifiants prédéfinis et insère les données de base.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    @Bean
    @Profile({"dev", "prod"})
    CommandLineRunner initData(ServiceRepository serviceRepository,
                                HierarchicalLevelRepository hierarchicalLevelRepository,
                                EmployeeRepository employeeRepository,
                                NonWorkingDayRepository nonWorkingDayRepository,
                                CongeRepository congeRepository,
                                AbsenceExceptionnelleRepository absenceExceptionnelleRepository) {
        
        return args -> {
            log.info("=== Démarrage du chargement des données de test ===");

            // Charger les données de référence (vérification individuelle pour éviter les doublons)
            log.info("Insertion des données de référence (si elles n'existent pas déjà)...");
            
            // 1. Services DSI - création seulement si n'existe pas
            Service applicationsService = serviceRepository.findByName("Applications")
                .orElseGet(() -> {
                    log.info("Création du service 'Applications'...");
                    return serviceRepository.save(Service.builder()
                        .name("Applications")
                        .description("Service des applications métier")
                        .build());
                });
            
            Service infrastructureService = serviceRepository.findByName("Infrastructure")
                .orElseGet(() -> {
                    log.info("Création du service 'Infrastructure'...");
                    return serviceRepository.save(Service.builder()
                        .name("Infrastructure")
                        .description("Service infrastructure réseau et système")
                        .build());
                });
            
            Service exploitationService = serviceRepository.findByName("Exploitation")
                .orElseGet(() -> {
                    log.info("Création du service 'Exploitation'...");
                    return serviceRepository.save(Service.builder()
                        .name("Exploitation")
                        .description("Service exploitation et maintenance")
                        .build());
                });
            
            long serviceCount = serviceRepository.count();
            log.info("✓ {} services disponibles", serviceCount);

            // 2. Niveaux hiérarchiques - création seulement si n'existe pas
            HierarchicalLevel collaborateurLevel = hierarchicalLevelRepository.findByName("Collaborateur")
                .orElseGet(() -> {
                    log.info("Création du niveau 'Collaborateur'...");
                    return hierarchicalLevelRepository.save(HierarchicalLevel.builder()
                        .name("Collaborateur")
                        .description("Niveau collaborateur")
                        .levelOrder(1)
                        .build());
                });
            
            HierarchicalLevel cadreLevel = hierarchicalLevelRepository.findByName("Cadre")
                .orElseGet(() -> {
                    log.info("Création du niveau 'Cadre'...");
                    return hierarchicalLevelRepository.save(HierarchicalLevel.builder()
                        .name("Cadre")
                        .description("Niveau cadre")
                        .levelOrder(2)
                        .build());
                });
            
            HierarchicalLevel managerLevel = hierarchicalLevelRepository.findByName("Manager")
                .orElseGet(() -> {
                    log.info("Création du niveau 'Manager'...");
                    return hierarchicalLevelRepository.save(HierarchicalLevel.builder()
                        .name("Manager")
                        .description("Niveau manager")
                        .levelOrder(3)
                        .build());
                });
            
            long levelCount = hierarchicalLevelRepository.count();
            log.info("✓ {} niveaux hiérarchiques disponibles", levelCount);

            // 3. Employés - création seulement si n'existe pas (vérification par email)
            if (employeeRepository.findByEmail("k.amenyonor@orabank.tg").isEmpty()) {
                log.info("Création des employés...");
                List<Employee> employees = Arrays.asList(
                    // Applications
                    Employee.builder().firstName("Kofi").lastName("AMENYONOR").email("k.amenyonor@orabank.tg")
                        .service(applicationsService).hierarchicalLevel(collaborateurLevel)
                        .active(true).isSoloInGroup(false).build(),
                    Employee.builder().firstName("Ama").lastName("TCHASSAN").email("a.tchassan@orabank.tg")
                        .service(applicationsService).hierarchicalLevel(collaborateurLevel)
                        .active(true).isSoloInGroup(false).build(),
                    Employee.builder().firstName("Komlan").lastName("ADJEKPLE").email("k.adjekple@orabank.tg")
                        .service(applicationsService).hierarchicalLevel(cadreLevel)
                        .active(true).isSoloInGroup(false).build(),
                    
                    // Infrastructure
                    Employee.builder().firstName("Folly").lastName("GANOU").email("f.ganou@orabank.tg")
                        .service(infrastructureService).hierarchicalLevel(collaborateurLevel)
                        .active(true).isSoloInGroup(false).build(),
                    Employee.builder().firstName("Esso").lastName("PITANG").email("e.pitang@orabank.tg")
                        .service(infrastructureService).hierarchicalLevel(collaborateurLevel)
                        .active(true).isSoloInGroup(false).build(),
                    Employee.builder().firstName("Akoussivi").lastName("BANITOKE").email("a.banitoke@orabank.tg")
                        .service(infrastructureService).hierarchicalLevel(collaborateurLevel)
                        .active(true).isSoloInGroup(false).build(),
                    
                    // Exploitation
                    Employee.builder().firstName("Dodzi").lastName("KPESSOU").email("d.kpessou@orabank.tg")
                        .service(exploitationService).hierarchicalLevel(collaborateurLevel)
                        .active(true).isSoloInGroup(false).build(),
                    Employee.builder().firstName("Amétévé").lastName("KOKOU").email("a.kokou@orabank.tg")
                        .service(exploitationService).hierarchicalLevel(collaborateurLevel)
                        .active(true).isSoloInGroup(false).build(),
                    Employee.builder().firstName("Mana").lastName("ASSOGNA").email("m.assogna@orabank.tg")
                        .service(exploitationService).hierarchicalLevel(managerLevel)
                        .active(true).isSoloInGroup(false).build()
                );
                employeeRepository.saveAll(employees);
                log.info("✓ {} employés créés", employees.size());
            } else {
                log.info("✓ Les employés existent déjà");
            }

            // Récupérer TOUS les employés nécessaires pour les congés et absences
            // Si un employé n'existe pas, on le récupère plus tard après création
            Employee kofiAmenyonor = employeeRepository.findByEmail("k.amenyonor@orabank.tg").orElse(null);
            Employee follyGanou = employeeRepository.findByEmail("f.ganou@orabank.tg").orElse(null);
            Employee amaTchassan = employeeRepository.findByEmail("a.tchassan@orabank.tg").orElse(null);
            Employee essoPitang = employeeRepository.findByEmail("e.pitang@orabank.tg").orElse(null);
            Employee akoussiviBanitoke = employeeRepository.findByEmail("a.banitoke@orabank.tg").orElse(null);
            Employee dodziKpessou = employeeRepository.findByEmail("d.kpessou@orabank.tg").orElse(null);
            Employee ameteveKokou = employeeRepository.findByEmail("a.kokou@orabank.tg").orElse(null);
            Employee manaAssogna = employeeRepository.findByEmail("m.assogna@orabank.tg").orElse(null);

            // 4. Jours fériés 2026 - création seulement si n'existe pas (vérification par date)
            if (nonWorkingDayRepository.findByDate(LocalDate.of(2026, 1, 1)).isEmpty()) {
                log.info("Création des jours fériés 2026...");
                List<NonWorkingDay> nonWorkingDays = Arrays.asList(
                    NonWorkingDay.builder().date(LocalDate.of(2026, 1, 1)).name("Jour de l'an").type(NonWorkingDay.NonWorkingDayType.FULL_DAY).build(),
                    NonWorkingDay.builder().date(LocalDate.of(2026, 1, 13)).name("Fête du Ramadan").type(NonWorkingDay.NonWorkingDayType.FULL_DAY).build(),
                    NonWorkingDay.builder().date(LocalDate.of(2026, 4, 3)).name("Vendredi Saint").type(NonWorkingDay.NonWorkingDayType.FULL_DAY).build(),
                    NonWorkingDay.builder().date(LocalDate.of(2026, 4, 6)).name("Lundi de Pâques").type(NonWorkingDay.NonWorkingDayType.FULL_DAY).build(),
                    NonWorkingDay.builder().date(LocalDate.of(2026, 5, 1)).name("Fête du Travail").type(NonWorkingDay.NonWorkingDayType.FULL_DAY).build(),
                    NonWorkingDay.builder().date(LocalDate.of(2026, 5, 14)).name("Ascension").type(NonWorkingDay.NonWorkingDayType.FULL_DAY).build(),
                    NonWorkingDay.builder().date(LocalDate.of(2026, 5, 25)).name("Lundi de Pentecôte").type(NonWorkingDay.NonWorkingDayType.FULL_DAY).build(),
                    NonWorkingDay.builder().date(LocalDate.of(2026, 6, 4)).name("Fête de Tabaski").type(NonWorkingDay.NonWorkingDayType.FULL_DAY).build(),
                    NonWorkingDay.builder().date(LocalDate.of(2026, 8, 15)).name("Assomption").type(NonWorkingDay.NonWorkingDayType.FULL_DAY).build(),
                    NonWorkingDay.builder().date(LocalDate.of(2026, 11, 1)).name("Toussaint").type(NonWorkingDay.NonWorkingDayType.FULL_DAY).build(),
                    NonWorkingDay.builder().date(LocalDate.of(2026, 12, 25)).name("Noël").type(NonWorkingDay.NonWorkingDayType.FULL_DAY).build(),
                    NonWorkingDay.builder().date(LocalDate.of(2026, 12, 26)).name("Lendemain de Noël").type(NonWorkingDay.NonWorkingDayType.FULL_DAY).build(),
                    // Demi-journées
                    NonWorkingDay.builder().date(LocalDate.of(2026, 12, 24)).name("Veille de Noël (après-midi)").type(NonWorkingDay.NonWorkingDayType.MORNING_ONLY).build(),
                    NonWorkingDay.builder().date(LocalDate.of(2026, 12, 31)).name("Veille du Nouvel An (après-midi)").type(NonWorkingDay.NonWorkingDayType.MORNING_ONLY).build()
                );
                nonWorkingDayRepository.saveAll(nonWorkingDays);
                log.info("✓ {} jours fériés créés", nonWorkingDays.size());
            } else {
                log.info("✓ Les jours fériés existent déjà");
            }

            // 6. Congés de test - création seulement si n'existe pas
            if (kofiAmenyonor != null && congeRepository.findByEmployeeAndDateDebut(kofiAmenyonor, LocalDate.of(2026, 6, 15)).isEmpty()) {
                log.info("Création des congés de test...");
                List<Conge> conges = Arrays.asList(
                    // Congé annuel pour Kofi AMENYONOR du 15 au 19 juin 2026
                    Conge.builder()
                        .employee(kofiAmenyonor)
                        .dateDebut(LocalDate.of(2026, 6, 15))
                        .dateFin(LocalDate.of(2026, 6, 19))
                        .estDemiJourneeDebut(false)
                        .estDemiJourneeFin(false)
                        .typeConge("CONGES_PAYES")
                        .build(),
                    // Congé pour Folly GANOU du 22 au 26 juin 2026
                    Conge.builder()
                        .employee(follyGanou)
                        .dateDebut(LocalDate.of(2026, 6, 22))
                        .dateFin(LocalDate.of(2026, 6, 26))
                        .estDemiJourneeDebut(false)
                        .estDemiJourneeFin(false)
                        .typeConge("CONGES_PAYES")
                        .build(),
                    // Demi-journée de congé pour Ama TCHASSAN (matin du 8 juin 2026)
                    Conge.builder()
                        .employee(amaTchassan)
                        .dateDebut(LocalDate.of(2026, 6, 8))
                        .dateFin(LocalDate.of(2026, 6, 8))
                        .estDemiJourneeDebut(true)
                        .estDemiJourneeFin(false)
                        .typeConge("CONGES_PAYES")
                        .build()
                );
                congeRepository.saveAll(conges);
                log.info("✓ {} congés créés", conges.size());
            } else {
                log.info("✓ Les congés de test existent déjà");
            }

            // 7. Absences exceptionnelles de test - création seulement si n'existe pas
            // Recharger les employés depuis la base pour s'assurer qu'ils existent
            Employee essoPitangFinal = employeeRepository.findByEmail("e.pitang@orabank.tg").orElse(null);
            Employee akoussiviBanitokeFinal = employeeRepository.findByEmail("a.banitoke@orabank.tg").orElse(null);
            
            if (essoPitangFinal != null && absenceExceptionnelleRepository.findByEmployeeAndDateDebut(essoPitangFinal, LocalDate.of(2026, 6, 10)).isEmpty()) {
                log.info("Création des absences exceptionnelles de test...");
                List<AbsenceExceptionnelle> absences = Arrays.asList(
                    // Absence maladie pour Esso PITANG du 10 au 12 juin 2026
                    AbsenceExceptionnelle.builder()
                        .employee(essoPitangFinal)
                        .dateDebut(LocalDate.of(2026, 6, 10))
                        .dateFin(LocalDate.of(2026, 6, 12))
                        .estDemiJourneeDebut(false)
                        .estDemiJourneeFin(false)
                        .motif("MALADIE")
                        .commentaire("Grippe avec fièvre")
                        .saisiPar("Admin DSI")
                        .dateSaisie(LocalDate.now())
                        .estReaffectationAuto(true)
                        .build(),
                    // Absence imprévue pour Akoussivi BANITOKE (demi-journée le 15 juin 2026)
                    AbsenceExceptionnelle.builder()
                        .employee(akoussiviBanitokeFinal)
                        .dateDebut(LocalDate.of(2026, 6, 15))
                        .dateFin(LocalDate.of(2026, 6, 15))
                        .estDemiJourneeDebut(true)
                        .estDemiJourneeFin(false)
                        .motif("IMPREVU")
                        .commentaire("Problème de transport")
                        .saisiPar("Admin DSI")
                        .dateSaisie(LocalDate.now())
                        .estReaffectationAuto(true)
                        .build()
                );
                absenceExceptionnelleRepository.saveAll(absences);
                log.info("✓ {} absences exceptionnelles créées", absences.size());
            } else {
                log.info("✓ Les absences exceptionnelles existent déjà ou employés non trouvés");
            }

            log.info("=== Chargement des données de test terminé avec succès ===");
        };
    }
}
