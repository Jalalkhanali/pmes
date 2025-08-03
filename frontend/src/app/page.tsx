'use client';

import { useQuery } from 'react-query';
import { apiClient } from '@/lib/api';
import { formatNumber, formatLargeNumber } from '@/lib/utils';
import {
  DocumentChartBarIcon,
  ExclamationTriangleIcon,
  GlobeAltIcon,
  UserGroupIcon,
} from '@heroicons/react/24/outline';

import { ArrowTrendingUpIcon as TrendingUpIcon } from '@heroicons/react/24/outline';

import { SECTORS, ENERGY_SOURCES } from '@/types';
import DashboardChart from '@/components/Dashboard/DashboardChart';
import RecentScenarios from '@/components/Dashboard/RecentScenarios';
import QuickActions from '@/components/Dashboard/QuickActions';

export default function DashboardPage() {
  const { data: scenarios, isLoading: scenariosLoading } = useQuery(
    'scenarios',
    () => apiClient.getScenarios(),
    {
      staleTime: 5 * 60 * 1000, // 5 minutes
    }
  );

  const { data: healthStatus, isLoading: healthLoading } = useQuery(
    'health',
    () => apiClient.healthCheck(),
    {
      refetchInterval: 30000, // 30 seconds
    }
  );

  const stats = [
    {
      name: 'Total Scenarios',
      value: scenarios?.length || 0,
      icon: DocumentChartBarIcon,
      color: 'text-blue-600',
      bgColor: 'bg-blue-100',
    },
    {
      name: 'Active Scenarios',
      value: scenarios?.filter(s => s.isActive)?.length || 0,
      icon: TrendingUpIcon,
      color: 'text-green-600',
      bgColor: 'bg-green-100',
    },
    {
      name: 'Energy Sources',
      value: ENERGY_SOURCES.length,
      icon: GlobeAltIcon,
      color: 'text-purple-600',
      bgColor: 'bg-purple-100',
    },
    {
      name: 'Sectors',
      value: SECTORS.length,
      icon: UserGroupIcon,
      color: 'text-orange-600',
      bgColor: 'bg-orange-100',
    },
  ];

  const isHealthy = healthStatus?.status === 'UP';

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-gray-600 mt-1">
            Welcome to PMES Energy Planning System
          </p>
        </div>
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-2">
            <div
              className={`w-3 h-3 rounded-full ${
                isHealthy ? 'bg-green-500' : 'bg-red-500'
              }`}
            />
            <span className="text-sm text-gray-600">
              {isHealthy ? 'System Healthy' : 'System Issues'}
            </span>
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => (
          <div key={stat.name} className="card">
            <div className="card-content">
              <div className="flex items-center">
                <div className={`p-3 rounded-lg ${stat.bgColor}`}>
                  <stat.icon className={`h-6 w-6 ${stat.color}`} />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600">{stat.name}</p>
                  <p className="text-2xl font-bold text-gray-900">
                    {formatNumber(stat.value)}
                  </p>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Charts Section */}
        <div className="lg:col-span-2 space-y-6">
          <DashboardChart />
          
          {/* Quick Stats */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="card">
              <div className="card-header">
                <h3 className="card-title">System Status</h3>
              </div>
              <div className="card-content">
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">API Status</span>
                    <span
                      className={`px-2 py-1 text-xs font-medium rounded-full ${
                        isHealthy
                          ? 'bg-green-100 text-green-800'
                          : 'bg-red-100 text-red-800'
                      }`}
                    >
                      {isHealthy ? 'Online' : 'Offline'}
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">Database</span>
                    <span className="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800">
                      Connected
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">Neural Network</span>
                    <span className="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800">
                      Ready
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div className="card">
              <div className="card-header">
                <h3 className="card-title">Recent Activity</h3>
              </div>
              <div className="card-content">
                <div className="space-y-3">
                  <div className="flex items-center space-x-3">
                    <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                    <span className="text-sm text-gray-600">
                      New scenario created
                    </span>
                  </div>
                  <div className="flex items-center space-x-3">
                    <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                    <span className="text-sm text-gray-600">
                      Forecast completed
                    </span>
                  </div>
                  <div className="flex items-center space-x-3">
                    <div className="w-2 h-2 bg-purple-500 rounded-full"></div>
                    <span className="text-sm text-gray-600">
                      Data imported
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          <QuickActions />
          <RecentScenarios scenarios={scenarios} isLoading={scenariosLoading} />
        </div>
      </div>

      {/* Alerts */}
      {!isHealthy && (
        <div className="card border-l-4 border-l-red-500">
          <div className="card-content">
            <div className="flex items-center">
              <ExclamationTriangleIcon className="h-5 w-5 text-red-500 mr-3" />
              <div>
                <h3 className="text-sm font-medium text-red-800">
                  System Warning
                </h3>
                <p className="text-sm text-red-700 mt-1">
                  The backend service appears to be offline. Some features may not work correctly.
                </p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
} 