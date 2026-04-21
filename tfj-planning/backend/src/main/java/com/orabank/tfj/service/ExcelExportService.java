package com.orabank.tfj.service;

import com.orabank.tfj.dto.ScheduleResponseDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    public byte[] exportPlanningToExcel(List<ScheduleResponseDTO> schedules, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Planning TFJ");

            // Style pour le titre
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Style pour les en-têtes
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Style pour les cellules
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            // Titre
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Planning TFJ et Permanences - Orabank Togo");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            // Période
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String period = "Du " + startDate.format(formatter) + " au " + endDate.format(formatter);
            Row periodRow = sheet.createRow(1);
            Cell periodCell = periodRow.createCell(0);
            periodCell.setCellValue(period);
            periodCell.setCellStyle(cellStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

            // En-têtes du tableau
            Row headerRow = sheet.createRow(3);
            String[] headers = {"N°", "Date", "Type", "Employés", "Statut"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Regrouper par date
            Map<LocalDate, List<ScheduleResponseDTO>> byDate = schedules.stream()
                .collect(Collectors.groupingBy(ScheduleResponseDTO::getDate));

            // Données
            int rowNum = 4;
            int scheduleNum = 1;
            for (LocalDate date : byDate.keySet().stream().sorted().toList()) {
                List<ScheduleResponseDTO> daySchedules = byDate.get(date);
                
                for (ScheduleResponseDTO schedule : daySchedules) {
                    Row row = sheet.createRow(rowNum++);
                    
                    Cell numCell = row.createCell(0);
                    numCell.setCellValue(scheduleNum++);
                    numCell.setCellStyle(cellStyle);

                    Cell dateCell = row.createCell(1);
                    dateCell.setCellValue(date.format(formatter));
                    dateCell.setCellStyle(cellStyle);

                    Cell typeCell = row.createCell(2);
                    typeCell.setCellValue(schedule.getType() != null ? schedule.getType().name() : "");
                    typeCell.setCellStyle(cellStyle);

                    String employees = schedule.getEmployeeFullName();
                    Cell empCell = row.createCell(3);
                    empCell.setCellValue(employees != null ? employees : "");
                    empCell.setCellStyle(cellStyle);

                    Cell statusCell = row.createCell(4);
                    statusCell.setCellValue("Planifié");
                    statusCell.setCellStyle(cellStyle);
                }
            }

            // Ajuster la largeur des colonnes
            sheet.setColumnWidth(0, 5);
            sheet.setColumnWidth(1, 15);
            sheet.setColumnWidth(2, 15);
            sheet.setColumnWidth(3, 40);
            sheet.setColumnWidth(4, 10);

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'export Excel: " + e.getMessage(), e);
        }
    }
}
