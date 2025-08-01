-- Energy Planning System Database Schema
-- This script creates all tables for the energy planning system

-- Energy Data Table
CREATE TABLE energy_data (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    sector VARCHAR(50) NOT NULL,
    energy_source VARCHAR(50) NOT NULL,
    consumption_twh DECIMAL(15,3) NOT NULL,
    gdp_billions DECIMAL(15,3),
    population_millions DECIMAL(10,2),
    avg_temperature_celsius DECIMAL(5,2),
    notes TEXT,
    data_source VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Scenarios Table
CREATE TABLE scenarios (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    scenario_type VARCHAR(50) NOT NULL,
    start_year INTEGER NOT NULL,
    end_year INTEGER NOT NULL,
    gdp_growth_rate DECIMAL(5,2),
    population_growth_rate DECIMAL(5,2),
    efficiency_improvement_rate DECIMAL(5,2),
    renewable_target DECIMAL(5,2),
    carbon_price DECIMAL(10,2),
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Emission Factors Table
CREATE TABLE emission_factors (
    id BIGSERIAL PRIMARY KEY,
    energy_source VARCHAR(50) NOT NULL,
    sector VARCHAR(50) NOT NULL,
    technology_type VARCHAR(100),
    co2_factor DECIMAL(15,3) NOT NULL,
    nox_factor DECIMAL(15,3),
    so2_factor DECIMAL(15,3),
    ch4_factor DECIMAL(15,3),
    n2o_factor DECIMAL(15,3),
    valid_year INTEGER NOT NULL,
    data_source VARCHAR(200),
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Forecast Results Table
CREATE TABLE forecast_results (
    id BIGSERIAL PRIMARY KEY,
    scenario_id BIGINT NOT NULL REFERENCES scenarios(id),
    forecast_year INTEGER NOT NULL,
    sector VARCHAR(50) NOT NULL,
    energy_source VARCHAR(50) NOT NULL,
    forecasted_consumption_twh DECIMAL(15,3) NOT NULL,
    lower_bound_twh DECIMAL(15,3),
    upper_bound_twh DECIMAL(15,3),
    confidence_level DECIMAL(3,2),
    model_accuracy DECIMAL(10,4),
    nn_architecture VARCHAR(500),
    pso_parameters TEXT,
    is_baseline BOOLEAN NOT NULL DEFAULT FALSE,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create indexes for better performance
CREATE INDEX idx_energy_data_year ON energy_data(year);
CREATE INDEX idx_energy_data_sector ON energy_data(sector);
CREATE INDEX idx_energy_data_energy_source ON energy_data(energy_source);
CREATE INDEX idx_energy_data_year_sector ON energy_data(year, sector);
CREATE INDEX idx_energy_data_year_source ON energy_data(year, energy_source);

CREATE INDEX idx_scenarios_name ON scenarios(name);
CREATE INDEX idx_scenarios_type ON scenarios(scenario_type);
CREATE INDEX idx_scenarios_active ON scenarios(is_active);
CREATE INDEX idx_scenarios_year_range ON scenarios(start_year, end_year);

CREATE INDEX idx_emission_factors_source ON emission_factors(energy_source);
CREATE INDEX idx_emission_factors_sector ON emission_factors(sector);
CREATE INDEX idx_emission_factors_active ON emission_factors(is_active);
CREATE INDEX idx_emission_factors_valid_year ON emission_factors(valid_year);

CREATE INDEX idx_forecast_results_scenario ON forecast_results(scenario_id);
CREATE INDEX idx_forecast_results_year ON forecast_results(forecast_year);
CREATE INDEX idx_forecast_results_sector ON forecast_results(sector);
CREATE INDEX idx_forecast_results_energy_source ON forecast_results(energy_source);
CREATE INDEX idx_forecast_results_baseline ON forecast_results(is_baseline);

-- Add constraints
ALTER TABLE energy_data ADD CONSTRAINT chk_year_range CHECK (year >= 1900 AND year <= 2100);
ALTER TABLE energy_data ADD CONSTRAINT chk_consumption_positive CHECK (consumption_twh > 0);

ALTER TABLE scenarios ADD CONSTRAINT chk_year_range CHECK (start_year >= 2020 AND end_year <= 2050);
ALTER TABLE scenarios ADD CONSTRAINT chk_start_before_end CHECK (start_year < end_year);

ALTER TABLE emission_factors ADD CONSTRAINT chk_co2_factor_positive CHECK (co2_factor >= 0);
ALTER TABLE emission_factors ADD CONSTRAINT chk_valid_year_range CHECK (valid_year >= 2000 AND valid_year <= 2050);

ALTER TABLE forecast_results ADD CONSTRAINT chk_forecast_year_range CHECK (forecast_year >= 2020 AND forecast_year <= 2050);
ALTER TABLE forecast_results ADD CONSTRAINT chk_consumption_positive CHECK (forecasted_consumption_twh > 0); 