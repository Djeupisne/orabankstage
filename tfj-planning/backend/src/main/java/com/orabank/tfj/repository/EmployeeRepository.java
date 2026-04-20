package com.orabank.tfj.repository;

import com.orabank.tfj.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    @Query("SELECT e FROM Employee e WHERE e.active = true ORDER BY e.lastName, e.firstName")
    List<Employee> findByActiveTrueOrderByLastNameFirstName();
    
    List<Employee> findByRoleIdAndActiveTrue(Long roleId);
    
    List<Employee> findByServiceIdAndActiveTrue(Long serviceId);
    
    @Query("SELECT e FROM Employee e WHERE e.active = true AND e.isSoloInGroup = :isSolo")
    List<Employee> findBySoloInGroupStatus(@Param("isSolo") Boolean isSolo);
    
    @Query("SELECT e FROM Employee e JOIN e.role r WHERE e.active = true AND r.name = :roleName")
    List<Employee> findByRoleNameAndActive(@Param("roleName") String roleName);
    
    boolean existsByEmail(String email);
}
