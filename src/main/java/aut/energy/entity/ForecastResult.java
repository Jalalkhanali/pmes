package aut.energy.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity representing energy demand forecast results
 * Stores the output of ANN forecasting for different scenarios and years
 */
@Entity
@Table(name = "forecast_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Associated scenario for this forecast
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;

    /**
     * Year for which the forecast is made
     */
    @Column(name = "forecast_year", nullable = false)
    private Integer forecastYear;

    /**
     * Energy sector (e.g., INDUSTRIAL, RESIDENTIAL, COMMERCIAL, TRANSPORT)
     */
    @Column(name = "sector", nullable = false, length = 50)
    private String sector;

    /**
     * Energy source/fuel type (e.g., COAL, GAS, OIL, RENEWABLES, NUCLEAR)
     */
    @Column(name = "energy_source", nullable = false, length = 50)
    private String energySource;

    /**
     * Forecasted energy consumption in TWh
     */
    @Column(name = "forecasted_consumption_twh", nullable = false, precision = 15, scale = 3)
    private BigDecimal forecastedConsumptionTwh;

    /**
     * Lower bound of the forecast confidence interval (TWh)
     */
    @Column(name = "lower_bound_twh", precision = 15, scale = 3)
    private BigDecimal lowerBoundTwh;

    /**
     * Upper bound of the forecast confidence interval (TWh)
     */
    @Column(name = "upper_bound_twh", precision = 15, scale = 3)
    private BigDecimal upperBoundTwh;

    /**
     * Confidence level of the forecast (0-1)
     */
    @Column(name = "confidence_level", precision = 3, scale = 2)
    private BigDecimal confidenceLevel;

    /**
     * Model accuracy metrics (e.g., RMSE, MAE)
     */
    @Column(name = "model_accuracy", precision = 10, scale = 4)
    private BigDecimal modelAccuracy;

    /**
     * Neural network architecture used for this forecast
     */
    @Column(name = "nn_architecture", length = 500)
    private String nnArchitecture;

    /**
     * PSO parameters used for optimization
     */
    @Column(name = "pso_parameters", length = 1000)
    private String psoParameters;

    /**
     * Whether this is a baseline forecast (no scenario modifications)
     */
    @Column(name = "is_baseline", nullable = false)
    private Boolean isBaseline = false;

    /**
     * Additional forecast metadata
     */
    @Column(name = "metadata", length = 2000)
    private String metadata;

    /**
     * Timestamp when the forecast was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the forecast was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Version for optimistic locking
     */
    @Version
    @Column(name = "version")
    private Long version;
} 