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
 * Entity representing energy planning scenarios
 * Scenarios define different energy policy paths and assumptions for forecasting
 */
@Entity
@Table(name = "scenarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the scenario (e.g., "Baseline", "Renewables Boost", "Coal Phaseout")
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Description of the scenario and its assumptions
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Scenario type (BASELINE, POLICY_CHANGE, TECHNOLOGY_SHIFT, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_type", nullable = false)
    private ScenarioType scenarioType;

    /**
     * Start year for the scenario
     */
    @Column(name = "start_year", nullable = false)
    private Integer startYear;

    /**
     * End year for the scenario (typically 30 years from start)
     */
    @Column(name = "end_year", nullable = false)
    private Integer endYear;

    /**
     * Annual GDP growth rate assumption (%)
     */
    @Column(name = "gdp_growth_rate", precision = 5, scale = 2)
    private BigDecimal gdpGrowthRate;

    /**
     * Annual population growth rate assumption (%)
     */
    @Column(name = "population_growth_rate", precision = 5, scale = 2)
    private BigDecimal populationGrowthRate;

    /**
     * Energy efficiency improvement rate (% per year)
     */
    @Column(name = "efficiency_improvement_rate", precision = 5, scale = 2)
    private BigDecimal efficiencyImprovementRate;

    /**
     * Renewable energy penetration target (%)
     */
    @Column(name = "renewable_target", precision = 5, scale = 2)
    private BigDecimal renewableTarget;

    /**
     * Carbon price assumption (currency per ton CO2)
     */
    @Column(name = "carbon_price", precision = 10, scale = 2)
    private BigDecimal carbonPrice;

    /**
     * Whether this scenario is active/current
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    /**
     * Created by user
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * Timestamp when the scenario was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the scenario was last updated
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

    /**
     * Scenario types enumeration
     */
    public enum ScenarioType {
        BASELINE,
        POLICY_CHANGE,
        TECHNOLOGY_SHIFT,
        CLIMATE_ACTION,
        ECONOMIC_CRISIS,
        ENERGY_CRISIS,
        CUSTOM
    }
} 