package aut.energy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for energy planning system properties
 */
@Configuration
@ConfigurationProperties(prefix = "energy")
@Data
public class EnergyPlanningConfig {

    private NeuralNetwork neuralNetwork = new NeuralNetwork();
    private ExcelImport excelImport = new ExcelImport();
    private Emissions emissions = new Emissions();

    @Data
    public static class NeuralNetwork {
        private Pso pso = new Pso();
        private Training training = new Training();
        private Forecasting forecasting = new Forecasting();

        @Data
        public static class Pso {
            private int particleCount = 30;
            private int iterations = 100;
            private double inertiaWeight = 0.7;
            private double cognitiveWeight = 1.5;
            private double socialWeight = 1.5;
        }

        @Data
        public static class Training {
            private int epochs = 500;
            private double learningRate = 0.01;
            private int batchSize = 32;
        }

        @Data
        public static class Forecasting {
            private double confidenceLevel = 0.95;
            private int maxForecastYears = 30;
        }
    }

    @Data
    public static class ExcelImport {
        private String maxFileSize = "10MB";
        private List<String> supportedFormats = List.of("xlsx", "xls");
        private List<String> requiredColumns = List.of("year", "sector", "energy_source", "consumption_twh");
    }

    @Data
    public static class Emissions {
        private int defaultYear = 2023;
        private String calculationMethod = "IPCC";
        private boolean includeIndirectEmissions = true;
    }
} 