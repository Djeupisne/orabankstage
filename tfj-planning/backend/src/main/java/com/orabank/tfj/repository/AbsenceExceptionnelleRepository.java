package com.orabank.tfj.repository;

import com.orabank.tfj.model.AbsenceExceptionnelle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceExceptionnelleRepository extends JpaRepository<AbsenceExceptionnelle, Long> {

    /**
     * Trouve toutes les absences exceptionnelles qui chevauchent une période donnée
     */
    @Query("SELECT ae FROM AbsenceExceptionnelle ae WHERE ae.employee.id = :employeeId " +
           "AND ae.dateFin >= :startDate AND ae.dateDebut <= :endDate")
    List<AbsenceExceptionnelle> findAbsencesByEmployeeAndPeriod(@Param("employeeId") Long employeeId,
                                                                 @Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate);

    /**
     * Trouve toutes les absences exceptionnelles actives dans une période
     */
    @Query("SELECT ae FROM AbsenceExceptionnelle ae WHERE ae.actif = true AND ae.dateFin >= :startDate AND ae.dateDebut <= :endDate")
    List<AbsenceExceptionnelle> findAbsencesInPeriod(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    /**
     * Vérifie si un employé est en absence exceptionnelle à une date donnée
     */
    @Query("SELECT COUNT(ae) > 0 FROM AbsenceExceptionnelle ae WHERE ae.employee.id = :employeeId " +
           "AND ae.dateDebut <= :date AND ae.dateFin >= :date")
    boolean isEmployeeOnExceptionalLeave(@Param("employeeId") Long employeeId,
                                          @Param("date") LocalDate date);

    /**
     * Trouve les absences par motif
     */
    List<AbsenceExceptionnelle> findByMotif(String motif);

    /**
     * Trouve les absences d'un employé triées par date de début
     */
    List<AbsenceExceptionnelle> findByEmployeeIdOrderByDateDebutDesc(Long employeeId);
}
