package ir.aut.jalal.pmes.energy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.stereotype.Service;

/**
 * Particle Swarm Optimization service for optimizing neural network architecture
 * Optimizes the number of neurons in hidden layers and learning rate
 */
@Service
@Slf4j
public class ParticleSwarmOptimizationService {

    private static final int DEFAULT_PARTICLE_COUNT = 30;
    private static final int DEFAULT_ITERATIONS = 100;
    private static final double INERTIA_WEIGHT = 0.7;
    private static final double COGNITIVE_WEIGHT = 1.5;
    private static final double SOCIAL_WEIGHT = 1.5;

    /**
     * Optimize neural network architecture using PSO
     * 
     * @param trainingData The training data for fitness evaluation
     * @return PSOOptimizationResult with optimized parameters
     */
    public PSOOptimizationResult optimizeNeuralNetwork(NeuralNetworkService.TrainingData trainingData) {
        log.info("Starting PSO optimization for neural network architecture");
        
        // Initialize particles
        List<Particle> particles = initializeParticles(DEFAULT_PARTICLE_COUNT);
        
        // Global best particle
        Particle globalBest = null;
        double globalBestFitness = Double.MAX_VALUE;
        
        // PSO main loop
        for (int iteration = 0; iteration < DEFAULT_ITERATIONS; iteration++) {
            // Evaluate fitness for all particles
            for (Particle particle : particles) {
                double fitness = evaluateFitness(particle, trainingData);
                particle.setFitness(fitness);
                
                // Update personal best
                if (fitness < particle.getPersonalBestFitness()) {
                    particle.setPersonalBestPosition(particle.getPosition().copy());
                    particle.setPersonalBestFitness(fitness);
                }
                
                // Update global best
                if (fitness < globalBestFitness) {
                    globalBestFitness = fitness;
                    globalBest = particle.copy();
                }
            }
            
            // Update particle velocities and positions
            for (Particle particle : particles) {
                updateParticle(particle, globalBest);
            }
            
            if (iteration % 10 == 0) {
                log.debug("PSO Iteration {}: Best fitness = {}", iteration, globalBestFitness);
            }
        }
        
        log.info("PSO optimization completed. Best fitness: {}", globalBestFitness);
        
        return createOptimizationResult(globalBest, trainingData);
    }

    /**
     * Initialize particles with random positions
     */
    private List<Particle> initializeParticles(int particleCount) {
        List<Particle> particles = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < particleCount; i++) {
            Particle particle = new Particle();
            
            // Initialize position with random values
            double[] position = new double[4]; // [hiddenLayer1Size, hiddenLayer2Size, learningRate, epochs]
            position[0] = 10 + random.nextDouble() * 90; // Hidden layer 1: 10-100 neurons
            position[1] = 5 + random.nextDouble() * 45;  // Hidden layer 2: 5-50 neurons
            position[2] = 0.001 + random.nextDouble() * 0.099; // Learning rate: 0.001-0.1
            position[3] = 100 + random.nextDouble() * 900; // Epochs: 100-1000
            
            particle.setPosition(new ArrayRealVector(position));
            particle.setVelocity(new ArrayRealVector(4));
            particle.setPersonalBestPosition(particle.getPosition().copy());
            particle.setPersonalBestFitness(Double.MAX_VALUE);
            
            particles.add(particle);
        }
        
