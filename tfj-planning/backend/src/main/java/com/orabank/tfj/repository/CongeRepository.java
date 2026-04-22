package com.orabank.tfj.repository;

import com.orabank.tfj.model.Conge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CongeRepository extends JpaRepository<Conge, Long> {

    /**
     * Trouve tous les congés qui chevauchent une période donnée
     */
    @Query("SELECT c FROM Conge c WHERE c.employee.id = :employeeId " +
           "AND c.dateFin >= :startDate AND c.dateDebut <= :endDate")
    List<Conge> findCongesByEmployeeAndPeriod(@Param("employeeId") Long employeeId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * Trouve tous les congés actifs dans une période
     */
    @Query("SELECT c FROM Conge c WHERE c.dateFin >= :startDate AND c.dateDebut <= :endDate")
    List<Conge> findCongesInPeriod(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    /**
     * Vérifie si un employé est en congé à une date donnée
     */
    @Query("SELECT COUNT(c) > 0 FROM Conge c WHERE c.employee.id = :employeeId " +
           "AND c.dateDebut <= :date AND c.dateFin >= :date")
    boolean isEmployeeOnLeave(@Param("employeeId") Long employeeId,
                               @Param("date") LocalDate date);

    /**
     * Trouve un congé par employé et date de début
     */
    List<Conge> findByEmployeeAndDateDebut(com.orabank.tfj.model.Employee employee, LocalDate dateDebut);
}
