'use client';

import Link from 'next/link';
import { formatDate } from '@/lib/utils';
import { Scenario } from '@/types';
import { ClockIcon, DocumentTextIcon } from '@heroicons/react/24/outline';

interface RecentScenariosProps {
  scenarios?: Scenario[];
  isLoading?: boolean;
}

export default function RecentScenarios({ scenarios, isLoading }: RecentScenariosProps) {
  if (isLoading) {
    return (
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Recent Scenarios</h3>
        </div>
        <div className="card-content">
          <div className="space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-3 bg-gray-200 rounded w-1/2"></div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  const recentScenarios = scenarios?.slice(0, 5) || [];

  return (
    <div className="card">
      <div className="card-header">
        <h3 className="card-title">Recent Scenarios</h3>
        <p className="card-description">Latest energy planning scenarios</p>
      </div>
      <div className="card-content">
        {recentScenarios.length > 0 ? (
          <div className="space-y-3">
            {recentScenarios.map((scenario) => (
              <Link
                key={scenario.id}
                href={`/scenarios/${scenario.id}`}
                className="block p-3 rounded-lg border border-gray-200 hover:border-gray-300 hover:bg-gray-50 transition-colors"
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-2">
                      <DocumentTextIcon className="h-4 w-4 text-gray-400" />
                      <h4 className="text-sm font-medium text-gray-900">
                        {scenario.name}
                      </h4>
                    </div>
                    <p className="text-xs text-gray-500 mt-1">
                      {scenario.startYear} - {scenario.endYear}
                    </p>
                    {scenario.description && (
                      <p className="text-xs text-gray-500 mt-1 line-clamp-2">
                        {scenario.description}
                      </p>
                    )}
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
                {scenario.createdAt && (
                  <div className="flex items-center mt-2 text-xs text-gray-400">
                    <ClockIcon className="h-3 w-3 mr-1" />
                    Created {formatDate(scenario.createdAt)}
                  </div>
                )}
              </Link>
            ))}
          </div>
        ) : (
          <div className="text-center py-8">
            <DocumentTextIcon className="h-12 w-12 text-gray-300 mx-auto mb-4" />
            <h3 className="text-sm font-medium text-gray-900 mb-2">No scenarios yet</h3>
            <p className="text-sm text-gray-500 mb-4">
              Create your first scenario to start energy planning
            </p>
            <Link
              href="/scenarios/new"
              className="btn btn-primary btn-sm"
            >
              Create Scenario
            </Link>
          </div>
        )}
        
        {scenarios && scenarios.length > 5 && (
          <div className="mt-4 pt-4 border-t border-gray-200">
            <Link
              href="/scenarios"
              className="text-sm text-primary-600 hover:text-primary-700 font-medium"
            >
              View all scenarios →
            </Link>
          </div>
        )}
      </div>
    </div>
  );
} 