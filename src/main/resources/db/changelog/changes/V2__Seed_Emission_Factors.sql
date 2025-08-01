-- Seed Emission Factors Data
-- This script inserts sample emission factors for different energy sources and sectors

-- Coal emission factors (high emissions)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
('COAL', 'INDUSTRIAL', 'CONVENTIONAL', 820.0, 2.5, 3.2, 0.1, 0.02, 2023, 'IPCC Guidelines', 'Conventional coal-fired power plants', true),
('COAL', 'RESIDENTIAL', 'HEATING', 950.0, 3.0, 4.0, 0.15, 0.03, 2023, 'IPCC Guidelines', 'Residential coal heating', true),
('COAL', 'COMMERCIAL', 'HEATING', 900.0, 2.8, 3.5, 0.12, 0.025, 2023, 'IPCC Guidelines', 'Commercial coal heating', true),
('COAL', 'TRANSPORT', 'STEAM_LOCOMOTIVE', 850.0, 2.7, 3.8, 0.13, 0.028, 2023, 'IPCC Guidelines', 'Steam locomotive (historical)', true);

-- Natural Gas emission factors (medium emissions)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
('GAS', 'INDUSTRIAL', 'COMBINED_CYCLE', 490.0, 0.8, 0.1, 0.05, 0.01, 2023, 'IPCC Guidelines', 'Combined cycle gas turbines', true),
('GAS', 'RESIDENTIAL', 'HEATING', 520.0, 1.2, 0.15, 0.08, 0.015, 2023, 'IPCC Guidelines', 'Residential gas heating', true),
('GAS', 'COMMERCIAL', 'HEATING', 500.0, 1.0, 0.12, 0.06, 0.012, 2023, 'IPCC Guidelines', 'Commercial gas heating', true),
('GAS', 'TRANSPORT', 'CNG_VEHICLE', 480.0, 0.9, 0.08, 0.04, 0.008, 2023, 'IPCC Guidelines', 'Compressed natural gas vehicles', true);

-- Oil/Petroleum emission factors (medium-high emissions)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
('OIL', 'INDUSTRIAL', 'CONVENTIONAL', 650.0, 1.8, 2.5, 0.08, 0.015, 2023, 'IPCC Guidelines', 'Conventional oil-fired power plants', true),
('OIL', 'RESIDENTIAL', 'HEATING', 680.0, 2.0, 2.8, 0.1, 0.018, 2023, 'IPCC Guidelines', 'Residential oil heating', true),
('OIL', 'COMMERCIAL', 'HEATING', 660.0, 1.9, 2.6, 0.09, 0.016, 2023, 'IPCC Guidelines', 'Commercial oil heating', true),
('OIL', 'TRANSPORT', 'DIESEL_ENGINE', 640.0, 1.7, 2.2, 0.07, 0.014, 2023, 'IPCC Guidelines', 'Diesel engine vehicles', true);

