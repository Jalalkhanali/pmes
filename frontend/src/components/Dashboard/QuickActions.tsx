'use client';

import Link from 'next/link';
import {
  PlusIcon,
  DocumentChartBarIcon,
  ChartBarIcon,
  ArrowUpTrayIcon,
  CogIcon,
} from '@heroicons/react/24/outline';

const actions = [
  {
    name: 'Create Scenario',
    description: 'Create a new energy planning scenario',
    href: '/scenarios/new',
    icon: PlusIcon,
    color: 'bg-blue-500',
  },
  {
    name: 'Run Forecast',
    description: 'Generate energy demand forecasts',
    href: '/forecasting',
    icon: ChartBarIcon,
    color: 'bg-green-500',
  },
  {
    name: 'Import Data',
    description: 'Import energy consumption data',
    href: '/import',
    icon: ArrowUpTrayIcon,
    color: 'bg-purple-500',
  },
  {
    name: 'View Analytics',
    description: 'Analyze emissions and trends',
    href: '/analytics',
    icon: DocumentChartBarIcon,
    color: 'bg-orange-500',
  },
  {
    name: 'Settings',
    description: 'Configure system settings',
    href: '/settings',
    icon: CogIcon,
    color: 'bg-gray-500',
  },
];

export default function QuickActions() {
  return (
    <div className="card">
      <div className="card-header">
        <h3 className="card-title">Quick Actions</h3>
        <p className="card-description">Common tasks and shortcuts</p>
      </div>
      <div className="card-content">
        <div className="grid grid-cols-1 gap-3">
          {actions.map((action) => (
            <Link
              key={action.name}
              href={action.href}
              className="flex items-center p-3 rounded-lg border border-gray-200 hover:border-gray-300 hover:bg-gray-50 transition-colors"
            >
              <div className={`p-2 rounded-lg ${action.color}`}>
                <action.icon className="h-5 w-5 text-white" />
              </div>
              <div className="ml-3 flex-1">
                <h4 className="text-sm font-medium text-gray-900">{action.name}</h4>
                <p className="text-xs text-gray-500">{action.description}</p>
              </div>
              <div className="text-gray-400">
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
} 