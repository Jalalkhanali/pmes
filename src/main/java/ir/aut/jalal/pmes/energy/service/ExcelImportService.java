package ir.aut.jalal.pmes.energy.service;

import ir.aut.jalal.pmes.energy.entity.EnergyData;
import ir.aut.jalal.pmes.energy.repository.EnergyDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Excel Import Service for energy data
 * Supports importing energy consumption data from Excel files
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final EnergyDataRepository energyDataRepository;

    /**
     * Import energy data from Excel file
     */
    public ImportResult importEnergyData(MultipartFile file, String dataSource) throws IOException {
        log.info("Starting Excel import for file: {}", file.getOriginalFilename());

        List<EnergyData> importedData = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalRows = 0;
        int importedRows = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Use first sheet

            // Find header row
            Row headerRow = findHeaderRow(sheet);
            if (headerRow == null) {
                throw new IllegalArgumentException("No valid header row found in Excel file");
            }

            // Map column indices
            Map<String, Integer> columnMap = mapColumns(headerRow);
            validateRequiredColumns(columnMap);

            // Process data rows
            for (int rowNum = headerRow.getRowNum() + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;

                totalRows++;
                try {
                    EnergyData energyData = parseEnergyDataRow(row, columnMap, dataSource);
                    if (energyData != null) {
                        importedData.add(energyData);
                        importedRows++;
                    }
                } catch (Exception e) {
                    String error = String.format("Row %d: %s", rowNum + 1, e.getMessage());
                    errors.add(error);
                    log.warn("Error parsing row {}: {}", rowNum + 1, e.getMessage());
                }
            }
        }

        // Save imported data
        if (!importedData.isEmpty()) {
            energyDataRepository.saveAll(importedData);
            log.info("Successfully imported {} energy data records", importedData.size());
        }

        return ImportResult.builder()
                .totalRows(totalRows)
                .importedRows(importedRows)
                .errors(errors)
                .dataSource(dataSource)
                .importedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Find the header row in the sheet
     */
    private Row findHeaderRow(Sheet sheet) {
        for (int rowNum = 0; rowNum <= Math.min(10, sheet.getLastRowNum()); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
                    Cell cell = row.getCell(cellNum);
                    if (cell != null && "year".equalsIgnoreCase(cell.getStringCellValue().trim())) {
                        return row;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Map column headers to column indices
     */
    private Map<String, Integer> mapColumns(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        for (int cellNum = 0; cellNum < headerRow.getLastCellNum(); cellNum++) {
            Cell cell = headerRow.getCell(cellNum);
            if (cell != null) {
                String header = cell.getStringCellValue().trim().toLowerCase();
                columnMap.put(header, cellNum);
            }
        }
        
        return columnMap;
    }

    /**
     * Validate that required columns are present
     */
    private void validateRequiredColumns(Map<String, Integer> columnMap) {
        List<String> requiredColumns = Arrays.asList("year", "sector", "energy_source", "consumption_twh");
        
        for (String required : requiredColumns) {
            if (!columnMap.containsKey(required)) {
                throw new IllegalArgumentException("Required column '" + required + "' not found in Excel file");
            }
        }
    }

    /**
     * Parse a single row of energy data
     */
    private EnergyData parseEnergyDataRow(Row row, Map<String, Integer> columnMap, String dataSource) {
        // Extract year
        int year = getIntValue(row, columnMap.get("year"));
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }

        // Extract sector
        String sector = getStringValue(row, columnMap.get("sector"));
        if (sector == null || sector.trim().isEmpty()) {
            throw new IllegalArgumentException("Sector cannot be empty");
        }

        // Extract energy source
        String energySource = getStringValue(row, columnMap.get("energy_source"));
        if (energySource == null || energySource.trim().isEmpty()) {
            throw new IllegalArgumentException("Energy source cannot be empty");
        }

        // Extract consumption
        double consumption = getDoubleValue(row, columnMap.get("consumption_twh"));
        if (consumption < 0) {
            throw new IllegalArgumentException("Consumption cannot be negative");
        }

        // Create EnergyData entity
        return EnergyData.builder()
                .year(year)
                .sector(sector.trim())
                .energySource(energySource.trim())
                .consumptionTwh(consumption)
                .dataSource(dataSource)
                .importedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Get integer value from cell
     */
    private int getIntValue(Row row, Integer columnIndex) {
        if (columnIndex == null) {
            throw new IllegalArgumentException("Column index is null");
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            throw new IllegalArgumentException("Cell is empty");
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse integer from: " + cell.getStringCellValue());
                }
            default:
                throw new IllegalArgumentException("Cell type not supported for integer");
        }
    }

    /**
     * Get string value from cell
     */
    private String getStringValue(Row row, Integer columnIndex) {
        if (columnIndex == null) {
            throw new IllegalArgumentException("Column index is null");
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                throw new IllegalArgumentException("Cell type not supported for string");
        }
    }

    /**
     * Get double value from cell
     */
    private double getDoubleValue(Row row, Integer columnIndex) {
        if (columnIndex == null) {
            throw new IllegalArgumentException("Column index is null");
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            throw new IllegalArgumentException("Cell is empty");
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse double from: " + cell.getStringCellValue());
                }
            default:
                throw new IllegalArgumentException("Cell type not supported for double");
        }
    }

    /**
     * Import result container
     */
    public static class ImportResult {
        private final int totalRows;
        private final int importedRows;
        private final List<String> errors;
        private final String dataSource;
        private final LocalDateTime importedAt;

        private ImportResult(Builder builder) {
            this.totalRows = builder.totalRows;
            this.importedRows = builder.importedRows;
            this.errors = builder.errors;
            this.dataSource = builder.dataSource;
            this.importedAt = builder.importedAt;
        }

        // Getters
        public int getTotalRows() { return totalRows; }
        public int getImportedRows() { return importedRows; }
        public List<String> getErrors() { return errors; }
        public String getDataSource() { return dataSource; }
        public LocalDateTime getImportedAt() { return importedAt; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int totalRows;
            private int importedRows;
            private List<String> errors = new ArrayList<>();
            private String dataSource;
            private LocalDateTime importedAt;

            public Builder totalRows(int totalRows) {
                this.totalRows = totalRows;
                return this;
            }

            public Builder importedRows(int importedRows) {
                this.importedRows = importedRows;
                return this;
            }

            public Builder errors(List<String> errors) {
                this.errors = errors;
                return this;
            }

            public Builder dataSource(String dataSource) {
                this.dataSource = dataSource;
                return this;
            }

            public Builder importedAt(LocalDateTime importedAt) {
                this.importedAt = importedAt;
                return this;
            }

            public ImportResult build() {
                return new ImportResult(this);
            }
        }
    }
} 