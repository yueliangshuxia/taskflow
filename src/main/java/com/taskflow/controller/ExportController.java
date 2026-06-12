package com.taskflow.controller;

import com.taskflow.dto.TaskDto;
import com.taskflow.service.TaskService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final TaskService taskService;

    @GetMapping("/project/{projectId}/tasks/excel")
    public void exportTasksToExcel(@PathVariable Long projectId,
                                    Authentication auth,
                                    HttpServletResponse response) throws IOException {
        var tasks = taskService.findByProjectId(projectId,
                org.springframework.data.domain.PageRequest.of(0, 5000,
                        org.springframework.data.domain.Sort.by("createdAt")));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("任务列表");

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFont(createFont(workbook, true, IndexedColors.WHITE.getIndex()));

        // Headers
        String[] headers = {"ID", "标题", "状态", "优先级", "负责人", "截止日期", "创建时间"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        int rowNum = 1;
        for (TaskDto task : tasks.getContent()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(task.getId() != null ? task.getId() : 0);
            row.createCell(1).setCellValue(task.getTitle());
            row.createCell(2).setCellValue(task.getStatus() != null ? task.getStatus().getDisplayName() : "");
            row.createCell(3).setCellValue(task.getPriority() != null ? task.getPriority().getDisplayName() : "");
            row.createCell(4).setCellValue(task.getAssigneeName() != null ? task.getAssigneeName() : "");
            row.createCell(5).setCellValue(task.getDueDate() != null ? task.getDueDate().format(dateFmt) : "");
            row.createCell(6).setCellValue(task.getCreatedAt() != null ? task.getCreatedAt().format(dtFmt) : "");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/project/{projectId}/tasks/csv")
    public void exportTasksToCsv(@PathVariable Long projectId,
                                  Authentication auth,
                                  HttpServletResponse response) throws IOException {
        var tasks = taskService.findByProjectId(projectId,
                org.springframework.data.domain.PageRequest.of(0, 5000,
                        org.springframework.data.domain.Sort.by("createdAt")));

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.csv");
        var writer = response.getWriter();

        writer.println("ID,标题,状态,优先级,负责人,截止日期,创建时间");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (TaskDto task : tasks.getContent()) {
            writer.println(String.join(",",
                    String.valueOf(task.getId()),
                    escapeCsv(task.getTitle()),
                    task.getStatus() != null ? task.getStatus().getDisplayName() : "",
                    task.getPriority() != null ? task.getPriority().getDisplayName() : "",
                    escapeCsv(task.getAssigneeName()),
                    task.getDueDate() != null ? task.getDueDate().format(dateFmt) : "",
                    ""
            ));
        }
        writer.flush();
    }

    private Font createFont(Workbook workbook, boolean bold, short colorIndex) {
        Font font = workbook.createFont();
        font.setBold(bold);
        font.setColor(colorIndex);
        return font;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
