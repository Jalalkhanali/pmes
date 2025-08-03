package aut.energy.repository;

import aut.energy.entity.ForecastResult;
import aut.energy.entity.Scenario;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ForecastResult entity
 * Provides data access methods for energy demand forecast results
 */
@Repository
public interface ForecastResultRepository extends JpaRepository<ForecastResult, Long> {

    /**
     * Find forecast results by scenario
     */
    List<ForecastResult> findByScenarioOrderByForecastYearAscSectorAscEnergySourceAsc(Scenario scenario);

    /**
     * Find forecast results by scenario and year
     */
    List<ForecastResult> findByScenarioAndForecastYearOrderBySectorAscEnergySourceAsc(Scenario scenario, Integer forecastYear);

    /**
     * Find forecast results by scenario and sector
     */
    List<ForecastResult> findByScenarioAndSectorOrderByForecastYearAscEnergySourceAsc(Scenario scenario, String sector);

    /**
     * Find forecast results by scenario and energy source
     */
    List<ForecastResult> findByScenarioAndEnergySourceOrderByForecastYearAscSectorAsc(Scenario scenario, String energySource);

    /**
     * Find baseline forecast results
     */
    List<ForecastResult> findByIsBaselineTrueOrderByForecastYearAscSectorAscEnergySourceAsc();

    /**
     * Find forecast results within a year range
     */
    List<ForecastResult> findByForecastYearBetweenOrderByForecastYearAscSectorAscEnergySourceAsc(Integer startYear, Integer endYear);

    /**
     * Find forecast results by scenario within a year range
     */
    List<ForecastResult> findByScenarioAndForecastYearBetweenOrderByForecastYearAscSectorAscEnergySourceAsc(Scenario scenario, Integer startYear, Integer endYear);

    /**
     * Find the latest forecast year for a scenario
     */
    @Query("SELECT MAX(fr.forecastYear) FROM ForecastResult fr WHERE fr.scenario = :scenario")
    Optional<Integer> findLatestForecastYearByScenario(@Param("scenario") Scenario scenario);

    /**
     * Find the earliest forecast year for a scenario
     */
    @Query("SELECT MIN(fr.forecastYear) FROM ForecastResult fr WHERE fr.scenario = :scenario")
    Optional<Integer> findEarliestForecastYearByScenario(@Param("scenario") Scenario scenario);

    /**
     * Calculate total forecasted consumption by year and scenario
     */
    @Query("SELECT fr.forecastYear, fr.scenario.id, SUM(fr.forecastedConsumptionTwh) FROM ForecastResult fr " +
           "WHERE fr.scenario = :scenario AND fr.forecastYear BETWEEN :startYear AND :endYear " +
           "GROUP BY fr.forecastYear, fr.scenario.id ORDER BY fr.forecastYear")
    List<Object[]> calculateTotalForecastedConsumptionByYear(@Param("scenario") Scenario scenario, 
                                                            @Param("startYear") Integer startYear, 
                                                            @Param("endYear") Integer endYear);

    /**
     * Calculate total forecasted consumption by sector and scenario
     */
    @Query("SELECT fr.sector, fr.scenario.id, SUM(fr.forecastedConsumptionTwh) FROM ForecastResult fr " +
           "WHERE fr.scenario = :scenario AND fr.forecastYear BETWEEN :startYear AND :endYear " +
           "GROUP BY fr.sector, fr.scenario.id ORDER BY fr.sector")
    List<Object[]> calculateTotalForecastedConsumptionBySector(@Param("scenario") Scenario scenario, 
                                                               @Param("startYear") Integer startYear, 
                                                               @Param("endYear") Integer endYear);

    /**
     * Calculate total forecasted consumption by energy source and scenario
     */
    @Query("SELECT fr.energySource, fr.scenario.id, SUM(fr.forecastedConsumptionTwh) FROM ForecastResult fr " +
           "WHERE fr.scenario = :scenario AND fr.forecastYear BETWEEN :startYear AND :endYear " +
           "GROUP BY fr.energySource, fr.scenario.id ORDER BY fr.energySource")
    List<Object[]> calculateTotalForecastedConsumptionByEnergySource(@Param("scenario") Scenario scenario, 
                                                                     @Param("startYear") Integer startYear, 
                                                                     @Param("endYear") Integer endYear);

    /**
     * Find forecast results with high confidence level
     */
    @Query("SELECT fr FROM ForecastResult fr WHERE fr.confidenceLevel >= :minConfidence " +
           "ORDER BY fr.confidenceLevel DESC")
    List<ForecastResult> findHighConfidenceForecasts(@Param("minConfidence") BigDecimal minConfidence);

    /**
     * Find forecast results with high model accuracy
     */
    @Query("SELECT fr FROM ForecastResult fr WHERE fr.modelAccuracy >= :minAccuracy " +
           "ORDER BY fr.modelAccuracy DESC")
    List<ForecastResult> findHighAccuracyForecasts(@Param("minAccuracy") BigDecimal minAccuracy);

    /**
     * Find forecast results by neural network architecture
     */
    List<ForecastResult> findByNnArchitectureContainingOrderByForecastYearAscSectorAscEnergySourceAsc(String architecture);

    /**
     * Find forecast results by scenario type
     */
    @Query("SELECT fr FROM ForecastResult fr WHERE fr.scenario.scenarioType = :scenarioType " +
           "ORDER BY fr.forecastYear, fr.sector, fr.energySource")
    List<ForecastResult> findByScenarioType(@Param("scenarioType") Scenario.ScenarioType scenarioType);

    /**
     * Compare forecast results between two scenarios
     */
    @Query("SELECT fr1.forecastYear, fr1.sector, fr1.energySource, " +
           "fr1.forecastedConsumptionTwh as scenario1_consumption, " +
           "fr2.forecastedConsumptionTwh as scenario2_consumption " +
           "FROM ForecastResult fr1 " +
           "LEFT JOIN ForecastResult fr2 ON fr1.forecastYear = fr2.forecastYear " +
           "AND fr1.sector = fr2.sector AND fr1.energySource = fr2.energySource " +
           "WHERE fr1.scenario = :scenario1 AND fr2.scenario = :scenario2 " +
           "ORDER BY fr1.forecastYear, fr1.sector, fr1.energySource")
    List<Object[]> compareForecastResults(@Param("scenario1") Scenario scenario1, 
                                         @Param("scenario2") Scenario scenario2);

    /**
     * Find forecast results with consumption above a threshold
     */
    @Query("SELECT fr FROM ForecastResult fr WHERE fr.forecastedConsumptionTwh > :threshold " +
           "ORDER BY fr.forecastedConsumptionTwh DESC")
    List<ForecastResult> findHighConsumptionForecasts(@Param("threshold") BigDecimal threshold);

    /**
     * Calculate average forecast accuracy by scenario
     */
    @Query("SELECT fr.scenario.id, AVG(fr.modelAccuracy) FROM ForecastResult fr " +
           "WHERE fr.modelAccuracy IS NOT NULL GROUP BY fr.scenario.id")
    List<Object[]> calculateAverageAccuracyByScenario();

    /**
     * Find forecast results created within a date range
     */
    List<ForecastResult> findByCreatedAtBetweenOrderByCreatedAtDesc(java.time.LocalDateTime startDate, 
                                                                    java.time.LocalDateTime endDate);
} 