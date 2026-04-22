package com.orabank.tfj.controller;

import com.orabank.tfj.dto.ScheduleResponseDTO;
import com.orabank.tfj.service.ExcelExportService;
import com.orabank.tfj.service.PdfExportService;
import com.orabank.tfj.service.PlanningAlgorithmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
@Tag(name = "Planning", description = "API de génération et consultation des plannings")
public class PlanningController {
    
    private final PlanningAlgorithmService planningService;
    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;
    
    @GetMapping("/generate")
    @Operation(summary = "Générer un planning", 
               description = "Génère automatiquement le planning des TFJ et permanences selon les règles métier")
    public ResponseEntity<List<ScheduleResponseDTO>> generatePlanning(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<ScheduleResponseDTO> schedules = planningService.generatePlanning(startDate, endDate);
        return ResponseEntity.ok(schedules);
    }
    
    @GetMapping("/export/pdf")
    @Operation(summary = "Exporter en PDF", 
               description = "Exporte le planning au format PDF")
    public ResponseEntity<byte[]> exportPlanningToPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<ScheduleResponseDTO> schedules = planningService.generatePlanning(startDate, endDate);
        byte[] pdfContent = pdfExportService.exportPlanningToPdf(schedules, startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "planning-tfj.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
    }
    
    @GetMapping("/export/excel")
    @Operation(summary = "Exporter en Excel", 
               description = "Exporte le planning au format Excel (.xlsx)")
    public ResponseEntity<byte[]> exportPlanningToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<ScheduleResponseDTO> schedules = planningService.generatePlanning(startDate, endDate);
        byte[] excelContent = excelExportService.exportPlanningToExcel(schedules, startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "planning-tfj.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
    }
}
