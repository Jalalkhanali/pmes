package aut.energy.repository;

import aut.energy.entity.EmissionFactor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for EmissionFactor entity
 * Provides data access methods for emission factors
 */
@Repository
public interface EmissionFactorRepository extends JpaRepository<EmissionFactor, Long> {

    /**
     * Find emission factors by energy source
     */
    List<EmissionFactor> findByEnergySourceOrderBySectorAscTechnologyTypeAsc(String energySource);

    /**
     * Find emission factors by sector
     */
    List<EmissionFactor> findBySectorOrderByEnergySourceAscTechnologyTypeAsc(String sector);

    /**
     * Find emission factors by energy source and sector
     */
    List<EmissionFactor> findByEnergySourceAndSectorOrderByTechnologyTypeAsc(String energySource, String sector);

    /**
     * Find emission factors by technology type
     */
    List<EmissionFactor> findByTechnologyTypeOrderByEnergySourceAscSectorAsc(String technologyType);

    /**
     * Find active emission factors
     */
    List<EmissionFactor> findByIsActiveTrueOrderByEnergySourceAscSectorAsc();

    /**
     * Find emission factors by valid year
     */
    List<EmissionFactor> findByValidYearOrderByEnergySourceAscSectorAsc(Integer validYear);

    /**
     * Find emission factors within a year range
     */
    List<EmissionFactor> findByValidYearBetweenOrderByValidYearAscEnergySourceAscSectorAsc(Integer startYear, Integer endYear);

    /**
     * Find emission factors by data source
     */
    List<EmissionFactor> findByDataSourceOrderByEnergySourceAscSectorAsc(String dataSource);

    /**
     * Find the most recent emission factor for a specific energy source and sector
     */
    @Query("SELECT ef FROM EmissionFactor ef WHERE ef.energySource = :energySource " +
           "AND ef.sector = :sector AND ef.isActive = true " +
           "ORDER BY ef.validYear DESC")
    Optional<EmissionFactor> findMostRecentByEnergySourceAndSector(@Param("energySource") String energySource, 
                                                                   @Param("sector") String sector);

    /**
     * Find emission factors with CO2 factor above a threshold
     */
    @Query("SELECT ef FROM EmissionFactor ef WHERE ef.co2Factor > :threshold AND ef.isActive = true " +
           "ORDER BY ef.co2Factor DESC")
    List<EmissionFactor> findHighCO2EmissionFactors(@Param("threshold") BigDecimal threshold);

    /**
     * Find emission factors with NOx factor above a threshold
     */
    @Query("SELECT ef FROM EmissionFactor ef WHERE ef.noxFactor > :threshold AND ef.isActive = true " +
           "ORDER BY ef.noxFactor DESC")
    List<EmissionFactor> findHighNOxEmissionFactors(@Param("threshold") BigDecimal threshold);

    /**
     * Find emission factors with SO2 factor above a threshold
     */
    @Query("SELECT ef FROM EmissionFactor ef WHERE ef.so2Factor > :threshold AND ef.isActive = true " +
           "ORDER BY ef.so2Factor DESC")
    List<EmissionFactor> findHighSO2EmissionFactors(@Param("threshold") BigDecimal threshold);

    /**
     * Get distinct energy sources from emission factors
     */
    @Query("SELECT DISTINCT ef.energySource FROM EmissionFactor ef WHERE ef.isActive = true ORDER BY ef.energySource")
    List<String> findDistinctEnergySources();

    /**
     * Get distinct sectors from emission factors
     */
    @Query("SELECT DISTINCT ef.sector FROM EmissionFactor ef WHERE ef.isActive = true ORDER BY ef.sector")
    List<String> findDistinctSectors();

    /**
     * Get distinct technology types from emission factors
     */
    @Query("SELECT DISTINCT ef.technologyType FROM EmissionFactor ef WHERE ef.technologyType IS NOT NULL ORDER BY ef.technologyType")
    List<String> findDistinctTechnologyTypes();

    /**
     * Calculate average CO2 factor by energy source
     */
    @Query("SELECT ef.energySource, AVG(ef.co2Factor) FROM EmissionFactor ef " +
           "WHERE ef.isActive = true GROUP BY ef.energySource ORDER BY ef.energySource")
    List<Object[]> calculateAverageCO2FactorByEnergySource();

    /**
     * Calculate average emission factors by sector
     */
    @Query("SELECT ef.sector, AVG(ef.co2Factor), AVG(ef.noxFactor), AVG(ef.so2Factor) " +
           "FROM EmissionFactor ef WHERE ef.isActive = true " +
           "GROUP BY ef.sector ORDER BY ef.sector")
    List<Object[]> calculateAverageEmissionFactorsBySector();

    /**
     * Find emission factors for renewable energy sources
     */
    @Query("SELECT ef FROM EmissionFactor ef WHERE ef.energySource IN ('RENEWABLES', 'SOLAR', 'WIND', 'HYDRO', 'BIOMASS') " +
           "AND ef.isActive = true ORDER BY ef.energySource, ef.sector")
    List<EmissionFactor> findRenewableEnergyEmissionFactors();

    /**
     * Find emission factors for fossil fuel sources
     */
    @Query("SELECT ef FROM EmissionFactor ef WHERE ef.energySource IN ('COAL', 'GAS', 'OIL', 'PETROLEUM') " +
           "AND ef.isActive = true ORDER BY ef.energySource, ef.sector")
    List<EmissionFactor> findFossilFuelEmissionFactors();
} 