        return particles;
    }

    /**
     * Evaluate fitness of a particle (neural network configuration)
     */
    private double evaluateFitness(Particle particle, NeuralNetworkService.TrainingData trainingData) {
        try {
            // Extract parameters from particle position
            int hiddenLayer1Size = (int) Math.round(particle.getPosition().getEntry(0));
            int hiddenLayer2Size = (int) Math.round(particle.getPosition().getEntry(1));
            double learningRate = particle.getPosition().getEntry(2);
            int epochs = (int) Math.round(particle.getPosition().getEntry(3));
            
            // Validate parameters
            if (hiddenLayer1Size < 5 || hiddenLayer1Size > 200 ||
                hiddenLayer2Size < 2 || hiddenLayer2Size > 100 ||
                learningRate < 0.0001 || learningRate > 0.5 ||
                epochs < 50 || epochs > 2000) {
                return Double.MAX_VALUE; // Penalty for invalid parameters
            }
            
            // Create and train a simple neural network for fitness evaluation
            return trainAndEvaluateNetwork(hiddenLayer1Size, hiddenLayer2Size, learningRate, epochs, trainingData);
            
        } catch (Exception e) {
            log.warn("Error evaluating particle fitness: {}", e.getMessage());
            return Double.MAX_VALUE;
        }
    }

    /**
     * Train and evaluate a neural network for fitness calculation
     */
    private double trainAndEvaluateNetwork(int hiddenLayer1Size, int hiddenLayer2Size, 
                                        double learningRate, int epochs, 
                                        NeuralNetworkService.TrainingData trainingData) {
        try {
            // Use a simple neural network for quick fitness evaluation
            // In a real implementation, you might use cross-validation
            
            // Split data into training and validation sets
            int splitIndex = (int) (trainingData.inputs.size() * 0.8);
            List<double[]> trainInputs = trainingData.inputs.subList(0, splitIndex);
            List<double[]> trainOutputs = trainingData.outputs.subList(0, splitIndex);
            List<double[]> valInputs = trainingData.inputs.subList(splitIndex, trainingData.inputs.size());
            List<double[]> valOutputs = trainingData.outputs.subList(splitIndex, trainingData.outputs.size());
            
            // Normalize data
            double[][] normalizedTrainInputs = normalizeData(trainInputs);
            double[][] normalizedTrainOutputs = normalizeData(trainOutputs);
            double[][] normalizedValInputs = normalizeData(valInputs);
            double[][] normalizedValOutputs = normalizeData(valOutputs);
            
            // Create simple neural network using Neuroph
            int inputSize = trainInputs.get(0).length;
            int outputSize = 1;
            
            int[] layerSizes = {inputSize, hiddenLayer1Size, hiddenLayer2Size, outputSize};
            
            // Train network (simplified training for fitness evaluation)
            double mse = trainSimpleNetwork(layerSizes, normalizedTrainInputs, normalizedTrainOutputs, 
                                          normalizedValInputs, normalizedValOutputs, learningRate, epochs);
            
            return mse;
            
        } catch (Exception e) {
            log.warn("Error in network training for fitness: {}", e.getMessage());
            return Double.MAX_VALUE;
        }
    }

    /**
     * Train a simple neural network using Neuroph
     */
    private double trainSimpleNetwork(int[] layerSizes, double[][] trainInputs, double[][] trainOutputs,
                                   double[][] valInputs, double[][] valOutputs, double learningRate, int epochs) {
        try {
            // Create neural network
            org.neuroph.nnet.MultiLayerPerceptron network = new org.neuroph.nnet.MultiLayerPerceptron(layerSizes);
            network.setLearningRule(new org.neuroph.core.learning.BackPropagation());
            network.getLearningRule().setLearningRate(learningRate);
            
            // Create training dataset
            org.neuroph.core.data.DataSet trainingSet = new org.neuroph.core.data.DataSet(trainInputs[0].length, 1);
            for (int i = 0; i < trainInputs.length; i++) {
                trainingSet.addRow(new org.neuroph.core.data.DataSetRow(trainInputs[i], trainOutputs[i]));
            }
            
            // Train network
            network.learn(trainingSet, epochs);
            
            // Calculate validation MSE
            double mse = 0.0;
            for (int i = 0; i < valInputs.length; i++) {
                network.setInput(valInputs[i]);
                network.calculate();
                double predicted = network.getOutput()[0];
                double actual = valOutputs[i][0];
                mse += Math.pow(predicted - actual, 2);
            }
            mse /= valInputs.length;
            
            return mse;
            
        } catch (Exception e) {
            log.warn("Error in simple network training: {}", e.getMessage());
            return Double.MAX_VALUE;
        }
    }

    /**
     * Normalize data for neural network training
     */
    private double[][] normalizeData(List<double[]> data) {
        if (data.isEmpty()) return new double[0][0];
        
        int featureCount = data.get(0).length;
        double[][] normalized = new double[data.size()][featureCount];
        
        // Calculate means and standard deviations
        double[] means = new double[featureCount];
        double[] stds = new double[featureCount];
        
        // Calculate means
        for (double[] row : data) {
            for (int i = 0; i < featureCount; i++) {
                means[i] += row[i];
            }
        }
        for (int i = 0; i < featureCount; i++) {
            means[i] /= data.size();
        }
        
        // Calculate standard deviations
        for (double[] row : data) {
            for (int i = 0; i < featureCount; i++) {
                stds[i] += Math.pow(row[i] - means[i], 2);
            }
        }
        for (int i = 0; i < featureCount; i++) {
            stds[i] = Math.sqrt(stds[i] / data.size());
            if (stds[i] == 0) stds[i] = 1.0;
        }
        
        // Normalize
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < featureCount; j++) {
                normalized[i][j] = (data.get(i)[j] - means[j]) / stds[j];
            }
        }
        
        return normalized;
    }

    /**
     * Update particle velocity and position
     */
    private void updateParticle(Particle particle, Particle globalBest) {
        Random random = new Random();
        RealVector velocity = particle.getVelocity();
        RealVector position = particle.getPosition();
        
        // Update velocity
        for (int i = 0; i < velocity.getDimension(); i++) {
            double cognitiveComponent = COGNITIVE_WEIGHT * random.nextDouble() * 
                (particle.getPersonalBestPosition().getEntry(i) - position.getEntry(i));
            double socialComponent = SOCIAL_WEIGHT * random.nextDouble() * 
                (globalBest.getPosition().getEntry(i) - position.getEntry(i));
            
            double newVelocity = INERTIA_WEIGHT * velocity.getEntry(i) + cognitiveComponent + socialComponent;
            velocity.setEntry(i, newVelocity);
        }
        
        // Update position
        for (int i = 0; i < position.getDimension(); i++) {
            double newPosition = position.getEntry(i) + velocity.getEntry(i);
            position.setEntry(i, newPosition);
        }
    }

    /**
     * Create optimization result from best particle
     */
    private PSOOptimizationResult createOptimizationResult(Particle bestParticle, NeuralNetworkService.TrainingData trainingData) {
        PSOOptimizationResult result = new PSOOptimizationResult();
        
        result.hiddenLayer1Size = (int) Math.round(bestParticle.getPosition().getEntry(0));
        result.hiddenLayer2Size = (int) Math.round(bestParticle.getPosition().getEntry(1));
        result.learningRate = bestParticle.getPosition().getEntry(2);
        result.epochs = (int) Math.round(bestParticle.getPosition().getEntry(3));
        result.fitness = bestParticle.getFitness();
        result.particleCount = DEFAULT_PARTICLE_COUNT;
        result.iterations = DEFAULT_ITERATIONS;
        result.inputSize = trainingData.inputs.get(0).length;
        
        return result;
    }

    /**
     * Particle class for PSO
     */
    private static class Particle {
        private RealVector position;
        private RealVector velocity;
        private RealVector personalBestPosition;
        private double personalBestFitness;
        private double fitness;

        public RealVector getPosition() { return position; }
        public void setPosition(RealVector position) { this.position = position; }
        
        public RealVector getVelocity() { return velocity; }
        public void setVelocity(RealVector velocity) { this.velocity = velocity; }
        
        public RealVector getPersonalBestPosition() { return personalBestPosition; }
        public void setPersonalBestPosition(RealVector personalBestPosition) { this.personalBestPosition = personalBestPosition; }
        
        public double getPersonalBestFitness() { return personalBestFitness; }
        public void setPersonalBestFitness(double personalBestFitness) { this.personalBestFitness = personalBestFitness; }
        
        public double getFitness() { return fitness; }
        public void setFitness(double fitness) { this.fitness = fitness; }
        
        public Particle copy() {
            Particle copy = new Particle();
            copy.position = this.position.copy();
            copy.velocity = this.velocity.copy();
            copy.personalBestPosition = this.personalBestPosition.copy();
            copy.personalBestFitness = this.personalBestFitness;
            copy.fitness = this.fitness;
            return copy;
        }
    }

    /**
     * Result of PSO optimization
     */
    public static class PSOOptimizationResult {
        public int hiddenLayer1Size;
        public int hiddenLayer2Size;
        public double learningRate;
        public int epochs;
        public double fitness;
        public int particleCount;
        public int iterations;
        public int inputSize;
    }
} 