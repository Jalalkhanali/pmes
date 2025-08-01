package ir.aut.jalal.pmes.energy.service;

import ir.aut.jalal.pmes.energy.entity.EnergyData;
import ir.aut.jalal.pmes.energy.entity.ForecastResult;
import ir.aut.jalal.pmes.energy.entity.Scenario;
import ir.aut.jalal.pmes.energy.repository.EnergyDataRepository;
import ir.aut.jalal.pmes.energy.repository.ForecastResultRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;

/**
 * Service for Artificial Neural Network forecasting with Particle Swarm Optimization
 * Uses both Neuroph and DL4J for different types of neural networks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NeuralNetworkService {

    private final EnergyDataRepository energyDataRepository;
    private final ForecastResultRepository forecastResultRepository;
    private final ParticleSwarmOptimizationService psoService;

    /**
     * Forecast energy demand using ANN with PSO optimization
     * 
     * @param scenario The scenario to forecast for
     * @param sectors List of sectors to forecast
     * @param energySources List of energy sources to forecast
     * @param forecastYears Number of years to forecast
     * @return List of forecast results
     */
    public List<ForecastResult> forecastEnergyDemand(Scenario scenario, 
                                                   List<String> sectors, 
                                                   List<String> energySources, 
                                                   int forecastYears) {
        log.info("Starting energy demand forecast for scenario: {}", scenario.getName());
        
        List<ForecastResult> results = new ArrayList<>();
        
        // Get historical data for training
        List<EnergyData> historicalData = getHistoricalData(sectors, energySources);
        if (historicalData.isEmpty()) {
            throw new IllegalStateException("No historical data available for forecasting");
        }
        
        // Prepare training data
        TrainingData trainingData = prepareTrainingData(historicalData);
        
        // Optimize neural network architecture using PSO
        PSOOptimizationResult psoResult = psoService.optimizeNeuralNetwork(trainingData);
        
        // Train neural network with optimized parameters
        NeuralNetworkModel trainedModel = trainNeuralNetwork(trainingData, psoResult);
        
        // Generate forecasts for each sector and energy source combination
        for (String sector : sectors) {
            for (String energySource : energySources) {
                List<ForecastResult> sectorResults = generateForecasts(scenario, sector, energySource, 
                                                                      forecastYears, trainedModel, psoResult);
                results.addAll(sectorResults);
            }
        }
        
        // Save forecast results
        forecastResultRepository.saveAll(results);
        
        log.info("Completed energy demand forecast. Generated {} forecast results", results.size());
        return results;
    }

    /**
     * Get historical energy data for training
     */
    private List<EnergyData> getHistoricalData(List<String> sectors, List<String> energySources) {
        List<EnergyData> allData = new ArrayList<>();
        
        for (String sector : sectors) {
            for (String energySource : energySources) {
                List<EnergyData> sectorData = energyDataRepository.findBySectorAndEnergySourceOrderByYearAsc(sector, energySource);
                allData.addAll(sectorData);
            }
        }
        
        return allData.stream()
                .sorted(Comparator.comparing(EnergyData::getYear))
                .collect(Collectors.toList());
    }

    /**
     * Prepare training data from historical energy data
     */
    private TrainingData prepareTrainingData(List<EnergyData> historicalData) {
        // Group data by sector and energy source
        Map<String, List<EnergyData>> groupedData = historicalData.stream()
                .collect(Collectors.groupingBy(data -> data.getSector() + "_" + data.getEnergySource()));
        
        List<double[]> inputs = new ArrayList<>();
        List<double[]> outputs = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (Map.Entry<String, List<EnergyData>> entry : groupedData.entrySet()) {
            List<EnergyData> series = entry.getValue();
            if (series.size() < 5) continue; // Need at least 5 years of data
            
            // Create sliding window features
            for (int i = 4; i < series.size(); i++) {
                double[] input = createInputFeatures(series, i);
                double[] output = new double[]{series.get(i).getConsumptionTwh().doubleValue()};
                
                inputs.add(input);
                outputs.add(output);
                labels.add(entry.getKey());
            }
        }
        
        return new TrainingData(inputs, outputs, labels);
    }

    /**
     * Create input features for neural network
     */
    private double[] createInputFeatures(List<EnergyData> series, int currentIndex) {
        // Use last 4 years of data as features
        double[] features = new double[16]; // 4 years * 4 features per year
        int featureIndex = 0;
        
        for (int i = currentIndex - 4; i < currentIndex; i++) {
            EnergyData data = series.get(i);
            features[featureIndex++] = data.getConsumptionTwh().doubleValue();
            features[featureIndex++] = data.getGdpBillions() != null ? data.getGdpBillions().doubleValue() : 0.0;
            features[featureIndex++] = data.getPopulationMillions() != null ? data.getPopulationMillions().doubleValue() : 0.0;
            features[featureIndex++] = data.getAvgTemperatureCelsius() != null ? data.getAvgTemperatureCelsius().doubleValue() : 0.0;
        }
        
        return features;
    }

    /**
     * Train neural network with optimized parameters
     */
    private NeuralNetworkModel trainNeuralNetwork(TrainingData trainingData, PSOOptimizationResult psoResult) {
        // Normalize training data
        DataNormalizer normalizer = new DataNormalizer();
        double[][] normalizedInputs = normalizer.normalizeInputs(trainingData.inputs);
        double[][] normalizedOutputs = normalizer.normalizeOutputs(trainingData.outputs);
        
        // Create and train neural network using DL4J
        MultiLayerNetwork network = createDL4JNetwork(psoResult);
        network.init();
        
        // Convert to INDArray for DL4J
        INDArray inputArray = Nd4j.create(normalizedInputs);
        INDArray outputArray = Nd4j.create(normalizedOutputs);
        
        // Train the network
        for (int epoch = 0; epoch < psoResult.epochs; epoch++) {
            network.fit(inputArray, outputArray);
            
            if (epoch % 100 == 0) {
                double loss = network.score();
                log.debug("Epoch {}: Loss = {}", epoch, loss);
            }
        }
        
        return new NeuralNetworkModel(network, normalizer, psoResult);
    }

    /**
     * Create DL4J neural network with optimized architecture
     */
    private MultiLayerNetwork createDL4JNetwork(PSOOptimizationResult psoResult) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Adam(psoResult.learningRate))
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(psoResult.inputSize)
                        .nOut(psoResult.hiddenLayer1Size)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(psoResult.hiddenLayer1Size)
                        .nOut(psoResult.hiddenLayer2Size)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(psoResult.hiddenLayer2Size)
                        .nOut(1)
                        .activation(Activation.IDENTITY)
                        .build())
                .build();
        
        return new MultiLayerNetwork(conf);
    }

    /**
     * Generate forecasts for a specific sector and energy source
     */
    private List<ForecastResult> generateForecasts(Scenario scenario, String sector, String energySource, 
                                                 int forecastYears, NeuralNetworkModel model, PSOOptimizationResult psoResult) {
        List<ForecastResult> forecasts = new ArrayList<>();
        
        // Get the latest historical data for this sector/energy source
        List<EnergyData> historicalSeries = energyDataRepository.findBySectorAndEnergySourceOrderByYearAsc(sector, energySource);
        if (historicalSeries.isEmpty()) {
            log.warn("No historical data for sector: {} and energy source: {}", sector, energySource);
            return forecasts;
        }
        
        // Get the latest year
        int latestYear = historicalSeries.get(historicalSeries.size() - 1).getYear();
        
        // Generate forecasts for each year
        for (int year = latestYear + 1; year <= latestYear + forecastYears; year++) {
            // Prepare input features for forecasting
            double[] inputFeatures = prepareForecastInput(historicalSeries, year, scenario);
            
            // Normalize input
            double[] normalizedInput = model.normalizer.normalizeInput(inputFeatures);
            
            // Make prediction
            INDArray inputArray = Nd4j.create(normalizedInput).reshape(1, -1);
            INDArray outputArray = model.network.output(inputArray);
            
            // Denormalize output
            double predictedConsumption = model.normalizer.denormalizeOutput(outputArray.getDouble(0));
            
            // Calculate confidence interval
            double confidence = calculateConfidence(model, normalizedInput);
            double lowerBound = predictedConsumption * (1 - confidence);
            double upperBound = predictedConsumption * (1 + confidence);
            
            // Create forecast result
            ForecastResult forecast = ForecastResult.builder()
                    .scenario(scenario)
                    .forecastYear(year)
                    .sector(sector)
                    .energySource(energySource)
                    .forecastedConsumptionTwh(BigDecimal.valueOf(predictedConsumption).setScale(3, RoundingMode.HALF_UP))
                    .lowerBoundTwh(BigDecimal.valueOf(lowerBound).setScale(3, RoundingMode.HALF_UP))
                    .upperBoundTwh(BigDecimal.valueOf(upperBound).setScale(3, RoundingMode.HALF_UP))
                    .confidenceLevel(BigDecimal.valueOf(confidence).setScale(3, RoundingMode.HALF_UP))
                    .modelAccuracy(BigDecimal.valueOf(psoResult.fitness).setScale(4, RoundingMode.HALF_UP))
                    .nnArchitecture(String.format("DL4J-%d-%d-%d", psoResult.hiddenLayer1Size, psoResult.hiddenLayer2Size, 1))
                    .psoParameters(String.format("particles=%d,iterations=%d,learningRate=%.4f", 
                                               psoResult.particleCount, psoResult.iterations, psoResult.learningRate))
                    .isBaseline(scenario.getScenarioType() == Scenario.ScenarioType.BASELINE)
                    .metadata(String.format("Trained on %d samples", historicalSeries.size()))
                    .build();
            
            forecasts.add(forecast);
        }
        
        return forecasts;
    }

    /**
     * Prepare input features for forecasting
     */
    private double[] prepareForecastInput(List<EnergyData> historicalSeries, int targetYear, Scenario scenario) {
        // Use the last 4 years of data
        int startIndex = Math.max(0, historicalSeries.size() - 4);
        double[] features = new double[16];
        int featureIndex = 0;
        
        for (int i = startIndex; i < historicalSeries.size(); i++) {
            EnergyData data = historicalSeries.get(i);
            features[featureIndex++] = data.getConsumptionTwh().doubleValue();
            features[featureIndex++] = data.getGdpBillions() != null ? data.getGdpBillions().doubleValue() : 0.0;
            features[featureIndex++] = data.getPopulationMillions() != null ? data.getPopulationMillions().doubleValue() : 0.0;
            features[featureIndex++] = data.getAvgTemperatureCelsius() != null ? data.getAvgTemperatureCelsius().doubleValue() : 0.0;
        }
        
        // Apply scenario modifications if not baseline
        if (scenario.getScenarioType() != Scenario.ScenarioType.BASELINE) {
            applyScenarioModifications(features, scenario, targetYear);
        }
        
        return features;
    }

    /**
     * Apply scenario modifications to input features
     */
    private void applyScenarioModifications(double[] features, Scenario scenario, int targetYear) {
        // Apply GDP growth rate
        if (scenario.getGdpGrowthRate() != null) {
            double growthRate = scenario.getGdpGrowthRate().doubleValue() / 100.0;
            for (int i = 1; i < features.length; i += 4) { // GDP features
                features[i] *= Math.pow(1 + growthRate, targetYear - 2020);
            }
        }
        
        // Apply population growth rate
        if (scenario.getPopulationGrowthRate() != null) {
            double growthRate = scenario.getPopulationGrowthRate().doubleValue() / 100.0;
            for (int i = 2; i < features.length; i += 4) { // Population features
                features[i] *= Math.pow(1 + growthRate, targetYear - 2020);
            }
        }
        
        // Apply efficiency improvements
        if (scenario.getEfficiencyImprovementRate() != null) {
            double efficiencyRate = scenario.getEfficiencyImprovementRate().doubleValue() / 100.0;
            for (int i = 0; i < features.length; i += 4) { // Consumption features
                features[i] *= Math.pow(1 - efficiencyRate, targetYear - 2020);
            }
        }
    }

    /**
     * Calculate confidence level for the forecast
     */
    private double calculateConfidence(NeuralNetworkModel model, double[] input) {
        // Simple confidence calculation based on model accuracy
        // In a real implementation, you might use ensemble methods or uncertainty quantification
        return Math.max(0.1, Math.min(0.9, model.psoResult.fitness));
    }

    /**
     * Training data container
     */
    private static class TrainingData {
        final List<double[]> inputs;
        final List<double[]> outputs;
        final List<String> labels;
        
        TrainingData(List<double[]> inputs, List<double[]> outputs, List<String> labels) {
            this.inputs = inputs;
            this.outputs = outputs;
            this.labels = labels;
        }
    }

    /**
     * Neural network model container
     */
    private static class NeuralNetworkModel {
        final MultiLayerNetwork network;
        final DataNormalizer normalizer;
        final PSOOptimizationResult psoResult;
        
        NeuralNetworkModel(MultiLayerNetwork network, DataNormalizer normalizer, PSOOptimizationResult psoResult) {
            this.network = network;
            this.normalizer = normalizer;
            this.psoResult = psoResult;
        }
    }

    /**
     * Data normalizer for neural network inputs and outputs
     */
    private static class DataNormalizer {
        private double[] inputMeans;
        private double[] inputStds;
        private double outputMean;
        private double outputStd;
        
        public double[][] normalizeInputs(List<double[]> inputs) {
            if (inputs.isEmpty()) return new double[0][0];
            
            int featureCount = inputs.get(0).length;
            inputMeans = new double[featureCount];
            inputStds = new double[featureCount];
            
            // Calculate means
            for (double[] input : inputs) {
                for (int i = 0; i < featureCount; i++) {
                    inputMeans[i] += input[i];
                }
            }
            for (int i = 0; i < featureCount; i++) {
                inputMeans[i] /= inputs.size();
            }
            
            // Calculate standard deviations
            for (double[] input : inputs) {
                for (int i = 0; i < featureCount; i++) {
                    inputStds[i] += Math.pow(input[i] - inputMeans[i], 2);
                }
            }
            for (int i = 0; i < featureCount; i++) {
                inputStds[i] = Math.sqrt(inputStds[i] / inputs.size());
                if (inputStds[i] == 0) inputStds[i] = 1.0; // Avoid division by zero
            }
            
            // Normalize
            double[][] normalized = new double[inputs.size()][featureCount];
            for (int i = 0; i < inputs.size(); i++) {
                for (int j = 0; j < featureCount; j++) {
                    normalized[i][j] = (inputs.get(i)[j] - inputMeans[j]) / inputStds[j];
                }
            }
            
            return normalized;
        }
        
        public double[][] normalizeOutputs(List<double[]> outputs) {
            if (outputs.isEmpty()) return new double[0][0];
            
            // Calculate output statistics
            double sum = 0.0;
            for (double[] output : outputs) {
                sum += output[0];
            }
            outputMean = sum / outputs.size();
            
            double sumSquares = 0.0;
            for (double[] output : outputs) {
                sumSquares += Math.pow(output[0] - outputMean, 2);
            }
            outputStd = Math.sqrt(sumSquares / outputs.size());
            if (outputStd == 0) outputStd = 1.0;
            
            // Normalize
            double[][] normalized = new double[outputs.size()][1];
            for (int i = 0; i < outputs.size(); i++) {
                normalized[i][0] = (outputs.get(i)[0] - outputMean) / outputStd;
            }
            
            return normalized;
        }
        
        public double[] normalizeInput(double[] input) {
            double[] normalized = new double[input.length];
            for (int i = 0; i < input.length; i++) {
                normalized[i] = (input[i] - inputMeans[i]) / inputStds[i];
            }
            return normalized;
        }
        
        public double denormalizeOutput(double normalizedOutput) {
            return normalizedOutput * outputStd + outputMean;
        }
    }
} 