package ir.aut.jalal.pmes.energy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Particle Swarm Optimization Service for neural network weight optimization
 */
@Service
@Slf4j
public class ParticleSwarmOptimizationService {

    // PSO Configuration
    private static final int DEFAULT_PARTICLE_COUNT = 30;
    private static final int DEFAULT_ITERATIONS = 100;
    private static final double INERTIA_WEIGHT = 0.7;
    private static final double COGNITIVE_WEIGHT = 1.5;
    private static final double SOCIAL_WEIGHT = 1.5;

    /**
     * Optimize neural network weights using PSO
     */
    public double[] optimizeNeuralNetworkWeights(NeuralNetwork network, TrainingData trainingData) {
        log.info("Starting PSO optimization for neural network weights");

        int particleCount = DEFAULT_PARTICLE_COUNT;
        int iterations = DEFAULT_ITERATIONS;
        int dimension = network.getWeights().length;

        // Initialize particles
        Particle[] particles = new Particle[particleCount];
        for (int i = 0; i < particleCount; i++) {
            particles[i] = new Particle(dimension);
        }

        // Initialize global best
        double[] globalBestPosition = new double[dimension];
        double globalBestFitness = Double.MAX_VALUE;

        // Initialize particles with random positions
        Random random = new Random();
        for (Particle particle : particles) {
            for (int j = 0; j < dimension; j++) {
                particle.position[j] = random.nextGaussian() * 0.1;
                particle.velocity[j] = random.nextGaussian() * 0.01;
            }
            
            // Evaluate initial fitness
            particle.fitness = evaluateFitness(particle.position, network, trainingData);
            particle.bestPosition = particle.position.clone();
            particle.bestFitness = particle.fitness;

            // Update global best
            if (particle.fitness < globalBestFitness) {
                globalBestFitness = particle.fitness;
                System.arraycopy(particle.position, 0, globalBestPosition, 0, dimension);
            }
        }

        // Main PSO loop
        for (int iteration = 0; iteration < iterations; iteration++) {
            for (Particle particle : particles) {
                // Update velocity
                for (int j = 0; j < dimension; j++) {
                    double r1 = random.nextDouble();
                    double r2 = random.nextDouble();

                    particle.velocity[j] = INERTIA_WEIGHT * particle.velocity[j] +
                            COGNITIVE_WEIGHT * r1 * (particle.bestPosition[j] - particle.position[j]) +
                            SOCIAL_WEIGHT * r2 * (globalBestPosition[j] - particle.position[j]);
                }

                // Update position
                for (int j = 0; j < dimension; j++) {
                    particle.position[j] += particle.velocity[j];
                }

                // Evaluate fitness
                particle.fitness = evaluateFitness(particle.position, network, trainingData);

                // Update personal best
                if (particle.fitness < particle.bestFitness) {
                    particle.bestFitness = particle.fitness;
                    System.arraycopy(particle.position, 0, particle.bestPosition, 0, dimension);
                }

                // Update global best
                if (particle.fitness < globalBestFitness) {
                    globalBestFitness = particle.fitness;
                    System.arraycopy(particle.position, 0, globalBestPosition, 0, dimension);
                }
            }

            if (iteration % 10 == 0) {
                log.debug("PSO iteration {}: best fitness = {}", iteration, globalBestFitness);
            }
        }

        log.info("PSO optimization completed. Best fitness: {}", globalBestFitness);
        return globalBestPosition;
    }

    /**
     * Evaluate fitness of a weight configuration
     */
    private double evaluateFitness(double[] weights, NeuralNetwork network, TrainingData trainingData) {
        // Set weights to network
        network.setWeights(weights);

        // Calculate mean squared error
        double totalError = 0.0;
        int sampleCount = 0;

        for (int i = 0; i < trainingData.inputs.size(); i++) {
            double[] input = trainingData.inputs.get(i);
            double[] expectedOutput = trainingData.outputs.get(i);

            // Forward pass
            double[] actualOutput = network.forward(input);

            // Calculate error
            for (int j = 0; j < actualOutput.length; j++) {
                double error = actualOutput[j] - expectedOutput[j];
                totalError += error * error;
            }
            sampleCount++;
        }

        return totalError / sampleCount;
    }

    /**
     * Particle class for PSO
     */
    private static class Particle {
        double[] position;
        double[] velocity;
        double[] bestPosition;
        double fitness;
        double bestFitness;

        Particle(int dimension) {
            position = new double[dimension];
            velocity = new double[dimension];
            bestPosition = new double[dimension];
            fitness = Double.MAX_VALUE;
            bestFitness = Double.MAX_VALUE;
        }
    }

    // ==================== INNER CLASSES ====================

    /**
     * Neural Network interface for PSO
     */
    public interface NeuralNetwork {
        double[] forward(double[] input);
        void setWeights(double[] weights);
        double[] getWeights();
    }

    /**
     * Training data interface for PSO
     */
    public interface TrainingData {
        java.util.List<double[]> getInputs();
        java.util.List<double[]> getOutputs();
    }
} 