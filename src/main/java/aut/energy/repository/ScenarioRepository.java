package aut.energy.repository;

import aut.energy.entity.Scenario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Scenario entity
 * Provides data access methods for energy planning scenarios
 */
@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    /**
     * Find scenarios by type
     */
    List<Scenario> findByScenarioTypeOrderByNameAsc(Scenario.ScenarioType scenarioType);

    /**
     * Find active scenarios
     */
    List<Scenario> findByIsActiveTrueOrderByNameAsc();

    /**
     * Find scenarios by name (case-insensitive)
     */
    List<Scenario> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    /**
     * Find scenarios by description (case-insensitive)
     */
    List<Scenario> findByDescriptionContainingIgnoreCaseOrderByNameAsc(String description);

    /**
     * Find scenarios within a year range
     */
    List<Scenario> findByStartYearLessThanEqualAndEndYearGreaterThanEqualOrderByNameAsc(Integer year, Integer year2);

    /**
     * Find baseline scenarios
     */
    @Query("SELECT s FROM Scenario s WHERE s.scenarioType = 'BASELINE' ORDER BY s.name")
    List<Scenario> findBaselineScenarios();

    /**
     * Find scenarios created by a specific user
     */
    List<Scenario> findByCreatedByOrderByNameAsc(String createdBy);

    /**
     * Find the currently active scenario
     */
    Optional<Scenario> findByIsActiveTrue();

    /**
     * Check if a scenario name exists
     */
    boolean existsByName(String name);

    /**
     * Find scenarios with renewable targets above a certain percentage
     */
    @Query("SELECT s FROM Scenario s WHERE s.renewableTarget >= :minTarget ORDER BY s.renewableTarget DESC")
    List<Scenario> findScenariosWithHighRenewableTarget(@Param("minTarget") Double minTarget);

    /**
     * Find scenarios with carbon price above a certain value
     */
    @Query("SELECT s FROM Scenario s WHERE s.carbonPrice >= :minPrice ORDER BY s.carbonPrice DESC")
    List<Scenario> findScenariosWithHighCarbonPrice(@Param("minPrice") Double minPrice);

    /**
     * Count scenarios by type
     */
    @Query("SELECT s.scenarioType, COUNT(s) FROM Scenario s GROUP BY s.scenarioType")
    List<Object[]> countScenariosByType();

    /**
     * Find scenarios that overlap with a given year range
     */
    @Query("SELECT s FROM Scenario s WHERE " +
           "(s.startYear <= :endYear AND s.endYear >= :startYear) " +
           "ORDER BY s.startYear, s.name")
    List<Scenario> findScenariosOverlappingYearRange(@Param("startYear") Integer startYear, 
                                                    @Param("endYear") Integer endYear);
} 