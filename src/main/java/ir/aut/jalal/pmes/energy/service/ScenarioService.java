package ir.aut.jalal.pmes.energy.service;

import ir.aut.jalal.pmes.energy.entity.Scenario;
import ir.aut.jalal.pmes.energy.repository.ScenarioRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing energy planning scenarios
 * Handles CRUD operations and scenario-specific business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;

    /**
     * Create a new scenario
     * 
     * @param scenario The scenario to create
     * @return The created scenario
     */
    @Transactional
    public Scenario createScenario(Scenario scenario) {
        log.info("Creating new scenario: {}", scenario.getName());
        
        // Validate scenario
        validateScenario(scenario);
        
        // Set default values if not provided
        if (scenario.getIsActive() == null) {
            scenario.setIsActive(false);
        }
        
        if (scenario.getCreatedAt() == null) {
            scenario.setCreatedAt(LocalDateTime.now());
        }
        
        Scenario savedScenario = scenarioRepository.save(scenario);
        log.info("Successfully created scenario with ID: {}", savedScenario.getId());
        
        return savedScenario;
    }

    /**
     * Update an existing scenario
     * 
     * @param id The scenario ID
     * @param scenario The updated scenario data
     * @return The updated scenario
     */
    @Transactional
    public Scenario updateScenario(Long id, Scenario scenario) {
        log.info("Updating scenario with ID: {}", id);
        
        Optional<Scenario> existingScenario = scenarioRepository.findById(id);
        if (existingScenario.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found with ID: " + id);
        }
        
        Scenario existing = existingScenario.get();
        
        // Update fields
        existing.setName(scenario.getName());
        existing.setDescription(scenario.getDescription());
        existing.setScenarioType(scenario.getScenarioType());
        existing.setStartYear(scenario.getStartYear());
        existing.setEndYear(scenario.getEndYear());
        existing.setGdpGrowthRate(scenario.getGdpGrowthRate());
        existing.setPopulationGrowthRate(scenario.getPopulationGrowthRate());
        existing.setEfficiencyImprovementRate(scenario.getEfficiencyImprovementRate());
        existing.setRenewableTarget(scenario.getRenewableTarget());
        existing.setCarbonPrice(scenario.getCarbonPrice());
        existing.setIsActive(scenario.getIsActive());
        existing.setCreatedBy(scenario.getCreatedBy());
        
        // Validate updated scenario
        validateScenario(existing);
        
        Scenario updatedScenario = scenarioRepository.save(existing);
        log.info("Successfully updated scenario with ID: {}", updatedScenario.getId());
        
        return updatedScenario;
    }

    /**
     * Get a scenario by ID
     * 
     * @param id The scenario ID
     * @return The scenario if found
     */
    public Optional<Scenario> getScenarioById(Long id) {
        return scenarioRepository.findById(id);
    }

    /**
     * Get all scenarios
     * 
     * @return List of all scenarios
     */
    public List<Scenario> getAllScenarios() {
        return scenarioRepository.findAll();
    }

    /**
     * Get scenarios by type
     * 
     * @param scenarioType The scenario type
     * @return List of scenarios of the specified type
     */
    public List<Scenario> getScenariosByType(Scenario.ScenarioType scenarioType) {
        return scenarioRepository.findByScenarioTypeOrderByNameAsc(scenarioType);
    }

    /**
     * Get active scenarios
     * 
     * @return List of active scenarios
     */
    public List<Scenario> getActiveScenarios() {
        return scenarioRepository.findByIsActiveTrueOrderByNameAsc();
    }

    /**
     * Search scenarios by name
     * 
     * @param name The name to search for
     * @return List of matching scenarios
     */
    public List<Scenario> searchScenariosByName(String name) {
        return scenarioRepository.findByNameContainingIgnoreCaseOrderByNameAsc(name);
    }

    /**
     * Search scenarios by description
     * 
     * @param description The description to search for
     * @return List of matching scenarios
     */
    public List<Scenario> searchScenariosByDescription(String description) {
        return scenarioRepository.findByDescriptionContainingIgnoreCaseOrderByNameAsc(description);
    }

    /**
     * Get scenarios within a year range
     * 
     * @param startYear Start year
     * @param endYear End year
     * @return List of scenarios overlapping the year range
     */
    public List<Scenario> getScenariosInYearRange(Integer startYear, Integer endYear) {
        return scenarioRepository.findScenariosOverlappingYearRange(startYear, endYear);
    }

    /**
     * Get baseline scenarios
     * 
     * @return List of baseline scenarios
     */
    public List<Scenario> getBaselineScenarios() {
        return scenarioRepository.findBaselineScenarios();
    }

    /**
     * Get scenarios created by a specific user
     * 
     * @param createdBy The creator's name
     * @return List of scenarios created by the user
     */
    public List<Scenario> getScenariosByCreator(String createdBy) {
        return scenarioRepository.findByCreatedByOrderByNameAsc(createdBy);
    }

    /**
     * Get the currently active scenario
     * 
     * @return The active scenario if any
     */
    public Optional<Scenario> getActiveScenario() {
        return scenarioRepository.findByIsActiveTrue();
    }

    /**
     * Activate a scenario (deactivates all others)
     * 
     * @param scenarioId The ID of the scenario to activate
     * @return The activated scenario
     */
    @Transactional
    public Scenario activateScenario(Long scenarioId) {
        log.info("Activating scenario with ID: {}", scenarioId);
        
        Optional<Scenario> scenarioOpt = scenarioRepository.findById(scenarioId);
        if (scenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found with ID: " + scenarioId);
        }
        
        // Deactivate all scenarios
        List<Scenario> activeScenarios = scenarioRepository.findByIsActiveTrueOrderByNameAsc();
        for (Scenario active : activeScenarios) {
            active.setIsActive(false);
            scenarioRepository.save(active);
        }
        
        // Activate the specified scenario
        Scenario scenario = scenarioOpt.get();
        scenario.setIsActive(true);
        Scenario activatedScenario = scenarioRepository.save(scenario);
        
        log.info("Successfully activated scenario: {}", activatedScenario.getName());
        return activatedScenario;
    }

    /**
     * Deactivate all scenarios
     */
    @Transactional
    public void deactivateAllScenarios() {
        log.info("Deactivating all scenarios");
        
        List<Scenario> activeScenarios = scenarioRepository.findByIsActiveTrueOrderByNameAsc();
        for (Scenario scenario : activeScenarios) {
            scenario.setIsActive(false);
            scenarioRepository.save(scenario);
        }
        
        log.info("Successfully deactivated {} scenarios", activeScenarios.size());
    }

    /**
     * Delete a scenario
     * 
     * @param id The scenario ID to delete
     */
    @Transactional
    public void deleteScenario(Long id) {
        log.info("Deleting scenario with ID: {}", id);
        
        Optional<Scenario> scenarioOpt = scenarioRepository.findById(id);
        if (scenarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Scenario not found with ID: " + id);
        }
        
        Scenario scenario = scenarioOpt.get();
        
        // Don't allow deletion of active scenarios
        if (scenario.getIsActive()) {
            throw new IllegalStateException("Cannot delete active scenario: " + scenario.getName());
        }
        
        scenarioRepository.deleteById(id);
        log.info("Successfully deleted scenario: {}", scenario.getName());
    }

    /**
     * Create a baseline scenario
     * 
     * @param name The scenario name
     * @param startYear The start year
     * @param endYear The end year
     * @return The created baseline scenario
     */
    @Transactional
    public Scenario createBaselineScenario(String name, Integer startYear, Integer endYear) {
        log.info("Creating baseline scenario: {}", name);
        
        Scenario baseline = Scenario.builder()
                .name(name)
                .description("Baseline scenario with no policy changes")
                .scenarioType(Scenario.ScenarioType.BASELINE)
                .startYear(startYear)
                .endYear(endYear)
                .gdpGrowthRate(BigDecimal.valueOf(2.0)) // Default 2% GDP growth
                .populationGrowthRate(BigDecimal.valueOf(1.0)) // Default 1% population growth
                .efficiencyImprovementRate(BigDecimal.valueOf(0.5)) // Default 0.5% efficiency improvement
                .renewableTarget(BigDecimal.valueOf(20.0)) // Default 20% renewable target
                .carbonPrice(BigDecimal.valueOf(30.0)) // Default $30/ton CO2
                .isActive(false)
                .createdBy("System")
                .build();
        
        return createScenario(baseline);
    }

    /**
     * Create a renewables boost scenario
     * 
     * @param name The scenario name
     * @param startYear The start year
     * @param endYear The end year
     * @param renewableTarget The renewable energy target percentage
     * @return The created renewables boost scenario
     */
    @Transactional
    public Scenario createRenewablesBoostScenario(String name, Integer startYear, Integer endYear, BigDecimal renewableTarget) {
        log.info("Creating renewables boost scenario: {}", name);
        
        Scenario renewablesScenario = Scenario.builder()
                .name(name)
                .description("Scenario with increased renewable energy deployment")
                .scenarioType(Scenario.ScenarioType.POLICY_CHANGE)
                .startYear(startYear)
                .endYear(endYear)
                .gdpGrowthRate(BigDecimal.valueOf(2.5)) // Slightly higher GDP growth
                .populationGrowthRate(BigDecimal.valueOf(1.0))
                .efficiencyImprovementRate(BigDecimal.valueOf(1.0)) // Higher efficiency
                .renewableTarget(renewableTarget)
                .carbonPrice(BigDecimal.valueOf(50.0)) // Higher carbon price
                .isActive(false)
                .createdBy("System")
                .build();
        
        return createScenario(renewablesScenario);
    }

    /**
     * Create a coal phaseout scenario
     * 
     * @param name The scenario name
     * @param startYear The start year
     * @param endYear The end year
     * @return The created coal phaseout scenario
     */
    @Transactional
    public Scenario createCoalPhaseoutScenario(String name, Integer startYear, Integer endYear) {
        log.info("Creating coal phaseout scenario: {}", name);
        
        Scenario coalPhaseout = Scenario.builder()
                .name(name)
                .description("Scenario with gradual coal phaseout")
                .scenarioType(Scenario.ScenarioType.CLIMATE_ACTION)
                .startYear(startYear)
                .endYear(endYear)
                .gdpGrowthRate(BigDecimal.valueOf(2.0))
                .populationGrowthRate(BigDecimal.valueOf(1.0))
                .efficiencyImprovementRate(BigDecimal.valueOf(1.5)) // Higher efficiency
                .renewableTarget(BigDecimal.valueOf(40.0)) // Higher renewable target
                .carbonPrice(BigDecimal.valueOf(80.0)) // Much higher carbon price
                .isActive(false)
                .createdBy("System")
                .build();
        
        return createScenario(coalPhaseout);
    }

    /**
     * Validate scenario data
     * 
     * @param scenario The scenario to validate
     */
    private void validateScenario(Scenario scenario) {
        if (scenario.getName() == null || scenario.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Scenario name is required");
        }
        
        if (scenario.getStartYear() == null || scenario.getEndYear() == null) {
            throw new IllegalArgumentException("Start year and end year are required");
        }
        
        if (scenario.getStartYear() >= scenario.getEndYear()) {
            throw new IllegalArgumentException("Start year must be before end year");
        }
        
        if (scenario.getStartYear() < 2020 || scenario.getEndYear() > 2050) {
            throw new IllegalArgumentException("Year range must be between 2020 and 2050");
        }
        
        // Validate growth rates
        if (scenario.getGdpGrowthRate() != null && 
            (scenario.getGdpGrowthRate().compareTo(BigDecimal.ZERO) < 0 || 
             scenario.getGdpGrowthRate().compareTo(BigDecimal.valueOf(10)) > 0)) {
            throw new IllegalArgumentException("GDP growth rate must be between 0% and 10%");
        }
        
        if (scenario.getPopulationGrowthRate() != null && 
            (scenario.getPopulationGrowthRate().compareTo(BigDecimal.valueOf(-2)) < 0 || 
             scenario.getPopulationGrowthRate().compareTo(BigDecimal.valueOf(5)) > 0)) {
            throw new IllegalArgumentException("Population growth rate must be between -2% and 5%");
        }
        
        if (scenario.getEfficiencyImprovementRate() != null && 
            (scenario.getEfficiencyImprovementRate().compareTo(BigDecimal.ZERO) < 0 || 
             scenario.getEfficiencyImprovementRate().compareTo(BigDecimal.valueOf(5)) > 0)) {
            throw new IllegalArgumentException("Efficiency improvement rate must be between 0% and 5%");
        }
        
        if (scenario.getRenewableTarget() != null && 
            (scenario.getRenewableTarget().compareTo(BigDecimal.ZERO) < 0 || 
             scenario.getRenewableTarget().compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("Renewable target must be between 0% and 100%");
        }
        
        if (scenario.getCarbonPrice() != null && 
            scenario.getCarbonPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Carbon price must be non-negative");
        }
        
        // Check for duplicate names
        if (scenario.getId() == null) { // New scenario
            if (scenarioRepository.existsByName(scenario.getName())) {
                throw new IllegalArgumentException("Scenario name already exists: " + scenario.getName());
            }
        } else { // Existing scenario
            Optional<Scenario> existing = scenarioRepository.findById(scenario.getId());
            if (existing.isPresent() && !existing.get().getName().equals(scenario.getName())) {
                if (scenarioRepository.existsByName(scenario.getName())) {
                    throw new IllegalArgumentException("Scenario name already exists: " + scenario.getName());
                }
            }
        }
    }
} 