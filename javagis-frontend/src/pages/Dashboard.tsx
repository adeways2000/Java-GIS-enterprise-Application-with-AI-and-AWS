import React, { useState, useEffect } from 'react';
import {
  Satellite,
  FileImage,
  Database,
  Bot,
  TrendingUp,
  AlertTriangle,
  MapPin,
  Activity
} from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell } from 'recharts';

interface DashboardStats {
  satelliteImages: number;
  shapefiles: number;
  stacCollections: number;
  activeWorkflows: number;
  totalStorage: string;
  lastUpdate: string;
}

interface ChartData {
  name: string;
  value: number;
  color?: string;
}

export const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats>({
    satelliteImages: 0,
    shapefiles: 0,
    stacCollections: 0,
    activeWorkflows: 0,
    totalStorage: '0 GB',
    lastUpdate: new Date().toISOString()
  });

  const [loading, setLoading] = useState(true);

  // Mock data for charts
  const activityData: ChartData[] = [
    { name: 'Jan', value: 120 },
    { name: 'Feb', value: 190 },
    { name: 'Mar', value: 300 },
    { name: 'Apr', value: 250 },
    { name: 'May', value: 420 },
    { name: 'Jun', value: 380 },
  ];

  const storageData: ChartData[] = [
    { name: 'Satellite Images', value: 45, color: '#3B82F6' },
    { name: 'Shapefiles', value: 25, color: '#10B981' },
    { name: 'STAC Data', value: 20, color: '#F59E0B' },
    { name: 'AI Models', value: 10, color: '#EF4444' },
  ];

  const workflowData: ChartData[] = [
    { name: 'Environmental Monitoring', value: 15 },
    { name: 'Asset Tracking', value: 8 },
    { name: 'Anomaly Detection', value: 12 },
    { name: 'Predictive Analysis', value: 6 },
  ];

  useEffect(() => {
    // Simulate API call to fetch dashboard data
    const fetchDashboardData = async () => {
      try {
        // In a real app, this would be an API call
        setTimeout(() => {
          setStats({
            satelliteImages: 1247,
            shapefiles: 89,
            stacCollections: 23,
            activeWorkflows: 41,
            totalStorage: '2.4 TB',
            lastUpdate: new Date().toISOString()
          });
          setLoading(false);
        }, 1000);
      } catch (error) {
        console.error('Failed to fetch dashboard data:', error);
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  const StatCard: React.FC<{
    title: string;
    value: string | number;
    icon: React.ComponentType<{ className?: string }>;
    color: string;
    change?: string;
  }> = ({ title, value, icon: Icon, color, change }) => (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-gray-600 dark:text-gray-400">{title}</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">{value}</p>
          {change && (
            <p className="text-sm text-green-600 dark:text-green-400 flex items-center mt-1">
              <TrendingUp className="h-4 w-4 mr-1" />
              {change}
            </p>
          )}
        </div>
        <div className={`p-3 rounded-full ${color}`}>
          <Icon className="h-6 w-6 text-white" />
        </div>
      </div>
    </div>
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Dashboard
          </h1>
          <p className="text-gray-600 dark:text-gray-400">
            BASF GmbH Geospatial Intelligence Overview
          </p>
        </div>
        <div className="text-sm text-gray-500 dark:text-gray-400">
          Last updated: {new Date(stats.lastUpdate).toLocaleString()}
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          title="Satellite Images"
          value={stats.satelliteImages.toLocaleString()}
          icon={Satellite}
          color="bg-blue-500"
          change="+12% this month"
        />
        <StatCard
          title="Shapefiles"
          value={stats.shapefiles}
          icon={FileImage}
          color="bg-green-500"
          change="+5% this month"
        />
        <StatCard
          title="STAC Collections"
          value={stats.stacCollections}
          icon={Database}
          color="bg-yellow-500"
          change="+8% this month"
        />
        <StatCard
          title="Active Workflows"
          value={stats.activeWorkflows}
          icon={Bot}
          color="bg-purple-500"
          change="+15% this month"
        />
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Activity Chart */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
            Data Processing Activity
          </h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={activityData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip />
              <Line type="monotone" dataKey="value" stroke="#3B82F6" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Storage Distribution */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
            Storage Distribution ({stats.totalStorage})
          </h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={storageData}
                cx="50%"
                cy="50%"
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
                label={({ name, percent }) => `${name} ${percent ? (percent * 100).toFixed(0) : 0}%`}
              >
                {storageData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Workflow Status and Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Active Workflows */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
            Active AI Workflows
          </h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={workflowData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" angle={-45} textAnchor="end" height={80} />
              <YAxis />
              <Tooltip />
              <Bar dataKey="value" fill="#10B981" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* System Status */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
            System Status
          </h3>
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <Activity className="h-5 w-5 text-green-500" />
                <span className="text-sm font-medium text-gray-900 dark:text-white">
                  Database Connection
                </span>
              </div>
              <span className="px-2 py-1 text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 rounded-full">
                Healthy
              </span>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <MapPin className="h-5 w-5 text-green-500" />
                <span className="text-sm font-medium text-gray-900 dark:text-white">
                  GeoTools Service
                </span>
              </div>
              <span className="px-2 py-1 text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 rounded-full">
                Online
              </span>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <Bot className="h-5 w-5 text-yellow-500" />
                <span className="text-sm font-medium text-gray-900 dark:text-white">
                  AI Processing Queue
                </span>
              </div>
              <span className="px-2 py-1 text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200 rounded-full">
                Processing
              </span>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <AlertTriangle className="h-5 w-5 text-red-500" />
                <span className="text-sm font-medium text-gray-900 dark:text-white">
                  AWS Lambda Functions
                </span>
              </div>
              <span className="px-2 py-1 text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200 rounded-full">
                Limited
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          Quick Actions
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <button className="p-4 text-left border border-gray-200 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
            <Satellite className="h-8 w-8 text-blue-500 mb-2" />
            <h4 className="font-medium text-gray-900 dark:text-white">Upload Satellite Data</h4>
            <p className="text-sm text-gray-600 dark:text-gray-400">Add new satellite imagery</p>
          </button>

          <button className="p-4 text-left border border-gray-200 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
            <FileImage className="h-8 w-8 text-green-500 mb-2" />
            <h4 className="font-medium text-gray-900 dark:text-white">Import Shapefiles</h4>
            <p className="text-sm text-gray-600 dark:text-gray-400">Load vector data</p>
          </button>

          <button className="p-4 text-left border border-gray-200 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
            <Bot className="h-8 w-8 text-purple-500 mb-2" />
            <h4 className="font-medium text-gray-900 dark:text-white">Run AI Analysis</h4>
            <p className="text-sm text-gray-600 dark:text-gray-400">Start automated workflow</p>
          </button>

          <button className="p-4 text-left border border-gray-200 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
            <Database className="h-8 w-8 text-yellow-500 mb-2" />
            <h4 className="font-medium text-gray-900 dark:text-white">Browse STAC</h4>
            <p className="text-sm text-gray-600 dark:text-gray-400">Explore catalog</p>
          </button>
        </div>
      </div>
    </div>
  );
};

