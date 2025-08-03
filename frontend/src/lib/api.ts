import axios, { AxiosInstance, AxiosResponse } from 'axios';
import {
  Scenario,
  ForecastResult,
  EmissionCalculation,
  EmissionSummary,
  EmissionComparison,
  ImportResult,
  BaselineForecastRequest,
  ScenarioForecastRequest,
  CombinedForecastRequest,
  CombinedForecastResponse,
  EnergyData,
} from '@/types';

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Request interceptor
    this.client.interceptors.request.use(
      (config) => {
        console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`);
        return config;
      },
      (error) => {
        console.error('API Request Error:', error);
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.client.interceptors.response.use(
      (response) => {
        console.log(`API Response: ${response.status} ${response.config.url}`);
        return response;
      },
      (error) => {
        console.error('API Response Error:', error.response?.data || error.message);
        return Promise.reject(error);
      }
    );
  }

  // Health Check
  async healthCheck(): Promise<any> {
    const response = await this.client.get('/actuator/health');
    return response.data;
  }

  // Scenario Management
  async getScenarios(): Promise<Scenario[]> {
    const response = await this.client.get('/api/energy/scenarios');
    return response.data;
  }

  async getScenario(id: number): Promise<Scenario> {
    const response = await this.client.get(`/api/energy/scenarios/${id}`);
    return response.data;
  }

  async createScenario(scenario: Omit<Scenario, 'id'>): Promise<Scenario> {
    const response = await this.client.post('/api/energy/scenarios', scenario);
    return response.data;
  }

  async updateScenario(id: number, scenario: Partial<Scenario>): Promise<Scenario> {
    const response = await this.client.put(`/api/energy/scenarios/${id}`, scenario);
    return response.data;
  }

  async deleteScenario(id: number): Promise<void> {
    await this.client.delete(`/api/energy/scenarios/${id}`);
  }

  async activateScenario(id: number): Promise<Scenario> {
    const response = await this.client.post(`/api/energy/scenarios/${id}/activate`);
    return response.data;
  }

  // Forecasting
  async baselineForecast(request: BaselineForecastRequest): Promise<ForecastResult[]> {
    const response = await this.client.post('/api/energy/forecast/baseline', request);
    return response.data;
  }

  async scenarioForecast(
    scenarioId: number,
    request: ScenarioForecastRequest
  ): Promise<ForecastResult[]> {
    const response = await this.client.post(
      `/api/energy/scenarios/${scenarioId}/forecast`,
      request
    );
    return response.data;
  }

  async combinedForecast(
    scenarioId: number,
    request: CombinedForecastRequest
  ): Promise<CombinedForecastResponse> {
    const response = await this.client.post(
      `/api/energy/scenarios/${scenarioId}/forecast-with-emissions`,
      request
    );
    return response.data;
  }

  // Emissions
  async getEmissionsForScenario(scenarioId: number): Promise<EmissionCalculation[]> {
    const response = await this.client.get(`/api/energy/scenarios/${scenarioId}/emissions`);
    return response.data;
  }

  async getEmissionsForYearRange(
    scenarioId: number,
    startYear: number,
    endYear: number
  ): Promise<EmissionCalculation[]> {
    const response = await this.client.get(
      `/api/energy/scenarios/${scenarioId}/emissions/range?startYear=${startYear}&endYear=${endYear}`
    );
    return response.data;
  }

  async getYearlyEmissions(scenarioId: number): Promise<Record<number, EmissionSummary>> {
    const response = await this.client.get(`/api/energy/scenarios/${scenarioId}/emissions/yearly`);
    return response.data;
  }

  async getSectorEmissions(scenarioId: number): Promise<Record<string, EmissionSummary>> {
    const response = await this.client.get(`/api/energy/scenarios/${scenarioId}/emissions/sector`);
    return response.data;
  }

  async getEnergySourceEmissions(scenarioId: number): Promise<Record<string, EmissionSummary>> {
    const response = await this.client.get(
      `/api/energy/scenarios/${scenarioId}/emissions/energy-source`
    );
    return response.data;
  }

  async compareScenarios(scenario1Id: number, scenario2Id: number): Promise<EmissionComparison> {
    const response = await this.client.get(
      `/api/energy/emissions/compare?scenario1Id=${scenario1Id}&scenario2Id=${scenario2Id}`
    );
    return response.data;
  }

  // Data Import
  async importExcelData(file: File, dataSource: string): Promise<ImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('dataSource', dataSource);

    const response = await this.client.post('/api/energy/import/excel', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  }

  // Energy Data
  async getEnergyData(): Promise<EnergyData[]> {
    const response = await this.client.get('/api/energy/data/energy');
    return response.data;
  }

  // Utility methods
  async isHealthy(): Promise<boolean> {
    try {
      await this.healthCheck();
      return true;
    } catch (error) {
      return false;
    }
  }

  // Error handling
  handleError(error: any): string {
    if (error.response?.data?.message) {
      return error.response.data.message;
    }
    if (error.message) {
      return error.message;
    }
    return 'An unexpected error occurred';
  }
}

// Create and export a singleton instance
export const apiClient = new ApiClient();

// Export types for convenience
export type { ApiClient }; 