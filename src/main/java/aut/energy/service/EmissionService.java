package aut.energy.service;

import aut.energy.entity.EmissionFactor;
import aut.energy.entity.ForecastResult;
import aut.energy.entity.Scenario;
import aut.energy.repository.EmissionFactorRepository;
import aut.energy.repository.ForecastResultRepository;
import aut.energy.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Emission Service for calculating emissions from energy consumption
 * Uses emission factors to convert energy consumption to CO2 emissions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmissionService {

    private final EmissionFactorRepository emissionFactorRepository;
    private final ForecastResultRepository forecastResultRepository;
    private final ScenarioRepository scenarioRepository;

    /**
     * Calculate emissions for a specific scenario
     */
    public List<EmissionCalculation> calculateEmissionsForScenario(Long scenarioId) {
        log.info("Calculating emissions for scenario ID: {}", scenarioId);

        Optional<Scenario> scenarioOpt = scenarioRepository.findById(scenarioId);
        if (scenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found: " + scenarioId);
        }

        Scenario scenario = scenarioOpt.get();
        List<ForecastResult> forecasts = forecastResultRepository.findByScenarioOrderByForecastYearAscSectorAscEnergySourceAsc(scenario);

        return calculateEmissionsFromForecasts(forecasts);
    }

    /**
     * Calculate emissions for a scenario within a year range
     */
    public List<EmissionCalculation> calculateEmissionsForYearRange(Long scenarioId, Integer startYear, Integer endYear) {
        log.info("Calculating emissions for scenario ID: {} from {} to {}", scenarioId, startYear, endYear);

        Optional<Scenario> scenarioOpt = scenarioRepository.findById(scenarioId);
        if (scenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found: " + scenarioId);
        }

        Scenario scenario = scenarioOpt.get();
        List<ForecastResult> forecasts = forecastResultRepository.findByScenarioAndForecastYearBetweenOrderByForecastYearAscSectorAscEnergySourceAsc(scenario, startYear, endYear);

        return calculateEmissionsFromForecasts(forecasts);
    }

    /**
     * Calculate total emissions by year for a scenario
     */
    public Map<Integer, EmissionSummary> calculateTotalEmissionsByYear(Long scenarioId) {
        log.info("Calculating total emissions by year for scenario ID: {}", scenarioId);

        List<EmissionCalculation> calculations = calculateEmissionsForScenario(scenarioId);

        return calculations.stream()
                .collect(Collectors.groupingBy(
                        EmissionCalculation::getYear,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::createEmissionSummary
                        )
                ));
    }

    /**
     * Calculate emissions by sector for a scenario
     */
    public Map<String, EmissionSummary> calculateEmissionsBySector(Long scenarioId) {
        log.info("Calculating emissions by sector for scenario ID: {}", scenarioId);

        List<EmissionCalculation> calculations = calculateEmissionsForScenario(scenarioId);

        return calculations.stream()
                .collect(Collectors.groupingBy(
                        EmissionCalculation::getSector,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::createEmissionSummary
                        )
                ));
    }

    /**
     * Calculate emissions by energy source for a scenario
     */
    public Map<String, EmissionSummary> calculateEmissionsByEnergySource(Long scenarioId) {
        log.info("Calculating emissions by energy source for scenario ID: {}", scenarioId);

        List<EmissionCalculation> calculations = calculateEmissionsForScenario(scenarioId);

        return calculations.stream()
                .collect(Collectors.groupingBy(
                        EmissionCalculation::getEnergySource,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::createEmissionSummary
                        )
                ));
    }

    /**
     * Compare emissions between two scenarios
     */
    public EmissionComparison compareScenarios(Long scenario1Id, Long scenario2Id) {
        log.info("Comparing emissions between scenarios: {} and {}", scenario1Id, scenario2Id);

        List<EmissionCalculation> emissions1 = calculateEmissionsForScenario(scenario1Id);
        List<EmissionCalculation> emissions2 = calculateEmissionsForScenario(scenario2Id);

        EmissionSummary summary1 = createEmissionSummary(emissions1);
        EmissionSummary summary2 = createEmissionSummary(emissions2);

        double totalDifference = summary2.getTotalEmissions() - summary1.getTotalEmissions();
        double percentageDifference = summary1.getTotalEmissions() > 0 ? 
                (totalDifference / summary1.getTotalEmissions()) * 100 : 0;

        return EmissionComparison.builder()
                .scenario1Id(scenario1Id)
                .scenario2Id(scenario2Id)
                .scenario1Emissions(summary1)
                .scenario2Emissions(summary2)
                .totalDifference(totalDifference)
                .percentageDifference(percentageDifference)
                .build();
    }

    /**
     * Calculate emissions from forecast results
     */
    private List<EmissionCalculation> calculateEmissionsFromForecasts(List<ForecastResult> forecasts) {
        List<EmissionCalculation> calculations = new ArrayList<>();

        // Get emission factors
        List<EmissionFactor> emissionFactors = emissionFactorRepository.findAll();
        Map<String, EmissionFactor> factorMap = emissionFactors.stream()
                .collect(Collectors.toMap(
                        factor -> factor.getSector() + "_" + factor.getEnergySource(),
                        factor -> factor
                ));

        for (ForecastResult forecast : forecasts) {
            String key = forecast.getSector() + "_" + forecast.getEnergySource();
            EmissionFactor factor = factorMap.get(key);

            if (factor == null) {
                log.warn("No emission factor found for sector: {} and energy source: {}", 
                        forecast.getSector(), forecast.getEnergySource());
                continue;
            }

            // Calculate emissions
            double emissions = forecast.getForecastedConsumptionTwh().doubleValue()
                * factor.getCo2Factor().doubleValue();

            EmissionCalculation calculation = EmissionCalculation.builder()
                .year(forecast.getForecastYear())
                    .sector(forecast.getSector())
                    .energySource(forecast.getEnergySource())
                    .energyConsumptionTwh(forecast.getForecastedConsumptionTwh().doubleValue())
                    .emissionsKgCo2(emissions)
                    .emissionFactor(factor.getCo2Factor().doubleValue())
                    .build();

            calculations.add(calculation);
        }

        return calculations;
    }

    /**
     * Create emission summary from calculations
     */
    private EmissionSummary createEmissionSummary(List<EmissionCalculation> calculations) {
        double totalEmissions = calculations.stream()
                .mapToDouble(EmissionCalculation::getEmissionsKgCo2)
                .sum();

        double totalEnergyConsumption = calculations.stream()
                .mapToDouble(EmissionCalculation::getEnergyConsumptionTwh)
                .sum();

        double averageEmissionFactor = calculations.stream()
                .mapToDouble(EmissionCalculation::getEmissionFactor)
                .average()
                .orElse(0.0);

        return EmissionSummary.builder()
                .totalEmissions(totalEmissions)
                .totalEnergyConsumption(totalEnergyConsumption)
                .averageEmissionFactor(averageEmissionFactor)
                .calculationCount(calculations.size())
                .build();
    }

    // ==================== DATA CLASSES ====================

    /**
     * Emission calculation result
     */
    public static class EmissionCalculation {
        private final Integer year;
        private final String sector;
        private final String energySource;
        private final Double energyConsumptionTwh;
        private final Double emissionsKgCo2;
        private final Double emissionFactor;

        private EmissionCalculation(Builder builder) {
            this.year = builder.year;
            this.sector = builder.sector;
            this.energySource = builder.energySource;
            this.energyConsumptionTwh = builder.energyConsumptionTwh;
            this.emissionsKgCo2 = builder.emissionsKgCo2;
            this.emissionFactor = builder.emissionFactor;
        }

        // Getters
        public Integer getYear() { return year; }
        public String getSector() { return sector; }
        public String getEnergySource() { return energySource; }
        public Double getEnergyConsumptionTwh() { return energyConsumptionTwh; }
        public Double getEmissionsKgCo2() { return emissionsKgCo2; }
        public Double getEmissionFactor() { return emissionFactor; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Integer year;
            private String sector;
            private String energySource;
            private Double energyConsumptionTwh;
            private Double emissionsKgCo2;
            private Double emissionFactor;

            public Builder year(Integer year) {
                this.year = year;
                return this;
            }

            public Builder sector(String sector) {
                this.sector = sector;
                return this;
            }

            public Builder energySource(String energySource) {
                this.energySource = energySource;
                return this;
            }

            public Builder energyConsumptionTwh(Double energyConsumptionTwh) {
                this.energyConsumptionTwh = energyConsumptionTwh;
                return this;
            }

            public Builder emissionsKgCo2(Double emissionsKgCo2) {
                this.emissionsKgCo2 = emissionsKgCo2;
                return this;
            }

            public Builder emissionFactor(Double emissionFactor) {
                this.emissionFactor = emissionFactor;
                return this;
            }

            public EmissionCalculation build() {
                return new EmissionCalculation(this);
            }
        }
    }

    /**
     * Emission summary
     */
    public static class EmissionSummary {
        private final Double totalEmissions;
        private final Double totalEnergyConsumption;
        private final Double averageEmissionFactor;
        private final Integer calculationCount;

        private EmissionSummary(Builder builder) {
            this.totalEmissions = builder.totalEmissions;
            this.totalEnergyConsumption = builder.totalEnergyConsumption;
            this.averageEmissionFactor = builder.averageEmissionFactor;
            this.calculationCount = builder.calculationCount;
        }

        // Getters
        public Double getTotalEmissions() { return totalEmissions; }
        public Double getTotalEnergyConsumption() { return totalEnergyConsumption; }
        public Double getAverageEmissionFactor() { return averageEmissionFactor; }
        public Integer getCalculationCount() { return calculationCount; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Double totalEmissions;
            private Double totalEnergyConsumption;
            private Double averageEmissionFactor;
            private Integer calculationCount;

            public Builder totalEmissions(Double totalEmissions) {
                this.totalEmissions = totalEmissions;
                return this;
            }

            public Builder totalEnergyConsumption(Double totalEnergyConsumption) {
                this.totalEnergyConsumption = totalEnergyConsumption;
                return this;
            }

            public Builder averageEmissionFactor(Double averageEmissionFactor) {
                this.averageEmissionFactor = averageEmissionFactor;
                return this;
            }

            public Builder calculationCount(Integer calculationCount) {
                this.calculationCount = calculationCount;
                return this;
            }

            public EmissionSummary build() {
                return new EmissionSummary(this);
            }
        }
    }

    /**
     * Emission comparison between scenarios
     */
    public static class EmissionComparison {
        private final Long scenario1Id;
        private final Long scenario2Id;
        private final EmissionSummary scenario1Emissions;
        private final EmissionSummary scenario2Emissions;
        private final Double totalDifference;
        private final Double percentageDifference;

        private EmissionComparison(Builder builder) {
            this.scenario1Id = builder.scenario1Id;
            this.scenario2Id = builder.scenario2Id;
            this.scenario1Emissions = builder.scenario1Emissions;
            this.scenario2Emissions = builder.scenario2Emissions;
            this.totalDifference = builder.totalDifference;
            this.percentageDifference = builder.percentageDifference;
        }

        // Getters
        public Long getScenario1Id() { return scenario1Id; }
        public Long getScenario2Id() { return scenario2Id; }
        public EmissionSummary getScenario1Emissions() { return scenario1Emissions; }
        public EmissionSummary getScenario2Emissions() { return scenario2Emissions; }
        public Double getTotalDifference() { return totalDifference; }
        public Double getPercentageDifference() { return percentageDifference; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long scenario1Id;
            private Long scenario2Id;
            private EmissionSummary scenario1Emissions;
            private EmissionSummary scenario2Emissions;
            private Double totalDifference;
            private Double percentageDifference;

            public Builder scenario1Id(Long scenario1Id) {
                this.scenario1Id = scenario1Id;
                return this;
            }

            public Builder scenario2Id(Long scenario2Id) {
                this.scenario2Id = scenario2Id;
                return this;
            }

            public Builder scenario1Emissions(EmissionSummary scenario1Emissions) {
                this.scenario1Emissions = scenario1Emissions;
                return this;
            }

            public Builder scenario2Emissions(EmissionSummary scenario2Emissions) {
                this.scenario2Emissions = scenario2Emissions;
                return this;
            }

            public Builder totalDifference(Double totalDifference) {
                this.totalDifference = totalDifference;
                return this;
            }

            public Builder percentageDifference(Double percentageDifference) {
                this.percentageDifference = percentageDifference;
                return this;
            }

            public EmissionComparison build() {
                return new EmissionComparison(this);
            }
        }
    }
} 