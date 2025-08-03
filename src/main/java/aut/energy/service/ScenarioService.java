package aut.energy.service;

import aut.energy.entity.Scenario;
import aut.energy.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Scenario Service for managing energy planning scenarios
 * Handles CRUD operations and scenario-specific business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;

    /**
     * Create a new scenario
     */
    public Scenario createScenario(Scenario scenario) {
        log.info("Creating scenario: {}", scenario.getName());

        // Set creation timestamp
        scenario.setCreatedAt(LocalDateTime.now());
        scenario.setUpdatedAt(LocalDateTime.now());

        // Set default values if not provided
        if (scenario.getIsActive() == null) {
            scenario.setIsActive(false);
        }

        if (scenario.getIsBaseline() == null) {
            scenario.setIsBaseline(false);
        }

        Scenario savedScenario = scenarioRepository.save(scenario);
        log.info("Successfully created scenario with ID: {}", savedScenario.getId());

        return savedScenario;
    }

    /**
     * Create a baseline scenario
     */
    public Scenario createBaselineScenario(String name, Integer startYear, Integer endYear) {
        log.info("Creating baseline scenario: {} for years {} to {}", name, startYear, endYear);

        Scenario baselineScenario = Scenario.builder()
                .name(name)
                .description("Baseline scenario for energy planning")
                .startYear(startYear)
                .endYear(endYear)
                .isBaseline(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Deactivate other baseline scenarios
        deactivateOtherBaselineScenarios();

        Scenario savedScenario = scenarioRepository.save(baselineScenario);
        log.info("Successfully created baseline scenario with ID: {}", savedScenario.getId());

        return savedScenario;
    }

    /**
     * Get all scenarios
     */
    public List<Scenario> getAllScenarios() {
        log.debug("Retrieving all scenarios");
        return scenarioRepository.findAll();
    }

    /**
     * Get scenario by ID
     */
    public Optional<Scenario> getScenarioById(Long id) {
        log.debug("Retrieving scenario with ID: {}", id);
        return scenarioRepository.findById(id);
    }

    /**
     * Get baseline scenarios
     */
    public List<Scenario> getBaselineScenarios() {
        log.debug("Retrieving baseline scenarios");
        return scenarioRepository.findByIsBaselineTrue();
    }

    /**
     * Get active scenarios
     */
    public List<Scenario> getActiveScenarios() {
        log.debug("Retrieving active scenarios");
        return scenarioRepository.findByIsActiveTrue();
    }

    /**
     * Update scenario
     */
    public Scenario updateScenario(Long id, Scenario scenario) {
        log.info("Updating scenario with ID: {}", id);

        Optional<Scenario> existingScenarioOpt = scenarioRepository.findById(id);
        if (existingScenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found with ID: " + id);
        }

        Scenario existingScenario = existingScenarioOpt.get();

        // Update fields
        existingScenario.setName(scenario.getName());
        existingScenario.setDescription(scenario.getDescription());
        existingScenario.setStartYear(scenario.getStartYear());
        existingScenario.setEndYear(scenario.getEndYear());
        existingScenario.setSectorGrowthRates(scenario.getSectorGrowthRates());
        existingScenario.setEnergySourceAdjustments(scenario.getEnergySourceAdjustments());
        existingScenario.setYearlyFactors(scenario.getYearlyFactors());
        existingScenario.setUpdatedAt(LocalDateTime.now());

        // Handle baseline scenario activation
        if (Boolean.TRUE.equals(scenario.getIsBaseline())) {
            deactivateOtherBaselineScenarios();
            existingScenario.setIsBaseline(true);
        }

        // Handle active scenario activation
        if (Boolean.TRUE.equals(scenario.getIsActive())) {
            deactivateOtherActiveScenarios();
            existingScenario.setIsActive(true);
        }

        Scenario updatedScenario = scenarioRepository.save(existingScenario);
        log.info("Successfully updated scenario with ID: {}", updatedScenario.getId());

        return updatedScenario;
    }

    /**
     * Delete scenario
     */
    public void deleteScenario(Long id) {
        log.info("Deleting scenario with ID: {}", id);

        Optional<Scenario> scenarioOpt = scenarioRepository.findById(id);
        if (scenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found with ID: " + id);
        }

        Scenario scenario = scenarioOpt.get();

        // Prevent deletion of baseline scenarios
        if (Boolean.TRUE.equals(scenario.getIsBaseline())) {
            throw new IllegalStateException("Cannot delete baseline scenario: " + scenario.getName());
        }

        scenarioRepository.deleteById(id);
        log.info("Successfully deleted scenario with ID: {}", id);
    }

    /**
     * Activate a scenario
     */
    public Scenario activateScenario(Long id) {
        log.info("Activating scenario with ID: {}", id);

        Optional<Scenario> scenarioOpt = scenarioRepository.findById(id);
        if (scenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found with ID: " + id);
        }

        Scenario scenario = scenarioOpt.get();

        // Deactivate other active scenarios
        deactivateOtherActiveScenarios();

        // Activate this scenario
        scenario.setIsActive(true);
        scenario.setUpdatedAt(LocalDateTime.now());

        Scenario activatedScenario = scenarioRepository.save(scenario);
        log.info("Successfully activated scenario with ID: {}", activatedScenario.getId());

        return activatedScenario;
    }

    /**
     * Deactivate a scenario
     */
    public Scenario deactivateScenario(Long id) {
        log.info("Deactivating scenario with ID: {}", id);

        Optional<Scenario> scenarioOpt = scenarioRepository.findById(id);
        if (scenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found with ID: " + id);
        }

        Scenario scenario = scenarioOpt.get();

        // Prevent deactivation of baseline scenarios
        if (Boolean.TRUE.equals(scenario.getIsBaseline())) {
            throw new IllegalStateException("Cannot deactivate baseline scenario: " + scenario.getName());
        }

        scenario.setIsActive(false);
        scenario.setUpdatedAt(LocalDateTime.now());

        Scenario deactivatedScenario = scenarioRepository.save(scenario);
        log.info("Successfully deactivated scenario with ID: {}", deactivatedScenario.getId());

        return deactivatedScenario;
    }

    /**
     * Get scenarios by year range
     */
    public List<Scenario> getScenariosByYearRange(Integer startYear, Integer endYear) {
        log.debug("Retrieving scenarios for year range: {} to {}", startYear, endYear);
        return scenarioRepository.findByStartYearLessThanEqualAndEndYearGreaterThanEqual(startYear, endYear);
    }

    /**
     * Get scenarios by sector
     */
    public List<Scenario> getScenariosBySector(String sector) {
        log.debug("Retrieving scenarios for sector: {}", sector);
        return scenarioRepository.findBySectorGrowthRatesContaining(sector);
    }

    /**
     * Clone a scenario
     */
    public Scenario cloneScenario(Long id, String newName) {
        log.info("Cloning scenario with ID: {} to new scenario: {}", id, newName);

        Optional<Scenario> originalScenarioOpt = scenarioRepository.findById(id);
        if (originalScenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found with ID: " + id);
        }

        Scenario originalScenario = originalScenarioOpt.get();

        // Create new scenario with copied data
        Scenario clonedScenario = Scenario.builder()
                .name(newName)
                .description("Cloned from: " + originalScenario.getName())
                .startYear(originalScenario.getStartYear())
                .endYear(originalScenario.getEndYear())
                .sectorGrowthRates(originalScenario.getSectorGrowthRates())
                .energySourceAdjustments(originalScenario.getEnergySourceAdjustments())
                .yearlyFactors(originalScenario.getYearlyFactors())
                .isBaseline(false)
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Scenario savedScenario = scenarioRepository.save(clonedScenario);
        log.info("Successfully cloned scenario. New scenario ID: {}", savedScenario.getId());

        return savedScenario;
    }

    /**
     * Update sector growth rates for a scenario
     */
    public Scenario updateSectorGrowthRates(Long id, Map<String, Double> sectorGrowthRates) {
        log.info("Updating sector growth rates for scenario ID: {}", id);

        Optional<Scenario> scenarioOpt = scenarioRepository.findById(id);
        if (scenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found with ID: " + id);
        }

        Scenario scenario = scenarioOpt.get();
        scenario.setSectorGrowthRates(sectorGrowthRates);
        scenario.setUpdatedAt(LocalDateTime.now());

        Scenario updatedScenario = scenarioRepository.save(scenario);
        log.info("Successfully updated sector growth rates for scenario ID: {}", updatedScenario.getId());

        return updatedScenario;
    }

    /**
     * Update energy source adjustments for a scenario
     */
    public Scenario updateEnergySourceAdjustments(Long id, Map<String, Double> energySourceAdjustments) {
        log.info("Updating energy source adjustments for scenario ID: {}", id);

        Optional<Scenario> scenarioOpt = scenarioRepository.findById(id);
        if (scenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found with ID: " + id);
        }

        Scenario scenario = scenarioOpt.get();
        scenario.setEnergySourceAdjustments(energySourceAdjustments);
        scenario.setUpdatedAt(LocalDateTime.now());

        Scenario updatedScenario = scenarioRepository.save(scenario);
        log.info("Successfully updated energy source adjustments for scenario ID: {}", updatedScenario.getId());

        return updatedScenario;
    }

    /**
     * Update yearly factors for a scenario
     */
    public Scenario updateYearlyFactors(Long id, Map<Integer, Double> yearlyFactors) {
        log.info("Updating yearly factors for scenario ID: {}", id);

        Optional<Scenario> scenarioOpt = scenarioRepository.findById(id);
        if (scenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found with ID: " + id);
        }

        Scenario scenario = scenarioOpt.get();
        scenario.setYearlyFactors(yearlyFactors);
        scenario.setUpdatedAt(LocalDateTime.now());

        Scenario updatedScenario = scenarioRepository.save(scenario);
        log.info("Successfully updated yearly factors for scenario ID: {}", updatedScenario.getId());

        return updatedScenario;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Deactivate other baseline scenarios
     */
    private void deactivateOtherBaselineScenarios() {
        List<Scenario> baselineScenarios = scenarioRepository.findByIsBaselineTrue();
        for (Scenario scenario : baselineScenarios) {
            scenario.setIsBaseline(false);
            scenario.setUpdatedAt(LocalDateTime.now());
            scenarioRepository.save(scenario);
        }
    }

    /**
     * Deactivate other active scenarios
     */
    private void deactivateOtherActiveScenarios() {
        List<Scenario> activeScenarios = scenarioRepository.findByIsActiveTrue();
        for (Scenario scenario : activeScenarios) {
            scenario.setIsActive(false);
            scenario.setUpdatedAt(LocalDateTime.now());
            scenarioRepository.save(scenario);
        }
    }
} 