-- Nuclear emission factors (very low emissions)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
('NUCLEAR', 'INDUSTRIAL', 'PRESSURIZED_WATER', 12.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Pressurized water reactors', true),
('NUCLEAR', 'RESIDENTIAL', 'ELECTRICITY', 15.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Nuclear electricity for residential use', true),
('NUCLEAR', 'COMMERCIAL', 'ELECTRICITY', 14.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Nuclear electricity for commercial use', true);

-- Hydroelectric emission factors (very low emissions)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
('HYDRO', 'INDUSTRIAL', 'LARGE_DAM', 4.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Large hydroelectric dams', true),
('HYDRO', 'RESIDENTIAL', 'ELECTRICITY', 5.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Hydroelectricity for residential use', true),
('HYDRO', 'COMMERCIAL', 'ELECTRICITY', 4.5, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Hydroelectricity for commercial use', true);

-- Solar emission factors (very low emissions)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
('SOLAR', 'INDUSTRIAL', 'PHOTOVOLTAIC', 45.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Industrial solar photovoltaic', true),
('SOLAR', 'RESIDENTIAL', 'PHOTOVOLTAIC', 50.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Residential solar photovoltaic', true),
('SOLAR', 'COMMERCIAL', 'PHOTOVOLTAIC', 48.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Commercial solar photovoltaic', true),
('SOLAR', 'INDUSTRIAL', 'CONCENTRATED', 25.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Concentrated solar power', true);

-- Wind emission factors (very low emissions)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
('WIND', 'INDUSTRIAL', 'ONSHORE', 11.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Onshore wind turbines', true),
('WIND', 'RESIDENTIAL', 'ELECTRICITY', 12.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Wind electricity for residential use', true),
('WIND', 'COMMERCIAL', 'ELECTRICITY', 11.5, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Wind electricity for commercial use', true),
('WIND', 'INDUSTRIAL', 'OFFSHORE', 12.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Offshore wind turbines', true);

-- Biomass emission factors (low-medium emissions, but renewable)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
('BIOMASS', 'INDUSTRIAL', 'COMBUSTION', 230.0, 1.5, 0.8, 0.2, 0.05, 2023, 'IPCC Guidelines', 'Biomass combustion for industrial use', true),
('BIOMASS', 'RESIDENTIAL', 'HEATING', 250.0, 1.8, 1.0, 0.25, 0.06, 2023, 'IPCC Guidelines', 'Residential biomass heating', true),
('BIOMASS', 'COMMERCIAL', 'HEATING', 240.0, 1.6, 0.9, 0.22, 0.055, 2023, 'IPCC Guidelines', 'Commercial biomass heating', true);

-- Geothermal emission factors (very low emissions)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
('GEOTHERMAL', 'INDUSTRIAL', 'DRY_STEAM', 38.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Dry steam geothermal power', true),
('GEOTHERMAL', 'RESIDENTIAL', 'ELECTRICITY', 40.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Geothermal electricity for residential use', true),
('GEOTHERMAL', 'COMMERCIAL', 'ELECTRICITY', 39.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Geothermal electricity for commercial use', true);

-- Hydrogen emission factors (very low emissions when produced from renewables)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
('HYDROGEN', 'INDUSTRIAL', 'GREEN_HYDROGEN', 15.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Green hydrogen from renewable sources', true),
('HYDROGEN', 'TRANSPORT', 'FUEL_CELL', 18.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Hydrogen fuel cell vehicles', true),
('HYDROGEN', 'RESIDENTIAL', 'HEATING', 20.0, 0.0, 0.0, 0.0, 0.0, 2023, 'IPCC Guidelines', 'Residential hydrogen heating', true);

-- Waste-to-Energy emission factors (medium emissions but waste management benefit)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
('WASTE', 'INDUSTRIAL', 'INCINERATION', 400.0, 2.0, 1.5, 0.1, 0.02, 2023, 'IPCC Guidelines', 'Waste-to-energy incineration', true),
('WASTE', 'RESIDENTIAL', 'ELECTRICITY', 420.0, 2.2, 1.6, 0.12, 0.025, 2023, 'IPCC Guidelines', 'Waste-to-energy for residential electricity', true),
('WASTE', 'COMMERCIAL', 'ELECTRICITY', 410.0, 2.1, 1.55, 0.11, 0.022, 2023, 'IPCC Guidelines', 'Waste-to-energy for commercial electricity', true);

-- Add some future emission factors for 2030 (improved technologies)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
-- Improved coal technologies
('COAL', 'INDUSTRIAL', 'SUPERCRITICAL', 750.0, 1.8, 2.0, 0.08, 0.015, 2030, 'IPCC Guidelines', 'Supercritical coal technology', true),
('COAL', 'INDUSTRIAL', 'IGCC', 680.0, 1.5, 1.2, 0.06, 0.012, 2030, 'IPCC Guidelines', 'Integrated Gasification Combined Cycle', true),

-- Improved gas technologies
('GAS', 'INDUSTRIAL', 'ADVANCED_CCGT', 420.0, 0.6, 0.08, 0.04, 0.008, 2030, 'IPCC Guidelines', 'Advanced combined cycle gas turbines', true),
('GAS', 'TRANSPORT', 'ADVANCED_CNG', 450.0, 0.7, 0.06, 0.03, 0.006, 2030, 'IPCC Guidelines', 'Advanced CNG vehicles', true),

-- Improved renewable technologies
('SOLAR', 'INDUSTRIAL', 'ADVANCED_PV', 35.0, 0.0, 0.0, 0.0, 0.0, 2030, 'IPCC Guidelines', 'Advanced photovoltaic technology', true),
('WIND', 'INDUSTRIAL', 'ADVANCED_OFFSHORE', 8.0, 0.0, 0.0, 0.0, 0.0, 2030, 'IPCC Guidelines', 'Advanced offshore wind technology', true),

-- Carbon capture and storage (CCS) technologies
('COAL', 'INDUSTRIAL', 'CCS', 120.0, 1.2, 1.5, 0.05, 0.01, 2030, 'IPCC Guidelines', 'Coal with carbon capture and storage', true),
('GAS', 'INDUSTRIAL', 'CCS', 80.0, 0.4, 0.05, 0.02, 0.004, 2030, 'IPCC Guidelines', 'Gas with carbon capture and storage', true);

-- Add some sector-specific emission factors for 2040 (further improvements)
INSERT INTO emission_factors (energy_source, sector, technology_type, co2_factor, nox_factor, so2_factor, ch4_factor, n2o_factor, valid_year, data_source, notes, is_active) VALUES
-- Ultra-low emission technologies
('SOLAR', 'INDUSTRIAL', 'ULTRA_EFFICIENT_PV', 25.0, 0.0, 0.0, 0.0, 0.0, 2040, 'IPCC Guidelines', 'Ultra-efficient photovoltaic technology', true),
('WIND', 'INDUSTRIAL', 'ULTRA_EFFICIENT_OFFSHORE', 5.0, 0.0, 0.0, 0.0, 0.0, 2040, 'IPCC Guidelines', 'Ultra-efficient offshore wind technology', true),
('HYDROGEN', 'TRANSPORT', 'ADVANCED_FUEL_CELL', 10.0, 0.0, 0.0, 0.0, 0.0, 2040, 'IPCC Guidelines', 'Advanced hydrogen fuel cell technology', true),

-- Negative emission technologies
('BIOMASS', 'INDUSTRIAL', 'BECCS', -200.0, 1.0, 0.5, 0.1, 0.02, 2040, 'IPCC Guidelines', 'Bioenergy with carbon capture and storage', true),
('DIRECT_AIR_CAPTURE', 'INDUSTRIAL', 'DAC', -800.0, 0.0, 0.0, 0.0, 0.0, 2040, 'IPCC Guidelines', 'Direct air capture technology', true); 