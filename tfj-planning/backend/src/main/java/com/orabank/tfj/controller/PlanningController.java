package com.orabank.tfj.controller;

import com.orabank.tfj.dto.ScheduleResponseDTO;
import com.orabank.tfj.service.PlanningAlgorithmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Planning", description = "API de génération et consultation des plannings")
public class PlanningController {
    
    private final PlanningAlgorithmService planningService;
    
    @GetMapping("/generate")
    @Operation(summary = "Générer un planning", 
               description = "Génère automatiquement le planning des TFJ et permanences selon les règles métier")
    public ResponseEntity<List<ScheduleResponseDTO>> generatePlanning(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<ScheduleResponseDTO> schedules = planningService.generatePlanning(startDate, endDate);
        return ResponseEntity.ok(schedules);
    }
}
