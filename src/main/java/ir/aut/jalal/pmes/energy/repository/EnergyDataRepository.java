package ir.aut.jalal.pmes.energy.repository;

import ir.aut.jalal.pmes.energy.entity.EnergyData;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for EnergyData entity
 * Provides data access methods for energy consumption data
 */
@Repository
public interface EnergyDataRepository extends JpaRepository<EnergyData, Long> {

    /**
     * Find energy data by year and sector
     */
    List<EnergyData> findByYearAndSectorOrderByEnergySource(Integer year, String sector);

    /**
     * Find energy data by year and energy source
     */
    List<EnergyData> findByYearAndEnergySourceOrderBySector(Integer year, String energySource);

    /**
     * Find energy data within a year range
     */
    List<EnergyData> findByYearBetweenOrderByYearAscSectorAscEnergySourceAsc(Integer startYear, Integer endYear);

    /**
     * Find energy data by sector within a year range
     */
    List<EnergyData> findBySectorAndYearBetweenOrderByYearAscEnergySourceAsc(String sector, Integer startYear, Integer endYear);

    /**
     * Find energy data by energy source within a year range
     */
    List<EnergyData> findByEnergySourceAndYearBetweenOrderByYearAscSectorAsc(String energySource, Integer startYear, Integer endYear);

    /**
     * Get distinct sectors from energy data
     */
    @Query("SELECT DISTINCT e.sector FROM EnergyData e ORDER BY e.sector")
    List<String> findDistinctSectors();

    /**
     * Get distinct energy sources from energy data
     */
    @Query("SELECT DISTINCT e.energySource FROM EnergyData e ORDER BY e.energySource")
    List<String> findDistinctEnergySources();

    /**
     * Get distinct years from energy data
     */
    @Query("SELECT DISTINCT e.year FROM EnergyData e ORDER BY e.year")
    List<Integer> findDistinctYears();

    /**
     * Calculate total consumption by year and sector
     */
    @Query("SELECT e.year, e.sector, SUM(e.consumptionTwh) FROM EnergyData e " +
           "WHERE e.year BETWEEN :startYear AND :endYear " +
           "GROUP BY e.year, e.sector ORDER BY e.year, e.sector")
    List<Object[]> calculateTotalConsumptionByYearAndSector(@Param("startYear") Integer startYear, 
                                                           @Param("endYear") Integer endYear);

    /**
     * Calculate total consumption by year and energy source
     */
    @Query("SELECT e.year, e.energySource, SUM(e.consumptionTwh) FROM EnergyData e " +
           "WHERE e.year BETWEEN :startYear AND :endYear " +
           "GROUP BY e.year, e.energySource ORDER BY e.year, e.energySource")
    List<Object[]> calculateTotalConsumptionByYearAndEnergySource(@Param("startYear") Integer startYear, 
                                                                 @Param("endYear") Integer endYear);

    /**
     * Find the latest year with data
     */
    @Query("SELECT MAX(e.year) FROM EnergyData e")
    Optional<Integer> findLatestYear();

    /**
     * Find the earliest year with data
     */
    @Query("SELECT MIN(e.year) FROM EnergyData e")
    Optional<Integer> findEarliestYear();

    /**
     * Check if data exists for a specific year, sector, and energy source combination
     */
    boolean existsByYearAndSectorAndEnergySource(Integer year, String sector, String energySource);

    /**
     * Find energy data by data source
     */
    List<EnergyData> findByDataSourceOrderByYearAscSectorAscEnergySourceAsc(String dataSource);

    /**
     * Delete energy data by data source (useful for cleaning up imported data)
     */
    void deleteByDataSource(String dataSource);
} 