package aut.energy.controller;

import aut.energy.entity.ForecastResult;
import aut.energy.entity.Scenario;
import aut.energy.service.EmissionService;
import aut.energy.service.ExcelImportService;
import aut.energy.service.NeuralNetworkService;
import aut.energy.service.ScenarioService;
import ir.aut.jalal.pmes.energy.service.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for energy planning operations
 * Provides endpoints for data import, forecasting, scenario management, and emissions calculation
 */
@RestController
@RequestMapping("/api/energy")
@RequiredArgsConstructor
@Slf4j
public class EnergyController {

    private final ExcelImportService excelImportService;
    private final NeuralNetworkService neuralNetworkService;
    private final ScenarioService scenarioService;
    private final EmissionService emissionService;

    // ==================== EXCEL IMPORT ENDPOINTS ====================

    /**
     * Upload and import energy data from Excel file
     * 
     * @param file The Excel file to import
     * @param dataSource Source identifier for the imported data
     * @return Import result with statistics
     */
    @PostMapping("/import/excel")
    public ResponseEntity<ExcelImportService.ImportResult> importExcelData(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dataSource", defaultValue = "Excel Import") String dataSource) {
        
        log.info("Received Excel import request for file: {}", file.getOriginalFilename());
        
        try {
            ExcelImportService.ImportResult result = excelImportService.importEnergyData(file, dataSource);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error importing Excel data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    // ==================== FORECASTING ENDPOINTS ====================

    /**
     * Forecast baseline energy demand
     * 
     * @param request The forecasting request
     * @return List of forecast results
     */
    @PostMapping("/forecast/baseline")
    public ResponseEntity<List<ForecastResult>> forecastBaseline(
            @RequestBody BaselineForecastRequest request) {
        
        log.info("Received baseline forecast request for sectors: {}, energy sources: {}", 
                request.getSectors(), request.getEnergySources());
        
        try {
            // Create or get baseline scenario
            Scenario baselineScenario = getOrCreateBaselineScenario(request.getStartYear(), request.getEndYear());
            
            List<ForecastResult> results =
                    neuralNetworkService.forecastEnergyDemand(
                            baselineScenario, 
                            request.getSectors(), 
                            request.getEnergySources(), 
                            request.getForecastYears());
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error in baseline forecasting: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create and manage scenarios
     * 
     * @param scenario The scenario to create
     * @return The created scenario
     */
    @PostMapping("/scenarios")
    public ResponseEntity<Scenario> createScenario(@RequestBody Scenario scenario) {
        log.info("Creating scenario: {}", scenario.getName());
        
        try {
            Scenario createdScenario = scenarioService.createScenario(scenario);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdScenario);
        } catch (Exception e) {
            log.error("Error creating scenario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get all scenarios
     * 
     * @return List of all scenarios
     */
    @GetMapping("/scenarios")
    public ResponseEntity<List<Scenario>> getAllScenarios() {
        try {
            List<Scenario> scenarios = scenarioService.getAllScenarios();
            return ResponseEntity.ok(scenarios);
        } catch (Exception e) {
            log.error("Error getting scenarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get scenario by ID
     * 
     * @param id The scenario ID
     * @return The scenario if found
     */
    @GetMapping("/scenarios/{id}")
    public ResponseEntity<Scenario> getScenarioById(@PathVariable Long id) {
        try {
            Optional<Scenario> scenario = scenarioService.getScenarioById(id);
            return scenario.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting scenario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update scenario
     * 
     * @param id The scenario ID
     * @param scenario The updated scenario data
     * @return The updated scenario
     */
    @PutMapping("/scenarios/{id}")
    public ResponseEntity<Scenario> updateScenario(@PathVariable Long id, @RequestBody Scenario scenario) {
        try {
            Scenario updatedScenario = scenarioService.updateScenario(id, scenario);
            return ResponseEntity.ok(updatedScenario);
        } catch (Exception e) {
            log.error("Error updating scenario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Delete scenario
     * 
     * @param id The scenario ID
     * @return No content if successful
     */
    @DeleteMapping("/scenarios/{id}")
    public ResponseEntity<Void> deleteScenario(@PathVariable Long id) {
        try {
            scenarioService.deleteScenario(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting scenario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Activate scenario
     * 
     * @param id The scenario ID to activate
     * @return The activated scenario
     */
    @PostMapping("/scenarios/{id}/activate")
    public ResponseEntity<Scenario> activateScenario(@PathVariable Long id) {
        try {
            Scenario activatedScenario = scenarioService.activateScenario(id);
            return ResponseEntity.ok(activatedScenario);
        } catch (Exception e) {
            log.error("Error activating scenario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Run forecast under a specific scenario
     * 
     * @param scenarioId The scenario ID
     * @param request The forecasting request
     * @return List of forecast results
     */
    @PostMapping("/scenarios/{scenarioId}/forecast")
    public ResponseEntity<List<ForecastResult>> forecastUnderScenario(
            @PathVariable Long scenarioId,
            @RequestBody ScenarioForecastRequest request) {
        
        log.info("Received scenario forecast request for scenario ID: {}", scenarioId);
        
        try {
            Optional<Scenario> scenarioOpt = scenarioService.getScenarioById(scenarioId);
            if (scenarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Scenario scenario = scenarioOpt.get();
            List<ForecastResult> results =
                    neuralNetworkService.forecastEnergyDemand(
                            scenario, 
                            request.getSectors(), 
                            request.getEnergySources(), 
                            request.getForecastYears());
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error in scenario forecasting: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== EMISSIONS ENDPOINTS ====================

    /**
     * Calculate emissions for a scenario
     * 
     * @param scenarioId The scenario ID
     * @return List of emission calculations
     */
    @GetMapping("/scenarios/{scenarioId}/emissions")
    public ResponseEntity<List<EmissionService.EmissionCalculation>> calculateEmissionsForScenario(
            @PathVariable Long scenarioId) {
        
        try {
            List<EmissionService.EmissionCalculation> calculations = 
                    emissionService.calculateEmissionsForScenario(scenarioId);
            return ResponseEntity.ok(calculations);
        } catch (Exception e) {
            log.error("Error calculating emissions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Calculate emissions for a scenario within a year range
     * 
     * @param scenarioId The scenario ID
     * @param startYear Start year
     * @param endYear End year
     * @return List of emission calculations
     */
    @GetMapping("/scenarios/{scenarioId}/emissions/range")
    public ResponseEntity<List<EmissionService.EmissionCalculation>> calculateEmissionsForYearRange(
            @PathVariable Long scenarioId,
            @RequestParam Integer startYear,
            @RequestParam Integer endYear) {
        
        try {
            List<EmissionService.EmissionCalculation> calculations = 
                    emissionService.calculateEmissionsForYearRange(scenarioId, startYear, endYear);
            return ResponseEntity.ok(calculations);
        } catch (Exception e) {
            log.error("Error calculating emissions for year range: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get total emissions by year for a scenario
     * 
     * @param scenarioId The scenario ID
     * @return Map of year to total emissions
     */
    @GetMapping("/scenarios/{scenarioId}/emissions/yearly")
    public ResponseEntity<Map<Integer, EmissionService.EmissionSummary>> getTotalEmissionsByYear(
            @PathVariable Long scenarioId) {
        
        try {
            Map<Integer, EmissionService.EmissionSummary> yearlyEmissions = 
                    emissionService.calculateTotalEmissionsByYear(scenarioId);
            return ResponseEntity.ok(yearlyEmissions);
        } catch (Exception e) {
            log.error("Error getting yearly emissions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get emissions by sector for a scenario
     * 
     * @param scenarioId The scenario ID
     * @return Map of sector to emissions
     */
    @GetMapping("/scenarios/{scenarioId}/emissions/sector")
    public ResponseEntity<Map<String, EmissionService.EmissionSummary>> getEmissionsBySector(
            @PathVariable Long scenarioId) {
        
        try {
            Map<String, EmissionService.EmissionSummary> sectorEmissions = 
                    emissionService.calculateEmissionsBySector(scenarioId);
            return ResponseEntity.ok(sectorEmissions);
        } catch (Exception e) {
            log.error("Error getting sector emissions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get emissions by energy source for a scenario
     * 
     * @param scenarioId The scenario ID
     * @return Map of energy source to emissions
     */
    @GetMapping("/scenarios/{scenarioId}/emissions/energy-source")
    public ResponseEntity<Map<String, EmissionService.EmissionSummary>> getEmissionsByEnergySource(
            @PathVariable Long scenarioId) {
        
        try {
            Map<String, EmissionService.EmissionSummary> energySourceEmissions = 
                    emissionService.calculateEmissionsByEnergySource(scenarioId);
            return ResponseEntity.ok(energySourceEmissions);
        } catch (Exception e) {
            log.error("Error getting energy source emissions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Compare emissions between two scenarios
     * 
     * @param scenario1Id First scenario ID
     * @param scenario2Id Second scenario ID
     * @return Emission comparison results
     */
    @GetMapping("/emissions/compare")
    public ResponseEntity<EmissionService.EmissionComparison> compareScenarios(
            @RequestParam Long scenario1Id,
            @RequestParam Long scenario2Id) {
        
        try {
            EmissionService.EmissionComparison comparison = 
                    emissionService.compareScenarios(scenario1Id, scenario2Id);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            log.error("Error comparing scenarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== COMBINED ENDPOINTS ====================

    /**
     * Combined endpoint for scenario-based forecast with emissions
     * 
     * @param scenarioId The scenario ID
     * @param request The combined request
     * @return Combined forecast and emissions results
     */
    @PostMapping("/scenarios/{scenarioId}/forecast-with-emissions")
    public ResponseEntity<CombinedForecastResponse> forecastWithEmissions(
            @PathVariable Long scenarioId,
            @RequestBody CombinedForecastRequest request) {
        
        log.info("Received combined forecast request for scenario ID: {}", scenarioId);
        
        try {
            Optional<Scenario> scenarioOpt = scenarioService.getScenarioById(scenarioId);
            if (scenarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Scenario scenario = scenarioOpt.get();
            
            // Run forecast
            List<ForecastResult> forecastResults =
                    neuralNetworkService.forecastEnergyDemand(
                            scenario, 
                            request.getSectors(), 
                            request.getEnergySources(), 
                            request.getForecastYears());
            
            // Calculate emissions
            List<EmissionService.EmissionCalculation> emissionCalculations = 
                    emissionService.calculateEmissionsForScenario(scenarioId);
            
            CombinedForecastResponse response = CombinedForecastResponse.builder()
                    .scenario(scenario)
                    .forecastResults(forecastResults)
                    .emissionCalculations(emissionCalculations)
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in combined forecast: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get or create baseline scenario
     */
    private Scenario getOrCreateBaselineScenario(Integer startYear, Integer endYear) {
        List<Scenario> baselineScenarios = scenarioService.getBaselineScenarios();
        if (!baselineScenarios.isEmpty()) {
            return baselineScenarios.get(0);
        }
        
        return scenarioService.createBaselineScenario("Baseline", startYear, endYear);
    }

    // ==================== REQUEST/RESPONSE DTOs ====================

    /**
     * Baseline forecast request
     */
    public static class BaselineForecastRequest {
        private List<String> sectors;
        private List<String> energySources;
        private Integer forecastYears;
        private Integer startYear;
        private Integer endYear;

        // Getters and setters
        public List<String> getSectors() { return sectors; }
        public void setSectors(List<String> sectors) { this.sectors = sectors; }
        
        public List<String> getEnergySources() { return energySources; }
        public void setEnergySources(List<String> energySources) { this.energySources = energySources; }
        
        public Integer getForecastYears() { return forecastYears; }
        public void setForecastYears(Integer forecastYears) { this.forecastYears = forecastYears; }
        
        public Integer getStartYear() { return startYear; }
        public void setStartYear(Integer startYear) { this.startYear = startYear; }
        
        public Integer getEndYear() { return endYear; }
        public void setEndYear(Integer endYear) { this.endYear = endYear; }
    }

    /**
     * Scenario forecast request
     */
    public static class ScenarioForecastRequest {
        private List<String> sectors;
        private List<String> energySources;
        private Integer forecastYears;

        // Getters and setters
        public List<String> getSectors() { return sectors; }
        public void setSectors(List<String> sectors) { this.sectors = sectors; }
        
        public List<String> getEnergySources() { return energySources; }
        public void setEnergySources(List<String> energySources) { this.energySources = energySources; }
        
        public Integer getForecastYears() { return forecastYears; }
        public void setForecastYears(Integer forecastYears) { this.forecastYears = forecastYears; }
    }

    /**
     * Combined forecast request
     */
    public static class CombinedForecastRequest {
        private List<String> sectors;
        private List<String> energySources;
        private Integer forecastYears;

        // Getters and setters
        public List<String> getSectors() { return sectors; }
        public void setSectors(List<String> sectors) { this.sectors = sectors; }
        
        public List<String> getEnergySources() { return energySources; }
        public void setEnergySources(List<String> energySources) { this.energySources = energySources; }
        
        public Integer getForecastYears() { return forecastYears; }
        public void setForecastYears(Integer forecastYears) { this.forecastYears = forecastYears; }
    }

    /**
     * Combined forecast response
     */
    public static class CombinedForecastResponse {
        private Scenario scenario;
        private List<ForecastResult> forecastResults;
        private List<EmissionService.EmissionCalculation> emissionCalculations;

        // Builder pattern
        public static CombinedForecastResponseBuilder builder() {
            return new CombinedForecastResponseBuilder();
        }

        // Getters and setters
        public Scenario getScenario() { return scenario; }
        public void setScenario(Scenario scenario) { this.scenario = scenario; }
        
        public List<ForecastResult> getForecastResults() { return forecastResults; }
        public void setForecastResults(List<ForecastResult> forecastResults) { this.forecastResults = forecastResults; }
        
        public List<EmissionService.EmissionCalculation> getEmissionCalculations() { return emissionCalculations; }
        public void setEmissionCalculations(List<EmissionService.EmissionCalculation> emissionCalculations) { this.emissionCalculations = emissionCalculations; }

        public static class CombinedForecastResponseBuilder {
            private CombinedForecastResponse response = new CombinedForecastResponse();

            public CombinedForecastResponseBuilder scenario(Scenario scenario) {
                response.scenario = scenario;
                return this;
            }

            public CombinedForecastResponseBuilder forecastResults(List<ForecastResult> forecastResults) {
                response.forecastResults = forecastResults;
                return this;
            }

            public CombinedForecastResponseBuilder emissionCalculations(List<EmissionService.EmissionCalculation> emissionCalculations) {
                response.emissionCalculations = emissionCalculations;
                return this;
            }

            public CombinedForecastResponse build() {
                return response;
            }
        }
    }
} 