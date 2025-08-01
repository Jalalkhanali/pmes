package ir.aut.jalal.pmes.energy.service;

import ir.aut.jalal.pmes.energy.entity.EmissionFactor;
import ir.aut.jalal.pmes.energy.entity.ForecastResult;
import ir.aut.jalal.pmes.energy.repository.EmissionFactorRepository;
import ir.aut.jalal.pmes.energy.repository.ForecastResultRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for calculating emissions from energy consumption
 * Uses emission factors to calculate CO2, NOx, SO2, and other emissions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmissionService {

    private final EmissionFactorRepository emissionFactorRepository;
    private final ForecastResultRepository forecastResultRepository;

    /**
     * Calculate emissions for a specific forecast result
     * 
     * @param forecastResult The forecast result to calculate emissions for
     * @return EmissionCalculation containing calculated emissions
     */
    public EmissionCalculation calculateEmissions(ForecastResult forecastResult) {
        log.debug("Calculating emissions for forecast: {} - {} - {}", 
                 forecastResult.getSector(), forecastResult.getEnergySource(), forecastResult.getForecastYear());
        
        // Get emission factor for this sector and energy source
        Optional<EmissionFactor> emissionFactorOpt = emissionFactorRepository
                .findMostRecentByEnergySourceAndSector(forecastResult.getEnergySource(), forecastResult.getSector());
        
        if (emissionFactorOpt.isEmpty()) {
            log.warn("No emission factor found for {} - {}", forecastResult.getEnergySource(), forecastResult.getSector());
            return createZeroEmissionCalculation(forecastResult);
        }
        
        EmissionFactor emissionFactor = emissionFactorOpt.get();
        
        // Calculate emissions
        BigDecimal co2Emissions = calculateCO2Emissions(forecastResult, emissionFactor);
        BigDecimal noxEmissions = calculateNOxEmissions(forecastResult, emissionFactor);
        BigDecimal so2Emissions = calculateSO2Emissions(forecastResult, emissionFactor);
        BigDecimal ch4Emissions = calculateCH4Emissions(forecastResult, emissionFactor);
        BigDecimal n2oEmissions = calculateN2OEmissions(forecastResult, emissionFactor);
        
        return EmissionCalculation.builder()
                .forecastResult(forecastResult)
                .emissionFactor(emissionFactor)
                .co2EmissionsKg(co2Emissions)
                .noxEmissionsKg(noxEmissions)
                .so2EmissionsKg(so2Emissions)
                .ch4EmissionsKg(ch4Emissions)
                .n2oEmissionsKg(n2oEmissions)
                .totalEmissionsKg(co2Emissions.add(noxEmissions).add(so2Emissions).add(ch4Emissions).add(n2oEmissions))
                .build();
    }

    /**
     * Calculate emissions for all forecast results in a scenario
     * 
     * @param scenarioId The scenario ID
     * @return List of emission calculations
     */
    public List<EmissionCalculation> calculateEmissionsForScenario(Long scenarioId) {
        log.info("Calculating emissions for scenario ID: {}", scenarioId);
        
        // Get all forecast results for the scenario
        List<ForecastResult> forecastResults = forecastResultRepository.findByScenarioId(scenarioId);
        
        return forecastResults.stream()
                .map(this::calculateEmissions)
                .toList();
    }

    /**
     * Calculate emissions for forecast results within a year range
     * 
     * @param scenarioId The scenario ID
     * @param startYear Start year
     * @param endYear End year
     * @return List of emission calculations
     */
    public List<EmissionCalculation> calculateEmissionsForYearRange(Long scenarioId, Integer startYear, Integer endYear) {
        log.info("Calculating emissions for scenario {} from {} to {}", scenarioId, startYear, endYear);
        
        // Get forecast results for the year range
        List<ForecastResult> forecastResults = forecastResultRepository
                .findByScenarioIdAndForecastYearBetweenOrderByForecastYearAsc(scenarioId, startYear, endYear);
        
        return forecastResults.stream()
                .map(this::calculateEmissions)
                .toList();
    }

    /**
     * Calculate total emissions by year for a scenario
     * 
     * @param scenarioId The scenario ID
     * @return Map of year to total emissions
     */
    public Map<Integer, EmissionSummary> calculateTotalEmissionsByYear(Long scenarioId) {
        log.info("Calculating total emissions by year for scenario: {}", scenarioId);
        
        List<EmissionCalculation> calculations = calculateEmissionsForScenario(scenarioId);
        
        Map<Integer, EmissionSummary> yearlyEmissions = new HashMap<>();
        
        for (EmissionCalculation calculation : calculations) {
            Integer year = calculation.getForecastResult().getForecastYear();
            
            yearlyEmissions.computeIfAbsent(year, k -> new EmissionSummary())
                    .addEmissions(calculation);
        }
        
        return yearlyEmissions;
    }

    /**
     * Calculate emissions by sector for a scenario
     * 
     * @param scenarioId The scenario ID
     * @return Map of sector to emissions
     */
    public Map<String, EmissionSummary> calculateEmissionsBySector(Long scenarioId) {
        log.info("Calculating emissions by sector for scenario: {}", scenarioId);
        
        List<EmissionCalculation> calculations = calculateEmissionsForScenario(scenarioId);
        
        Map<String, EmissionSummary> sectorEmissions = new HashMap<>();
        
        for (EmissionCalculation calculation : calculations) {
            String sector = calculation.getForecastResult().getSector();
            
            sectorEmissions.computeIfAbsent(sector, k -> new EmissionSummary())
                    .addEmissions(calculation);
        }
        
        return sectorEmissions;
    }

    /**
     * Calculate emissions by energy source for a scenario
     * 
     * @param scenarioId The scenario ID
     * @return Map of energy source to emissions
     */
    public Map<String, EmissionSummary> calculateEmissionsByEnergySource(Long scenarioId) {
        log.info("Calculating emissions by energy source for scenario: {}", scenarioId);
        
        List<EmissionCalculation> calculations = calculateEmissionsForScenario(scenarioId);
        
        Map<String, EmissionSummary> energySourceEmissions = new HashMap<>();
        
        for (EmissionCalculation calculation : calculations) {
            String energySource = calculation.getForecastResult().getEnergySource();
            
            energySourceEmissions.computeIfAbsent(energySource, k -> new EmissionSummary())
                    .addEmissions(calculation);
        }
        
        return energySourceEmissions;
    }

    /**
     * Compare emissions between two scenarios
     * 
     * @param scenario1Id First scenario ID
     * @param scenario2Id Second scenario ID
     * @return EmissionComparison containing the comparison results
     */
    public EmissionComparison compareScenarios(Long scenario1Id, Long scenario2Id) {
        log.info("Comparing emissions between scenarios {} and {}", scenario1Id, scenario2Id);
        
        Map<Integer, EmissionSummary> emissions1 = calculateTotalEmissionsByYear(scenario1Id);
        Map<Integer, EmissionSummary> emissions2 = calculateTotalEmissionsByYear(scenario2Id);
        
        return EmissionComparison.builder()
                .scenario1Id(scenario1Id)
                .scenario2Id(scenario2Id)
                .scenario1Emissions(emissions1)
                .scenario2Emissions(emissions2)
                .build();
    }

    /**
     * Calculate CO2 emissions
     */
    private BigDecimal calculateCO2Emissions(ForecastResult forecastResult, EmissionFactor emissionFactor) {
        if (emissionFactor.getCo2Factor() == null) {
            return BigDecimal.ZERO;
        }
        
        return forecastResult.getForecastedConsumptionTwh()
                .multiply(emissionFactor.getCo2Factor())
                .setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * Calculate NOx emissions
     */
    private BigDecimal calculateNOxEmissions(ForecastResult forecastResult, EmissionFactor emissionFactor) {
        if (emissionFactor.getNoxFactor() == null) {
            return BigDecimal.ZERO;
        }
        
        return forecastResult.getForecastedConsumptionTwh()
                .multiply(emissionFactor.getNoxFactor())
                .setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * Calculate SO2 emissions
     */
    private BigDecimal calculateSO2Emissions(ForecastResult forecastResult, EmissionFactor emissionFactor) {
        if (emissionFactor.getSo2Factor() == null) {
            return BigDecimal.ZERO;
        }
        
        return forecastResult.getForecastedConsumptionTwh()
                .multiply(emissionFactor.getSo2Factor())
                .setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * Calculate CH4 emissions
     */
    private BigDecimal calculateCH4Emissions(ForecastResult forecastResult, EmissionFactor emissionFactor) {
        if (emissionFactor.getCh4Factor() == null) {
            return BigDecimal.ZERO;
        }
        
        return forecastResult.getForecastedConsumptionTwh()
                .multiply(emissionFactor.getCh4Factor())
                .setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * Calculate N2O emissions
     */
    private BigDecimal calculateN2OEmissions(ForecastResult forecastResult, EmissionFactor emissionFactor) {
        if (emissionFactor.getN2oFactor() == null) {
            return BigDecimal.ZERO;
        }
        
        return forecastResult.getForecastedConsumptionTwh()
                .multiply(emissionFactor.getN2oFactor())
                .setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * Create zero emission calculation when no emission factor is found
     */
    private EmissionCalculation createZeroEmissionCalculation(ForecastResult forecastResult) {
        return EmissionCalculation.builder()
                .forecastResult(forecastResult)
                .emissionFactor(null)
                .co2EmissionsKg(BigDecimal.ZERO)
                .noxEmissionsKg(BigDecimal.ZERO)
                .so2EmissionsKg(BigDecimal.ZERO)
                .ch4EmissionsKg(BigDecimal.ZERO)
                .n2oEmissionsKg(BigDecimal.ZERO)
                .totalEmissionsKg(BigDecimal.ZERO)
                .build();
    }

    /**
     * Emission calculation result
     */
    public static class EmissionCalculation {
        private ForecastResult forecastResult;
        private EmissionFactor emissionFactor;
        private BigDecimal co2EmissionsKg;
        private BigDecimal noxEmissionsKg;
        private BigDecimal so2EmissionsKg;
        private BigDecimal ch4EmissionsKg;
        private BigDecimal n2oEmissionsKg;
        private BigDecimal totalEmissionsKg;

        // Builder pattern
        public static EmissionCalculationBuilder builder() {
            return new EmissionCalculationBuilder();
        }

        // Getters and setters
        public ForecastResult getForecastResult() { return forecastResult; }
        public void setForecastResult(ForecastResult forecastResult) { this.forecastResult = forecastResult; }
        
        public EmissionFactor getEmissionFactor() { return emissionFactor; }
        public void setEmissionFactor(EmissionFactor emissionFactor) { this.emissionFactor = emissionFactor; }
        
        public BigDecimal getCo2EmissionsKg() { return co2EmissionsKg; }
        public void setCo2EmissionsKg(BigDecimal co2EmissionsKg) { this.co2EmissionsKg = co2EmissionsKg; }
        
        public BigDecimal getNoxEmissionsKg() { return noxEmissionsKg; }
        public void setNoxEmissionsKg(BigDecimal noxEmissionsKg) { this.noxEmissionsKg = noxEmissionsKg; }
        
        public BigDecimal getSo2EmissionsKg() { return so2EmissionsKg; }
        public void setSo2EmissionsKg(BigDecimal so2EmissionsKg) { this.so2EmissionsKg = so2EmissionsKg; }
        
        public BigDecimal getCh4EmissionsKg() { return ch4EmissionsKg; }
        public void setCh4EmissionsKg(BigDecimal ch4EmissionsKg) { this.ch4EmissionsKg = ch4EmissionsKg; }
        
        public BigDecimal getN2oEmissionsKg() { return n2oEmissionsKg; }
        public void setN2oEmissionsKg(BigDecimal n2oEmissionsKg) { this.n2oEmissionsKg = n2oEmissionsKg; }
        
        public BigDecimal getTotalEmissionsKg() { return totalEmissionsKg; }
        public void setTotalEmissionsKg(BigDecimal totalEmissionsKg) { this.totalEmissionsKg = totalEmissionsKg; }

        public static class EmissionCalculationBuilder {
            private EmissionCalculation calculation = new EmissionCalculation();

            public EmissionCalculationBuilder forecastResult(ForecastResult forecastResult) {
                calculation.forecastResult = forecastResult;
                return this;
            }

            public EmissionCalculationBuilder emissionFactor(EmissionFactor emissionFactor) {
                calculation.emissionFactor = emissionFactor;
                return this;
            }

            public EmissionCalculationBuilder co2EmissionsKg(BigDecimal co2EmissionsKg) {
                calculation.co2EmissionsKg = co2EmissionsKg;
                return this;
            }

            public EmissionCalculationBuilder noxEmissionsKg(BigDecimal noxEmissionsKg) {
                calculation.noxEmissionsKg = noxEmissionsKg;
                return this;
            }

            public EmissionCalculationBuilder so2EmissionsKg(BigDecimal so2EmissionsKg) {
                calculation.so2EmissionsKg = so2EmissionsKg;
                return this;
            }

            public EmissionCalculationBuilder ch4EmissionsKg(BigDecimal ch4EmissionsKg) {
                calculation.ch4EmissionsKg = ch4EmissionsKg;
                return this;
            }

            public EmissionCalculationBuilder n2oEmissionsKg(BigDecimal n2oEmissionsKg) {
                calculation.n2oEmissionsKg = n2oEmissionsKg;
                return this;
            }

            public EmissionCalculationBuilder totalEmissionsKg(BigDecimal totalEmissionsKg) {
                calculation.totalEmissionsKg = totalEmissionsKg;
                return this;
            }

            public EmissionCalculation build() {
                return calculation;
            }
        }
    }

    /**
     * Emission summary for aggregating emissions
     */
    public static class EmissionSummary {
        private BigDecimal totalCo2Kg = BigDecimal.ZERO;
        private BigDecimal totalNoxKg = BigDecimal.ZERO;
        private BigDecimal totalSo2Kg = BigDecimal.ZERO;
        private BigDecimal totalCh4Kg = BigDecimal.ZERO;
        private BigDecimal totalN2oKg = BigDecimal.ZERO;
        private BigDecimal totalEmissionsKg = BigDecimal.ZERO;
        private int calculationCount = 0;

        public void addEmissions(EmissionCalculation calculation) {
            this.totalCo2Kg = this.totalCo2Kg.add(calculation.getCo2EmissionsKg());
            this.totalNoxKg = this.totalNoxKg.add(calculation.getNoxEmissionsKg());
            this.totalSo2Kg = this.totalSo2Kg.add(calculation.getSo2EmissionsKg());
            this.totalCh4Kg = this.totalCh4Kg.add(calculation.getCh4EmissionsKg());
            this.totalN2oKg = this.totalN2oKg.add(calculation.getN2oEmissionsKg());
            this.totalEmissionsKg = this.totalEmissionsKg.add(calculation.getTotalEmissionsKg());
            this.calculationCount++;
        }

        // Getters
        public BigDecimal getTotalCo2Kg() { return totalCo2Kg; }
        public BigDecimal getTotalNoxKg() { return totalNoxKg; }
        public BigDecimal getTotalSo2Kg() { return totalSo2Kg; }
        public BigDecimal getTotalCh4Kg() { return totalCh4Kg; }
        public BigDecimal getTotalN2oKg() { return totalN2oKg; }
        public BigDecimal getTotalEmissionsKg() { return totalEmissionsKg; }
        public int getCalculationCount() { return calculationCount; }
    }

    /**
     * Emission comparison between scenarios
     */
    public static class EmissionComparison {
        private Long scenario1Id;
        private Long scenario2Id;
        private Map<Integer, EmissionSummary> scenario1Emissions;
        private Map<Integer, EmissionSummary> scenario2Emissions;

        // Builder pattern
        public static EmissionComparisonBuilder builder() {
            return new EmissionComparisonBuilder();
        }

        // Getters and setters
        public Long getScenario1Id() { return scenario1Id; }
        public void setScenario1Id(Long scenario1Id) { this.scenario1Id = scenario1Id; }
        
        public Long getScenario2Id() { return scenario2Id; }
        public void setScenario2Id(Long scenario2Id) { this.scenario2Id = scenario2Id; }
        
        public Map<Integer, EmissionSummary> getScenario1Emissions() { return scenario1Emissions; }
        public void setScenario1Emissions(Map<Integer, EmissionSummary> scenario1Emissions) { this.scenario1Emissions = scenario1Emissions; }
        
        public Map<Integer, EmissionSummary> getScenario2Emissions() { return scenario2Emissions; }
        public void setScenario2Emissions(Map<Integer, EmissionSummary> scenario2Emissions) { this.scenario2Emissions = scenario2Emissions; }

        public static class EmissionComparisonBuilder {
            private EmissionComparison comparison = new EmissionComparison();

            public EmissionComparisonBuilder scenario1Id(Long scenario1Id) {
                comparison.scenario1Id = scenario1Id;
                return this;
            }

            public EmissionComparisonBuilder scenario2Id(Long scenario2Id) {
                comparison.scenario2Id = scenario2Id;
                return this;
            }

            public EmissionComparisonBuilder scenario1Emissions(Map<Integer, EmissionSummary> scenario1Emissions) {
                comparison.scenario1Emissions = scenario1Emissions;
                return this;
            }

            public EmissionComparisonBuilder scenario2Emissions(Map<Integer, EmissionSummary> scenario2Emissions) {
                comparison.scenario2Emissions = scenario2Emissions;
                return this;
            }

            public EmissionComparison build() {
                return comparison;
            }
        }
    }
} 