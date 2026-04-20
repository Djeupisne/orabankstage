package com.orabank.tfj.repository;

import com.orabank.tfj.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    List<Schedule> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
    
    List<Schedule> findByEmployeeIdAndDateBetweenOrderByDateAsc(Long employeeId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT s FROM Schedule s WHERE s.date BETWEEN :startDate AND :endDate ORDER BY s.date ASC")
    List<Schedule> findSchedulesInPeriod(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    @Query("SELECT s FROM Schedule s JOIN s.employee e WHERE e.id = :employeeId " +
           "AND s.date BETWEEN :startDate AND :endDate ORDER BY s.date DESC")
    List<Schedule> findEmployeeSchedulesInPeriod(@Param("employeeId") Long employeeId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(s) > 0 FROM Schedule s WHERE s.employee.id = :employeeId AND s.date = :date")
    boolean existsByEmployeeIdAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);
    
    List<Schedule> findByDateAndType(LocalDate date, Schedule.ScheduleType type);
}
