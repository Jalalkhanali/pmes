'use client';

import { useQuery } from 'react-query';
import { apiClient } from '@/lib/api';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { formatNumber } from '@/lib/utils';

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

export default function DashboardChart() {
  const { data: scenarios } = useQuery('scenarios', () => apiClient.getScenarios());

  // Sample data for demonstration
  const energyData = [
    { year: 2020, Industrial: 150.5, Residential: 85.2, Commercial: 65.4 },
    { year: 2021, Industrial: 155.2, Residential: 87.6, Commercial: 67.8 },
    { year: 2022, Industrial: 160.8, Residential: 90.2, Commercial: 70.3 },
    { year: 2023, Industrial: 165.5, Residential: 92.8, Commercial: 72.9 },
    { year: 2024, Industrial: 170.2, Residential: 95.4, Commercial: 75.5 },
  ];

  const sectorData = [
    { name: 'Industrial', value: 170.2 },
    { name: 'Residential', value: 95.4 },
    { name: 'Commercial', value: 75.5 },
    { name: 'Transportation', value: 184.7 },
    { name: 'Agriculture', value: 27.5 },
  ];

  const energySourceData = [
    { name: 'Electricity', value: 45.2 },
    { name: 'Natural Gas', value: 35.8 },
    { name: 'Oil', value: 25.4 },
    { name: 'Coal', value: 15.6 },
    { name: 'Renewables', value: 8.0 },
  ];

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Energy Consumption Trend */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Energy Consumption Trend</h3>
          <p className="card-description">Historical energy consumption by sector</p>
        </div>
        <div className="card-content">
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={energyData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="year" />
              <YAxis />
              <Tooltip
                formatter={(value: number) => [formatNumber(value), 'TWh']}
                labelFormatter={(label) => `Year: ${label}`}
              />
              <Line
                type="monotone"
                dataKey="Industrial"
                stroke="#3b82f6"
                strokeWidth={2}
                name="Industrial"
              />
              <Line
                type="monotone"
                dataKey="Residential"
                stroke="#10b981"
                strokeWidth={2}
                name="Residential"
              />
              <Line
                type="monotone"
                dataKey="Commercial"
                stroke="#f59e0b"
                strokeWidth={2}
                name="Commercial"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Sector Distribution */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Sector Distribution</h3>
          <p className="card-description">Current energy consumption by sector</p>
        </div>
        <div className="card-content">
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={sectorData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {sectorData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip
                formatter={(value: number) => [formatNumber(value), 'TWh']}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Energy Sources */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Energy Sources</h3>
          <p className="card-description">Energy consumption by source</p>
        </div>
        <div className="card-content">
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={energySourceData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip
                formatter={(value: number) => [formatNumber(value), 'TWh']}
              />
              <Bar dataKey="value" fill="#3b82f6" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Scenarios Overview */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Scenarios Overview</h3>
          <p className="card-description">Active and baseline scenarios</p>
        </div>
        <div className="card-content">
          <div className="space-y-4">
            {scenarios?.slice(0, 5).map((scenario) => (
              <div
                key={scenario.id}
                className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
              >
                <div>
                  <h4 className="font-medium text-gray-900">{scenario.name}</h4>
                  <p className="text-sm text-gray-600">
                    {scenario.startYear} - {scenario.endYear}
                  </p>
                </div>
                <div className="flex items-center space-x-2">
                  {scenario.isBaseline && (
                    <span className="px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-800">
                      Baseline
                    </span>
                  )}
                  {scenario.isActive && (
                    <span className="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800">
                      Active
                    </span>
                  )}
                </div>
              </div>
            ))}
            {(!scenarios || scenarios.length === 0) && (
              <div className="text-center py-8 text-gray-500">
                <p>No scenarios available</p>
                <p className="text-sm mt-1">Create your first scenario to get started</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
} 