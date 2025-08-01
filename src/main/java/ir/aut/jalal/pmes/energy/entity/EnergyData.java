package ir.aut.jalal.pmes.energy.entity;

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
 * Entity representing historical energy consumption data
 * This stores the raw data imported from Excel files for energy planning analysis
 */
@Entity
@Table(name = "energy_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Year of the energy data
     */
    @Column(name = "year", nullable = false)
    private Integer year;

    /**
     * Energy sector (e.g., INDUSTRIAL, RESIDENTIAL, COMMERCIAL, TRANSPORT)
     */
    @Column(name = "sector", nullable = false)
    private String sector;

    /**
     * Energy source/fuel type (e.g., COAL, GAS, OIL, RENEWABLES, NUCLEAR)
     */
    @Column(name = "energy_source", nullable = false)
    private String energySource;

    /**
     * Energy consumption in TWh (Terawatt-hours)
     */
    @Column(name = "consumption_twh", nullable = false, precision = 15, scale = 3)
    private BigDecimal consumptionTwh;

    /**
     * GDP in billions of currency units (for correlation analysis)
     */
    @Column(name = "gdp_billions", precision = 15, scale = 3)
    private BigDecimal gdpBillions;

    /**
     * Population in millions (for per capita calculations)
     */
    @Column(name = "population_millions", precision = 10, scale = 2)
    private BigDecimal populationMillions;

    /**
     * Average temperature in Celsius (for seasonal adjustments)
     */
    @Column(name = "avg_temperature_celsius", precision = 5, scale = 2)
    private BigDecimal avgTemperatureCelsius;

    /**
     * Additional metadata or notes
     */
    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * Source of the data (e.g., "Excel Import", "Manual Entry")
     */
    @Column(name = "data_source", length = 100)
    private String dataSource;

    /**
     * Timestamp when the record was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated
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