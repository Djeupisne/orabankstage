package com.orabank.tfj.service;

import com.orabank.tfj.dto.DashboardStatsDTO;
import com.orabank.tfj.model.Employee;
import com.orabank.tfj.model.Schedule;
import com.orabank.tfj.repository.EmployeeRepository;
import com.orabank.tfj.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final ScheduleRepository scheduleRepository;

    public DashboardStatsDTO getDashboardStats() {
        List<Employee> allEmployees = employeeRepository.findAll();
        List<Schedule> allSchedules = scheduleRepository.findAll();

        long activeEmployees = allEmployees.stream().filter(Employee::getActive).count();
        long inactiveEmployees = allEmployees.size() - activeEmployees;

        long tfjSchedules = allSchedules.stream()
            .filter(s -> "TFJ".equals(s.getType()))
            .count();
        long permanenceSchedules = allSchedules.stream()
            .filter(s -> "PERMANENCE".equals(s.getType()))
            .count();

        return DashboardStatsDTO.builder()
            .totalEmployees((long) allEmployees.size())
            .activeEmployees(activeEmployees)
            .inactiveEmployees(inactiveEmployees)
            .totalSchedules((long) allSchedules.size())
            .tfjSchedules(tfjSchedules)
            .permanenceSchedules(permanenceSchedules)
            .build();
    }
}
