package com.orabank.tfj.repository;

import com.orabank.tfj.model.NonWorkingDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NonWorkingDayRepository extends JpaRepository<NonWorkingDay, Long> {
    
    List<NonWorkingDay> findByDateBetweenOrderByDate(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT n FROM NonWorkingDay n WHERE n.date BETWEEN :startDate AND :endDate ORDER BY n.date")
    List<NonWorkingDay> findNonWorkingDaysInPeriod(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
    
    Optional<NonWorkingDay> findByDate(LocalDate date);
    
    boolean existsByDate(LocalDate date);
}
