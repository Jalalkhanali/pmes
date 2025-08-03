// API Response Types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

// Energy Data Types
export interface EnergyData {
  id?: number;
  year: number;
  sector: string;
  energySource: string;
  consumptionTwh: number;
  dataSource?: string;
  importedAt?: string;
}

// Scenario Types
export interface Scenario {
  id?: number;
  name: string;
  description?: string;
  startYear: number;
  endYear: number;
  sectorGrowthRates?: Record<string, number>;
  energySourceAdjustments?: Record<string, number>;
  yearlyFactors?: Record<number, number>;
  isBaseline?: boolean;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// Forecast Types
export interface ForecastResult {
  id?: number;
  scenario: Scenario;
  sector: string;
  energySource: string;
  year: number;
  forecastedConsumptionTwh: number;
  confidenceLevel: number;
  createdAt?: string;
}

// Emission Types
export interface EmissionCalculation {
  year: number;
  sector: string;
  energySource: string;
  energyConsumptionTwh: number;
  emissionsKgCo2: number;
  emissionFactor: number;
}

export interface EmissionSummary {
  totalEmissions: number;
  totalEnergyConsumption: number;
  averageEmissionFactor: number;
  calculationCount: number;
}

export interface EmissionComparison {
  scenario1Id: number;
  scenario2Id: number;
  scenario1Emissions: EmissionSummary;
  scenario2Emissions: EmissionSummary;
  totalDifference: number;
  percentageDifference: number;
}

// Import Types
export interface ImportResult {
  totalRows: number;
  importedRows: number;
  errors: string[];
  dataSource: string;
  importedAt: string;
}

// Forecasting Request Types
export interface BaselineForecastRequest {
  sectors: string[];
  energySources: string[];
  forecastYears: number;
  startYear: number;
  endYear: number;
}

export interface ScenarioForecastRequest {
  sectors: string[];
  energySources: string[];
  forecastYears: number;
}

export interface CombinedForecastRequest {
  sectors: string[];
  energySources: string[];
  forecastYears: number;
}

export interface CombinedForecastResponse {
  scenario: Scenario;
  forecastResults: ForecastResult[];
  emissionCalculations: EmissionCalculation[];
}

// Chart Data Types
export interface ChartDataPoint {
  name: string;
  value: number;
  [key: string]: any;
}

export interface TimeSeriesData {
  year: number;
  [key: string]: number;
}

// Form Types
export interface ScenarioFormData {
  name: string;
  description: string;
  startYear: number;
  endYear: number;
  sectorGrowthRates: Record<string, number>;
  energySourceAdjustments: Record<string, number>;
  yearlyFactors: Record<number, number>;
}

// Navigation Types
export interface NavItem {
  name: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  current?: boolean;
}

// Table Types
export interface TableColumn<T> {
  header: string;
  accessorKey: keyof T;
  cell?: (value: any, row: T) => React.ReactNode;
  sortable?: boolean;
}

// Filter Types
export interface FilterOption {
  label: string;
  value: string;
}

export interface FilterState {
  sectors: string[];
  energySources: string[];
  years: [number, number];
  scenarios: number[];
}

// Dashboard Types
export interface DashboardStats {
  totalScenarios: number;
  totalForecasts: number;
  totalEmissions: number;
  activeScenario?: Scenario;
}

// API Error Types
export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
  path: string;
}

// Constants
export const SECTORS = [
  'Industrial',
  'Residential',
  'Commercial',
  'Transportation',
  'Agriculture'
] as const;

export const ENERGY_SOURCES = [
  'Electricity',
  'Natural Gas',
  'Oil',
  'Coal',
  'Renewables'
] as const;

export type Sector = typeof SECTORS[number];
export type EnergySource = typeof ENERGY_SOURCES[number]; 