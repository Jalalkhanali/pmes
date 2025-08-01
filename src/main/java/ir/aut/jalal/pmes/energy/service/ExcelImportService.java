package ir.aut.jalal.pmes.energy.service;

import ir.aut.jalal.pmes.energy.entity.EnergyData;
import ir.aut.jalal.pmes.energy.repository.EnergyDataRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for importing energy data from Excel files
 * Supports both .xlsx and .xls formats using Apache POI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final EnergyDataRepository energyDataRepository;

    /**
     * Import energy data from an Excel file
     * 
     * @param file The Excel file to import
     * @param dataSource Source identifier for the imported data
     * @return ImportResult containing statistics about the import
     */
    @Transactional
    public ImportResult importEnergyData(MultipartFile file, String dataSource) {
        log.info("Starting Excel import for file: {}", file.getOriginalFilename());
        
        ImportResult result = new ImportResult();
        List<EnergyData> energyDataList = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Use first sheet
            
            // Find header row and column indices
            Row headerRow = findHeaderRow(sheet);
            if (headerRow == null) {
                throw new IllegalArgumentException("No header row found in Excel file");
            }
            
            ColumnMapping columnMapping = mapColumns(headerRow);
            
            // Process data rows
            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                
                try {
                    EnergyData energyData = parseEnergyDataRow(row, columnMapping, dataSource);
                    if (energyData != null) {
                        energyDataList.add(energyData);
                        result.incrementProcessedRows();
                    }
                } catch (Exception e) {
                    log.warn("Error parsing row {}: {}", rowIndex + 1, e.getMessage());
                    result.incrementErrorRows();
                }
            }
            
            // Save to database
            if (!energyDataList.isEmpty()) {
                energyDataRepository.saveAll(energyDataList);
                result.setImportedRows(energyDataList.size());
                log.info("Successfully imported {} energy data records", energyDataList.size());
            }
            
        } catch (IOException e) {
            log.error("Error reading Excel file: {}", e.getMessage());
            throw new RuntimeException("Failed to read Excel file", e);
        }
        
        return result;
    }

    /**
     * Find the header row in the Excel sheet
     */
    private Row findHeaderRow(Sheet sheet) {
        for (int rowIndex = 0; rowIndex <= Math.min(10, sheet.getLastRowNum()); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null && isHeaderRow(row)) {
                return row;
            }
        }
        return null;
    }

    /**
     * Check if a row is a header row
     */
    private boolean isHeaderRow(Row row) {
        int headerCount = 0;
        for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
            Cell cell = row.getCell(cellIndex);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String cellValue = cell.getStringCellValue().toLowerCase();
                if (cellValue.contains("year") || cellValue.contains("sector") || 
                    cellValue.contains("energy") || cellValue.contains("consumption")) {
                    headerCount++;
                }
            }
        }
        return headerCount >= 3; // At least 3 expected headers
    }

    /**
     * Map column headers to their indices
     */
    private ColumnMapping mapColumns(Row headerRow) {
        ColumnMapping mapping = new ColumnMapping();
        
        for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
            Cell cell = headerRow.getCell(cellIndex);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String header = cell.getStringCellValue().toLowerCase().trim();
                
                if (header.contains("year")) {
                    mapping.yearColumn = cellIndex;
                } else if (header.contains("sector")) {
                    mapping.sectorColumn = cellIndex;
                } else if (header.contains("energy") && header.contains("source")) {
                    mapping.energySourceColumn = cellIndex;
                } else if (header.contains("consumption") || header.contains("twh")) {
                    mapping.consumptionColumn = cellIndex;
                } else if (header.contains("gdp")) {
                    mapping.gdpColumn = cellIndex;
                } else if (header.contains("population")) {
                    mapping.populationColumn = cellIndex;
                } else if (header.contains("temperature")) {
                    mapping.temperatureColumn = cellIndex;
                } else if (header.contains("note")) {
                    mapping.notesColumn = cellIndex;
                }
            }
        }
        
        // Validate required columns
        if (mapping.yearColumn == -1 || mapping.sectorColumn == -1 || 
            mapping.energySourceColumn == -1 || mapping.consumptionColumn == -1) {
            throw new IllegalArgumentException("Missing required columns: year, sector, energy source, or consumption");
        }
        
        return mapping;
    }

    /**
     * Parse a single row of energy data
     */
    private EnergyData parseEnergyDataRow(Row row, ColumnMapping mapping, String dataSource) {
        // Year
        Integer year = getIntegerCellValue(row.getCell(mapping.yearColumn));
        if (year == null || year < 1900 || year > 2100) {
            return null; // Skip invalid years
        }
        
        // Sector
        String sector = getStringCellValue(row.getCell(mapping.sectorColumn));
        if (sector == null || sector.trim().isEmpty()) {
            return null; // Skip rows without sector
        }
        
        // Energy Source
        String energySource = getStringCellValue(row.getCell(mapping.energySourceColumn));
        if (energySource == null || energySource.trim().isEmpty()) {
            return null; // Skip rows without energy source
        }
        
        // Consumption
        BigDecimal consumption = getBigDecimalCellValue(row.getCell(mapping.consumptionColumn));
        if (consumption == null || consumption.compareTo(BigDecimal.ZERO) <= 0) {
            return null; // Skip invalid consumption values
        }
        
        // Optional fields
        BigDecimal gdp = mapping.gdpColumn != -1 ? getBigDecimalCellValue(row.getCell(mapping.gdpColumn)) : null;
        BigDecimal population = mapping.populationColumn != -1 ? getBigDecimalCellValue(row.getCell(mapping.populationColumn)) : null;
        BigDecimal temperature = mapping.temperatureColumn != -1 ? getBigDecimalCellValue(row.getCell(mapping.temperatureColumn)) : null;
        String notes = mapping.notesColumn != -1 ? getStringCellValue(row.getCell(mapping.notesColumn)) : null;
        
        return EnergyData.builder()
                .year(year)
                .sector(sector.trim())
                .energySource(energySource.trim())
                .consumptionTwh(consumption)
                .gdpBillions(gdp)
                .populationMillions(population)
                .avgTemperatureCelsius(temperature)
                .notes(notes)
                .dataSource(dataSource)
                .build();
    }

    /**
     * Get integer value from cell
     */
    private Integer getIntegerCellValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * Get string value from cell
     */
    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            default:
                return null;
        }
    }

    /**
     * Get BigDecimal value from cell
     */
    private BigDecimal getBigDecimalCellValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                try {
                    return new BigDecimal(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * Column mapping for Excel import
     */
    private static class ColumnMapping {
        int yearColumn = -1;
        int sectorColumn = -1;
        int energySourceColumn = -1;
        int consumptionColumn = -1;
        int gdpColumn = -1;
        int populationColumn = -1;
        int temperatureColumn = -1;
        int notesColumn = -1;
    }

    /**
     * Result of Excel import operation
     */
    public static class ImportResult {
        private int processedRows = 0;
        private int importedRows = 0;
        private int errorRows = 0;
        private LocalDateTime importTime = LocalDateTime.now();

        public void incrementProcessedRows() {
            processedRows++;
        }

        public void incrementErrorRows() {
            errorRows++;
        }

        // Getters and setters
        public int getProcessedRows() { return processedRows; }
        public int getImportedRows() { return importedRows; }
        public int getErrorRows() { return errorRows; }
        public LocalDateTime getImportTime() { return importTime; }
        
        public void setImportedRows(int importedRows) { this.importedRows = importedRows; }
    }
} 