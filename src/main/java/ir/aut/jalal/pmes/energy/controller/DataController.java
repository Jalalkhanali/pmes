package ir.aut.jalal.pmes.energy.controller;

import ir.aut.jalal.pmes.energy.entity.EmissionFactor;
import ir.aut.jalal.pmes.energy.entity.EnergyData;
import ir.aut.jalal.pmes.energy.repository.EmissionFactorRepository;
import ir.aut.jalal.pmes.energy.repository.EnergyDataRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing energy data and emission factors
 * Provides endpoints for CRUD operations on energy data and emission factors
 */
@RestController
@RequestMapping("/api/energy/data")
@RequiredArgsConstructor
@Slf4j
public class DataController {

    private final EnergyDataRepository energyDataRepository;
    private final EmissionFactorRepository emissionFactorRepository;

    // ==================== ENERGY DATA ENDPOINTS ====================

    /**
     * Get all energy data
     * 
     * @return List of all energy data
     */
    @GetMapping("/energy")
    public ResponseEntity<List<EnergyData>> getAllEnergyData() {
        try {
            List<EnergyData> energyData = energyDataRepository.findAll();
            return ResponseEntity.ok(energyData);
        } catch (Exception e) {
            log.error("Error getting energy data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get energy data by year and sector
     * 
     * @param year The year
     * @param sector The sector
     * @return List of energy data
     */
    @GetMapping("/energy/year/{year}/sector/{sector}")
    public ResponseEntity<List<EnergyData>> getEnergyDataByYearAndSector(
            @PathVariable Integer year,
            @PathVariable String sector) {
        
        try {
            List<EnergyData> energyData = energyDataRepository.findByYearAndSectorOrderByEnergySource(year, sector);
            return ResponseEntity.ok(energyData);
        } catch (Exception e) {
            log.error("Error getting energy data by year and sector: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get energy data within a year range
     * 
     * @param startYear Start year
     * @param endYear End year
     * @return List of energy data
     */
    @GetMapping("/energy/range")
    public ResponseEntity<List<EnergyData>> getEnergyDataByYearRange(
            @RequestParam Integer startYear,
            @RequestParam Integer endYear) {
        
        try {
            List<EnergyData> energyData = energyDataRepository.findByYearBetweenOrderByYearAscSectorAscEnergySourceAsc(startYear, endYear);
            return ResponseEntity.ok(energyData);
        } catch (Exception e) {
            log.error("Error getting energy data by year range: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get energy data by sector within a year range
     * 
     * @param sector The sector
     * @param startYear Start year
     * @param endYear End year
     * @return List of energy data
     */
    @GetMapping("/energy/sector/{sector}/range")
    public ResponseEntity<List<EnergyData>> getEnergyDataBySectorAndYearRange(
            @PathVariable String sector,
            @RequestParam Integer startYear,
            @RequestParam Integer endYear) {
        
        try {
            List<EnergyData> energyData = energyDataRepository.findBySectorAndYearBetweenOrderByYearAscEnergySourceAsc(sector, startYear, endYear);
            return ResponseEntity.ok(energyData);
        } catch (Exception e) {
            log.error("Error getting energy data by sector and year range: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get energy data by energy source within a year range
     * 
     * @param energySource The energy source
     * @param startYear Start year
     * @param endYear End year
     * @return List of energy data
     */
    @GetMapping("/energy/source/{energySource}/range")
    public ResponseEntity<List<EnergyData>> getEnergyDataByEnergySourceAndYearRange(
            @PathVariable String energySource,
            @RequestParam Integer startYear,
            @RequestParam Integer endYear) {
        
        try {
            List<EnergyData> energyData = energyDataRepository.findByEnergySourceAndYearBetweenOrderByYearAscSectorAsc(energySource, startYear, endYear);
            return ResponseEntity.ok(energyData);
        } catch (Exception e) {
            log.error("Error getting energy data by energy source and year range: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get distinct sectors
     * 
     * @return List of distinct sectors
     */
    @GetMapping("/energy/sectors")
    public ResponseEntity<List<String>> getDistinctSectors() {
        try {
            List<String> sectors = energyDataRepository.findDistinctSectors();
            return ResponseEntity.ok(sectors);
        } catch (Exception e) {
            log.error("Error getting distinct sectors: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get distinct energy sources
     * 
     * @return List of distinct energy sources
     */
    @GetMapping("/energy/sources")
    public ResponseEntity<List<String>> getDistinctEnergySources() {
        try {
            List<String> energySources = energyDataRepository.findDistinctEnergySources();
            return ResponseEntity.ok(energySources);
        } catch (Exception e) {
            log.error("Error getting distinct energy sources: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get distinct years
     * 
     * @return List of distinct years
     */
    @GetMapping("/energy/years")
    public ResponseEntity<List<Integer>> getDistinctYears() {
        try {
            List<Integer> years = energyDataRepository.findDistinctYears();
            return ResponseEntity.ok(years);
        } catch (Exception e) {
            log.error("Error getting distinct years: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get total consumption by year and sector
     * 
     * @param startYear Start year
     * @param endYear End year
     * @return List of total consumption data
     */
    @GetMapping("/energy/consumption/yearly-sector")
    public ResponseEntity<List<Object[]>> getTotalConsumptionByYearAndSector(
            @RequestParam Integer startYear,
            @RequestParam Integer endYear) {
        
        try {
            List<Object[]> consumption = energyDataRepository.calculateTotalConsumptionByYearAndSector(startYear, endYear);
            return ResponseEntity.ok(consumption);
        } catch (Exception e) {
            log.error("Error getting total consumption by year and sector: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get total consumption by year and energy source
     * 
     * @param startYear Start year
     * @param endYear End year
     * @return List of total consumption data
     */
    @GetMapping("/energy/consumption/yearly-source")
    public ResponseEntity<List<Object[]>> getTotalConsumptionByYearAndEnergySource(
            @RequestParam Integer startYear,
            @RequestParam Integer endYear) {
        
        try {
            List<Object[]> consumption = energyDataRepository.calculateTotalConsumptionByYearAndEnergySource(startYear, endYear);
            return ResponseEntity.ok(consumption);
        } catch (Exception e) {
            log.error("Error getting total consumption by year and energy source: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get latest year with data
     * 
     * @return The latest year
     */
    @GetMapping("/energy/latest-year")
    public ResponseEntity<Integer> getLatestYear() {
        try {
            Optional<Integer> latestYear = energyDataRepository.findLatestYear();
            return latestYear.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting latest year: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get earliest year with data
     * 
     * @return The earliest year
     */
    @GetMapping("/energy/earliest-year")
    public ResponseEntity<Integer> getEarliestYear() {
        try {
            Optional<Integer> earliestYear = energyDataRepository.findEarliestYear();
            return earliestYear.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting earliest year: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create energy data
     * 
     * @param energyData The energy data to create
     * @return The created energy data
     */
    @PostMapping("/energy")
    public ResponseEntity<EnergyData> createEnergyData(@RequestBody EnergyData energyData) {
        try {
            EnergyData createdEnergyData = energyDataRepository.save(energyData);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEnergyData);
        } catch (Exception e) {
            log.error("Error creating energy data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Update energy data
     * 
     * @param id The energy data ID
     * @param energyData The updated energy data
     * @return The updated energy data
     */
    @PutMapping("/energy/{id}")
    public ResponseEntity<EnergyData> updateEnergyData(@PathVariable Long id, @RequestBody EnergyData energyData) {
        try {
            Optional<EnergyData> existingEnergyData = energyDataRepository.findById(id);
            if (existingEnergyData.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            EnergyData existing = existingEnergyData.get();
            existing.setYear(energyData.getYear());
            existing.setSector(energyData.getSector());
            existing.setEnergySource(energyData.getEnergySource());
            existing.setConsumptionTwh(energyData.getConsumptionTwh());
            existing.setGdpBillions(energyData.getGdpBillions());
            existing.setPopulationMillions(energyData.getPopulationMillions());
            existing.setAvgTemperatureCelsius(energyData.getAvgTemperatureCelsius());
            existing.setNotes(energyData.getNotes());
            existing.setDataSource(energyData.getDataSource());
            
            EnergyData updatedEnergyData = energyDataRepository.save(existing);
            return ResponseEntity.ok(updatedEnergyData);
        } catch (Exception e) {
            log.error("Error updating energy data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Delete energy data
     * 
     * @param id The energy data ID
     * @return No content if successful
     */
    @DeleteMapping("/energy/{id}")
    public ResponseEntity<Void> deleteEnergyData(@PathVariable Long id) {
        try {
            if (!energyDataRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            energyDataRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting energy data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ==================== EMISSION FACTOR ENDPOINTS ====================

    /**
     * Get all emission factors
     * 
     * @return List of all emission factors
     */
    @GetMapping("/emission-factors")
    public ResponseEntity<List<EmissionFactor>> getAllEmissionFactors() {
        try {
            List<EmissionFactor> emissionFactors = emissionFactorRepository.findAll();
            return ResponseEntity.ok(emissionFactors);
        } catch (Exception e) {
            log.error("Error getting emission factors: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get emission factors by energy source
     * 
     * @param energySource The energy source
     * @return List of emission factors
     */
    @GetMapping("/emission-factors/source/{energySource}")
    public ResponseEntity<List<EmissionFactor>> getEmissionFactorsByEnergySource(@PathVariable String energySource) {
        try {
            List<EmissionFactor> emissionFactors = emissionFactorRepository.findByEnergySourceOrderBySectorAscTechnologyTypeAsc(energySource);
            return ResponseEntity.ok(emissionFactors);
        } catch (Exception e) {
            log.error("Error getting emission factors by energy source: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get emission factors by sector
     * 
     * @param sector The sector
     * @return List of emission factors
     */
    @GetMapping("/emission-factors/sector/{sector}")
    public ResponseEntity<List<EmissionFactor>> getEmissionFactorsBySector(@PathVariable String sector) {
        try {
            List<EmissionFactor> emissionFactors = emissionFactorRepository.findBySectorOrderByEnergySourceAscTechnologyTypeAsc(sector);
            return ResponseEntity.ok(emissionFactors);
        } catch (Exception e) {
            log.error("Error getting emission factors by sector: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get emission factors by energy source and sector
     * 
     * @param energySource The energy source
     * @param sector The sector
     * @return List of emission factors
     */
    @GetMapping("/emission-factors/source/{energySource}/sector/{sector}")
    public ResponseEntity<List<EmissionFactor>> getEmissionFactorsByEnergySourceAndSector(
            @PathVariable String energySource,
            @PathVariable String sector) {
        
        try {
            List<EmissionFactor> emissionFactors = emissionFactorRepository.findByEnergySourceAndSectorOrderByTechnologyTypeAsc(energySource, sector);
            return ResponseEntity.ok(emissionFactors);
        } catch (Exception e) {
            log.error("Error getting emission factors by energy source and sector: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active emission factors
     * 
     * @return List of active emission factors
     */
    @GetMapping("/emission-factors/active")
    public ResponseEntity<List<EmissionFactor>> getActiveEmissionFactors() {
        try {
            List<EmissionFactor> emissionFactors = emissionFactorRepository.findByIsActiveTrueOrderByEnergySourceAscSectorAsc();
            return ResponseEntity.ok(emissionFactors);
        } catch (Exception e) {
            log.error("Error getting active emission factors: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get emission factors by valid year
     * 
     * @param validYear The valid year
     * @return List of emission factors
     */
    @GetMapping("/emission-factors/year/{validYear}")
    public ResponseEntity<List<EmissionFactor>> getEmissionFactorsByValidYear(@PathVariable Integer validYear) {
        try {
            List<EmissionFactor> emissionFactors = emissionFactorRepository.findByValidYearOrderByEnergySourceAscSectorAsc(validYear);
            return ResponseEntity.ok(emissionFactors);
        } catch (Exception e) {
            log.error("Error getting emission factors by valid year: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get emission factors within a year range
     * 
     * @param startYear Start year
     * @param endYear End year
     * @return List of emission factors
     */
    @GetMapping("/emission-factors/range")
    public ResponseEntity<List<EmissionFactor>> getEmissionFactorsByYearRange(
            @RequestParam Integer startYear,
            @RequestParam Integer endYear) {
        
        try {
            List<EmissionFactor> emissionFactors = emissionFactorRepository.findByValidYearBetweenOrderByValidYearAscEnergySourceAscSectorAsc(startYear, endYear);
            return ResponseEntity.ok(emissionFactors);
        } catch (Exception e) {
            log.error("Error getting emission factors by year range: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get distinct energy sources from emission factors
     * 
     * @return List of distinct energy sources
     */
    @GetMapping("/emission-factors/sources")
    public ResponseEntity<List<String>> getDistinctEnergySourcesFromEmissionFactors() {
        try {
            List<String> energySources = emissionFactorRepository.findDistinctEnergySources();
            return ResponseEntity.ok(energySources);
        } catch (Exception e) {
            log.error("Error getting distinct energy sources from emission factors: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get distinct sectors from emission factors
     * 
     * @return List of distinct sectors
     */
    @GetMapping("/emission-factors/sectors")
    public ResponseEntity<List<String>> getDistinctSectorsFromEmissionFactors() {
        try {
            List<String> sectors = emissionFactorRepository.findDistinctSectors();
            return ResponseEntity.ok(sectors);
        } catch (Exception e) {
            log.error("Error getting distinct sectors from emission factors: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get average CO2 factor by energy source
     * 
     * @return List of average CO2 factors
     */
    @GetMapping("/emission-factors/co2-averages")
    public ResponseEntity<List<Object[]>> getAverageCO2FactorByEnergySource() {
        try {
            List<Object[]> averages = emissionFactorRepository.calculateAverageCO2FactorByEnergySource();
            return ResponseEntity.ok(averages);
        } catch (Exception e) {
            log.error("Error getting average CO2 factors: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get average emission factors by sector
     * 
     * @return List of average emission factors
     */
    @GetMapping("/emission-factors/sector-averages")
    public ResponseEntity<List<Object[]>> getAverageEmissionFactorsBySector() {
        try {
            List<Object[]> averages = emissionFactorRepository.calculateAverageEmissionFactorsBySector();
            return ResponseEntity.ok(averages);
        } catch (Exception e) {
            log.error("Error getting average emission factors by sector: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create emission factor
     * 
     * @param emissionFactor The emission factor to create
     * @return The created emission factor
     */
    @PostMapping("/emission-factors")
    public ResponseEntity<EmissionFactor> createEmissionFactor(@RequestBody EmissionFactor emissionFactor) {
        try {
            EmissionFactor createdEmissionFactor = emissionFactorRepository.save(emissionFactor);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEmissionFactor);
        } catch (Exception e) {
            log.error("Error creating emission factor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Update emission factor
     * 
     * @param id The emission factor ID
     * @param emissionFactor The updated emission factor
     * @return The updated emission factor
     */
    @PutMapping("/emission-factors/{id}")
    public ResponseEntity<EmissionFactor> updateEmissionFactor(@PathVariable Long id, @RequestBody EmissionFactor emissionFactor) {
        try {
            Optional<EmissionFactor> existingEmissionFactor = emissionFactorRepository.findById(id);
            if (existingEmissionFactor.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            EmissionFactor existing = existingEmissionFactor.get();
            existing.setEnergySource(emissionFactor.getEnergySource());
            existing.setSector(emissionFactor.getSector());
            existing.setTechnologyType(emissionFactor.getTechnologyType());
            existing.setCo2Factor(emissionFactor.getCo2Factor());
            existing.setNoxFactor(emissionFactor.getNoxFactor());
            existing.setSo2Factor(emissionFactor.getSo2Factor());
            existing.setCh4Factor(emissionFactor.getCh4Factor());
            existing.setN2oFactor(emissionFactor.getN2oFactor());
            existing.setValidYear(emissionFactor.getValidYear());
            existing.setDataSource(emissionFactor.getDataSource());
            existing.setNotes(emissionFactor.getNotes());
            existing.setIsActive(emissionFactor.getIsActive());
            
            EmissionFactor updatedEmissionFactor = emissionFactorRepository.save(existing);
            return ResponseEntity.ok(updatedEmissionFactor);
        } catch (Exception e) {
            log.error("Error updating emission factor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Delete emission factor
     * 
     * @param id The emission factor ID
     * @return No content if successful
     */
    @DeleteMapping("/emission-factors/{id}")
    public ResponseEntity<Void> deleteEmissionFactor(@PathVariable Long id) {
        try {
            if (!emissionFactorRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            emissionFactorRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting emission factor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
} 