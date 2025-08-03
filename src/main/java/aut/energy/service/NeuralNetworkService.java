package aut.energy.service;

import aut.energy.entity.EnergyData;
import aut.energy.entity.ForecastResult;
import aut.energy.entity.Scenario;
import aut.energy.repository.EnergyDataRepository;
import aut.energy.repository.ForecastResultRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Neural Network Service for energy demand forecasting
 * Uses Artificial Neural Networks with Particle Swarm Optimization for training
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NeuralNetworkService {

    private final EnergyDataRepository energyDataRepository;
    private final ForecastResultRepository forecastResultRepository;
    private final ParticleSwarmOptimizationService psoService;

    // Neural Network Configuration
    private static final int INPUT_LAYER_SIZE = 4;  // year, sector, energy_source, historical_consumption
    private static final int HIDDEN_LAYER_SIZE = 8;
    private static final int OUTPUT_LAYER_SIZE = 1;  // forecasted_consumption
    private static final double LEARNING_RATE = 0.01;
    private static final int MAX_EPOCHS = 500;

    /**
     * Forecast energy demand using ANN with PSO optimization
     */
    public List<ForecastResult> forecastEnergyDemand(
            Scenario scenario,
            List<String> sectors,
            List<String> energySources,
            Integer forecastYears) {

        log.info("Starting energy demand forecast for scenario: {}, sectors: {}, energy sources: {}", 
                scenario.getName(), sectors, energySources);

        List<ForecastResult> results = new ArrayList<>();

        // Get historical data for training
        List<EnergyData> historicalData = energyDataRepository.findBySectorsAndEnergySources(
                sectors, energySources);

        if (historicalData.isEmpty()) {
            log.warn("No historical data found for the specified sectors and energy sources");
            return results;
        }

        // Prepare training data
        TrainingData trainingData = prepareTrainingData(historicalData);

        // Train neural network using PSO
        NeuralNetwork network = trainNeuralNetwork(trainingData);

        // Generate forecasts for each sector and energy source combination
        for (String sector : sectors) {
            for (String energySource : energySources) {
                List<ForecastResult> sectorResults = generateForecasts(
                        network, scenario, sector, energySource, forecastYears);
                results.addAll(sectorResults);
            }
        }

        // Save forecast results
        forecastResultRepository.saveAll(results);

        log.info("Completed energy demand forecast. Generated {} forecast results", results.size());
        return results;
    }

    /**
     * Prepare training data from historical energy data
     */
    private TrainingData prepareTrainingData(List<EnergyData> historicalData) {
        // Group data by sector and energy source
        Map<String, List<EnergyData>> groupedData = historicalData.stream()
                .collect(Collectors.groupingBy(data -> 
                        data.getSector() + "_" + data.getEnergySource()));

        List<double[]> inputs = new ArrayList<>();
        List<double[]> outputs = new ArrayList<>();

        for (List<EnergyData> groupData : groupedData.values()) {
            // Sort by year
            groupData.sort(Comparator.comparing(EnergyData::getYear));

            for (int i = 1; i < groupData.size(); i++) {
                EnergyData current = groupData.get(i);
                EnergyData previous = groupData.get(i - 1);

                // Input features: year, sector_encoded, energy_source_encoded, previous_consumption
                double[] input = {
                        normalizeYear(current.getYear()),
                        encodeSector(current.getSector()),
                        encodeEnergySource(current.getEnergySource()),
                        normalizeConsumption(previous.getConsumptionTwh().doubleValue())
                };

                // Output: current consumption
                double[] output = {normalizeConsumption(current.getConsumptionTwh().doubleValue())};

                inputs.add(input);
                outputs.add(output);
            }
        }

        return new TrainingData(inputs, outputs);
    }

    /**
     * Train neural network using PSO optimization
     */
    private NeuralNetwork trainNeuralNetwork(TrainingData trainingData) {
        log.info("Training neural network with {} training samples", trainingData.inputs.size());

        // Initialize neural network
        NeuralNetwork network = new NeuralNetwork(INPUT_LAYER_SIZE, HIDDEN_LAYER_SIZE, OUTPUT_LAYER_SIZE);

        // Create PSO-compatible training data
        ParticleSwarmOptimizationService.TrainingData psoTrainingData = new ParticleSwarmOptimizationService.TrainingData() {
            @Override
            public java.util.List<double[]> getInputs() {
                return trainingData.inputs;
            }

            @Override
            public java.util.List<double[]> getOutputs() {
                return trainingData.outputs;
            }
        };

        // Use PSO to optimize network weights
        double[] optimizedWeights = psoService.optimizeNeuralNetworkWeights(network, psoTrainingData);

        // Apply optimized weights to network
        network.setWeights(optimizedWeights);

        log.info("Neural network training completed");
        return network;
    }

    /**
     * Generate forecasts for a specific sector and energy source
     */
    private List<ForecastResult> generateForecasts(
            NeuralNetwork network,
            Scenario scenario,
            String sector,
            String energySource,
            Integer forecastYears) {

        List<ForecastResult> forecasts = new ArrayList<>();

        // Get the latest historical data point for this sector/energy source
        Optional<EnergyData> latestData = energyDataRepository
                .findTopBySectorAndEnergySourceOrderByYearDesc(sector, energySource);

        if (latestData.isEmpty()) {
            log.warn("No historical data found for sector: {} and energy source: {}", sector, energySource);
            return forecasts;
        }

        EnergyData latest = latestData.get();
        double currentConsumption = latest.getConsumptionTwh().doubleValue();
        int currentYear = latest.getYear();

        // Generate forecasts for each future year
        for (int i = 1; i <= forecastYears; i++) {
            int forecastYear = currentYear + i;

            // Prepare input for forecasting
            double[] input = {
                    normalizeYear(forecastYear),
                    encodeSector(sector),
                    encodeEnergySource(energySource),
                    normalizeConsumption(currentConsumption)
            };

            // Get forecast from neural network
            double[] output = network.forward(input);
            double forecastedConsumption = denormalizeConsumption(output[0]);

            // Apply scenario adjustments
            forecastedConsumption = applyScenarioAdjustments(
                    forecastedConsumption, scenario, sector, energySource, forecastYear);

            // Create forecast result
            ForecastResult forecast = ForecastResult.builder()
                    .scenario(scenario)
                    .sector(sector)
                    .energySource(energySource)
                    .forecastYear(forecastYear)
                    .forecastedConsumptionTwh(BigDecimal.valueOf(forecastedConsumption))
                    .confidenceLevel(BigDecimal.valueOf(0.95))
                    .createdAt(LocalDateTime.now())
                    .build();

            forecasts.add(forecast);

            // Update current consumption for next iteration
            currentConsumption = forecastedConsumption;
        }

        return forecasts;
    }

    /**
     * Apply scenario-specific adjustments to forecasts
     */
    private double applyScenarioAdjustments(
            double baseForecast,
            Scenario scenario,
            String sector,
            String energySource,
            int year) {

        double adjustedForecast = baseForecast;

        // Apply sector-specific growth rates from scenario
        if (scenario.getSectorGrowthRates() != null && 
            scenario.getSectorGrowthRates().containsKey(sector)) {
            double growthRate = scenario.getSectorGrowthRates().get(sector);
            adjustedForecast *= (1 + growthRate);
        }

        // Apply energy source-specific adjustments
        if (scenario.getEnergySourceAdjustments() != null && 
            scenario.getEnergySourceAdjustments().containsKey(energySource)) {
            double adjustment = scenario.getEnergySourceAdjustments().get(energySource);
            adjustedForecast *= (1 + adjustment);
        }

        // Apply year-specific factors
        if (scenario.getYearlyFactors() != null && 
            scenario.getYearlyFactors().containsKey(year)) {
            double factor = scenario.getYearlyFactors().get(year);
            adjustedForecast *= factor;
        }

        return adjustedForecast;
    }

    // ==================== UTILITY METHODS ====================

    private double normalizeYear(int year) {
        return (year - 2000) / 100.0; // Normalize to 0-1 range
    }

    private double encodeSector(String sector) {
        // Simple encoding - in production, use proper encoding
        Map<String, Double> sectorEncoding = Map.of(
                "Industrial", 0.1,
                "Residential", 0.2,
                "Commercial", 0.3,
                "Transportation", 0.4,
                "Agriculture", 0.5
        );
        return sectorEncoding.getOrDefault(sector, 0.0);
    }

    private double encodeEnergySource(String energySource) {
        // Simple encoding - in production, use proper encoding
        Map<String, Double> sourceEncoding = Map.of(
                "Electricity", 0.1,
                "Natural Gas", 0.2,
                "Oil", 0.3,
                "Coal", 0.4,
                "Renewables", 0.5
        );
        return sourceEncoding.getOrDefault(energySource, 0.0);
    }

    private double normalizeConsumption(double consumption) {
        // Simple normalization - in production, use proper scaling
        return Math.min(consumption / 1000.0, 1.0);
    }

    private double denormalizeConsumption(double normalized) {
        return normalized * 1000.0;
    }

    // ==================== INNER CLASSES ====================

    /**
     * Training data container
     */
    private static class TrainingData {
        final List<double[]> inputs;
        final List<double[]> outputs;

        TrainingData(List<double[]> inputs, List<double[]> outputs) {
            this.inputs = inputs;
            this.outputs = outputs;
        }
    }

    /**
     * Simple Neural Network implementation
     */
    private static class NeuralNetwork implements ParticleSwarmOptimizationService.NeuralNetwork {
        private final int inputSize;
        private final int hiddenSize;
        private final int outputSize;
        private RealMatrix weights1; // Input to hidden
        private RealMatrix weights2; // Hidden to output
        private RealVector bias1;
        private RealVector bias2;

        public NeuralNetwork(int inputSize, int hiddenSize, int outputSize) {
            this.inputSize = inputSize;
            this.hiddenSize = hiddenSize;
            this.outputSize = outputSize;
            initializeWeights();
        }

        private void initializeWeights() {
            // Initialize with random weights
            Random random = new Random();
            
            weights1 = new Array2DRowRealMatrix(hiddenSize, inputSize);
            weights2 = new Array2DRowRealMatrix(outputSize, hiddenSize);
            
            for (int i = 0; i < hiddenSize; i++) {
                for (int j = 0; j < inputSize; j++) {
                    weights1.setEntry(i, j, random.nextGaussian() * 0.1);
                }
            }
            
            for (int i = 0; i < outputSize; i++) {
                for (int j = 0; j < hiddenSize; j++) {
                    weights2.setEntry(i, j, random.nextGaussian() * 0.1);
                }
            }
            
            bias1 = new ArrayRealVector(hiddenSize);
            bias2 = new ArrayRealVector(outputSize);
            
            for (int i = 0; i < hiddenSize; i++) {
                bias1.setEntry(i, random.nextGaussian() * 0.1);
            }
            
            for (int i = 0; i < outputSize; i++) {
                bias2.setEntry(i, random.nextGaussian() * 0.1);
            }
        }

        public double[] forward(double[] input) {
            RealVector inputVector = new ArrayRealVector(input);
            
            // Hidden layer
            RealVector hidden = weights1.operate(inputVector).add(bias1);
            for (int i = 0; i < hidden.getDimension(); i++) {
                hidden.setEntry(i, sigmoid(hidden.getEntry(i)));
            }
            
            // Output layer
            RealVector output = weights2.operate(hidden).add(bias2);
            for (int i = 0; i < output.getDimension(); i++) {
                output.setEntry(i, sigmoid(output.getEntry(i)));
            }
            
            return output.toArray();
        }

        private double sigmoid(double x) {
            return 1.0 / (1.0 + Math.exp(-x));
        }

        public void setWeights(double[] weights) {
            int pos = 0;
            
            // Set weights1
            for (int i = 0; i < hiddenSize; i++) {
                for (int j = 0; j < inputSize; j++) {
                    weights1.setEntry(i, j, weights[pos++]);
                }
            }
            
            // Set weights2
            for (int i = 0; i < outputSize; i++) {
                for (int j = 0; j < hiddenSize; j++) {
                    weights2.setEntry(i, j, weights[pos++]);
                }
            }
            
            // Set biases
            for (int i = 0; i < hiddenSize; i++) {
                bias1.setEntry(i, weights[pos++]);
            }
            
            for (int i = 0; i < outputSize; i++) {
                bias2.setEntry(i, weights[pos++]);
            }
        }

        public double[] getWeights() {
            int totalWeights = hiddenSize * inputSize + outputSize * hiddenSize + hiddenSize + outputSize;
            double[] weights = new double[totalWeights];
            int pos = 0;
            
            // Get weights1
            for (int i = 0; i < hiddenSize; i++) {
                for (int j = 0; j < inputSize; j++) {
                    weights[pos++] = weights1.getEntry(i, j);
                }
            }
            
            // Get weights2
            for (int i = 0; i < outputSize; i++) {
                for (int j = 0; j < hiddenSize; j++) {
                    weights[pos++] = weights2.getEntry(i, j);
                }
            }
            
            // Get biases
            for (int i = 0; i < hiddenSize; i++) {
                weights[pos++] = bias1.getEntry(i);
            }
            
            for (int i = 0; i < outputSize; i++) {
                weights[pos++] = bias2.getEntry(i);
            }
            
            return weights;
        }
    }
} 