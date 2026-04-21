package com.orabank.tfj.service;

import com.orabank.tfj.dto.ScheduleResponseDTO;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    public byte[] exportPlanningToPdf(List<ScheduleResponseDTO> schedules, LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());

            // Titre
            Paragraph title = new Paragraph("Planning TFJ et Permanences - Orabank Togo")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Période
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String period = "Du " + startDate.format(formatter) + " au " + endDate.format(formatter);
            Paragraph periodParagraph = new Paragraph(period)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
            document.add(periodParagraph);

            document.add(new Paragraph("\n"));

            // Regrouper par date
            Map<LocalDate, List<ScheduleResponseDTO>> byDate = schedules.stream()
                .collect(Collectors.groupingBy(ScheduleResponseDTO::getDate));

            // Créer le tableau
            float[] columnWidths = {1, 3, 3, 4, 2};
            Table table = new Table(columnWidths);
            table.setWidth(100f);

            // En-têtes
            String[] headers = {"N°", "Date", "Type", "Employés", "Statut"};
            for (String header : headers) {
                Cell cell = new Cell().add(new Paragraph(header).setBold());
                cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                cell.setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(cell);
            }

            // Données
            int rowNum = 1;
            for (LocalDate date : byDate.keySet().stream().sorted().toList()) {
                List<ScheduleResponseDTO> daySchedules = byDate.get(date);
                
                for (ScheduleResponseDTO schedule : daySchedules) {
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(rowNum++))).setTextAlignment(TextAlignment.CENTER));
                    table.addCell(new Cell().add(new Paragraph(date.format(formatter))));
                    table.addCell(new Cell().add(new Paragraph(schedule.getType() != null ? schedule.getType().name() : "")));
                    
                    String employees = schedule.getEmployeeFullName();
                    table.addCell(new Cell().add(new Paragraph(employees != null ? employees : "")));
                    
                    table.addCell(new Cell().add(new Paragraph("Planifié"))
                        .setTextAlignment(TextAlignment.CENTER));
                }
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'export PDF: " + e.getMessage(), e);
        }
    }
}
