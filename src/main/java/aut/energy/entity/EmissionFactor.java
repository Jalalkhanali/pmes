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
 * Entity representing emission factors for different energy sources and sectors
 * Used to calculate CO2, NOx, SO2 emissions from energy consumption
 */
@Entity
@Table(name = "emission_factors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmissionFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Energy source/fuel type (e.g., COAL, GAS, OIL, RENEWABLES, NUCLEAR)
     */
    @Column(name = "energy_source", nullable = false, length = 50)
    private String energySource;

    /**
     * Energy sector (e.g., INDUSTRIAL, RESIDENTIAL, COMMERCIAL, TRANSPORT)
     */
    @Column(name = "sector", nullable = false, length = 50)
    private String sector;

    /**
     * Technology type (e.g., CONVENTIONAL, COMBINED_CYCLE, RENEWABLE)
     */
    @Column(name = "technology_type", length = 100)
    private String technologyType;

    /**
     * CO2 emission factor (kg CO2 per TWh)
     */
    @Column(name = "co2_factor", nullable = false, precision = 15, scale = 3)
    private BigDecimal co2Factor;

    /**
     * NOx emission factor (kg NOx per TWh)
     */
    @Column(name = "nox_factor", precision = 15, scale = 3)
    private BigDecimal noxFactor;

    /**
     * SO2 emission factor (kg SO2 per TWh)
     */
    @Column(name = "so2_factor", precision = 15, scale = 3)
    private BigDecimal so2Factor;

    /**
     * CH4 emission factor (kg CH4 per TWh)
     */
    @Column(name = "ch4_factor", precision = 15, scale = 3)
    private BigDecimal ch4Factor;

    /**
     * N2O emission factor (kg N2O per TWh)
     */
    @Column(name = "n2o_factor", precision = 15, scale = 3)
    private BigDecimal n2oFactor;

    /**
     * Year for which this emission factor is valid
     */
    @Column(name = "valid_year", nullable = false)
    private Integer validYear;

    /**
     * Source of the emission factor data
     */
    @Column(name = "data_source", length = 200)
    private String dataSource;

    /**
     * Additional notes or references
     */
    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * Whether this emission factor is currently active
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Timestamp when the emission factor was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the emission factor was last updated
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