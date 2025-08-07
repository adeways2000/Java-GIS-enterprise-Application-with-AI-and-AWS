import React, { useState } from 'react';
import { Save, Database, Cloud, Shield, Bell } from 'lucide-react';

export const Settings: React.FC = () => {
  const [settings, setSettings] = useState({
    database: {
      host: 'localhost',
      port: '5432',
      database: 'javagis_basf',
      username: 'postgres',
      ssl: true
    },
    cloud: {
      provider: 'AWS',
      region: 'eu-central-1',
      bucketName: 'basf-javagis-data',
      enableAutoSync: true
    },
    security: {
      enableTwoFactor: true,
      sessionTimeout: 30,
      passwordExpiry: 90,
      ipRestriction: false
    },
    notifications: {
      email: true,
      slack: false,
      system: true,
      mobileApp: true
    }
  });

  const [activeTab, setActiveTab] = useState('database');
  const [saved, setSaved] = useState(false);

  const handleSave = () => {
    // In a real app, this would save to backend
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  const handleInputChange = (section: string, field: string, value: any) => {
    setSettings({
      ...settings,
      [section]: {
        ...settings[section as keyof typeof settings],
        [field]: value
      }
    });
  };

  const handleCheckboxChange = (section: string, field: string) => {
    setSettings({
      ...settings,
      [section]: {
        ...settings[section as keyof typeof settings],
        [field]: !settings[section as keyof typeof settings][field]
      }
    });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
          Settings
        </h1>
        <p className="text-gray-600 dark:text-gray-400">
          Configure application settings and preferences
        </p>
      </div>

      {/* Settings Container */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
        {/* Tabs */}
        <div className="flex border-b border-gray-200 dark:border-gray-700">
          <button
            onClick={() => setActiveTab('database')}
            className={`px-4 py-3 text-sm font-medium ${
              activeTab === 'database'
                ? 'border-b-2 border-blue-600 text-blue-600 dark:text-blue-400 dark:border-blue-400'
                : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
            }`}
          >
            <div className="flex items-center">
              <Database className="h-4 w-4 mr-2" />
              Database
            </div>
          </button>

          <button
            onClick={() => setActiveTab('cloud')}
            className={`px-4 py-3 text-sm font-medium ${
              activeTab === 'cloud'
                ? 'border-b-2 border-blue-600 text-blue-600 dark:text-blue-400 dark:border-blue-400'
                : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
            }`}
          >
            <div className="flex items-center">
              <Cloud className="h-4 w-4 mr-2" />
              Cloud Storage
            </div>
          </button>

          <button
            onClick={() => setActiveTab('security')}
            className={`px-4 py-3 text-sm font-medium ${
              activeTab === 'security'
                ? 'border-b-2 border-blue-600 text-blue-600 dark:text-blue-400 dark:border-blue-400'
                : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
            }`}
          >
            <div className="flex items-center">
              <Shield className="h-4 w-4 mr-2" />
              Security
            </div>
          </button>

          <button
            onClick={() => setActiveTab('notifications')}
            className={`px-4 py-3 text-sm font-medium ${
              activeTab === 'notifications'
                ? 'border-b-2 border-blue-600 text-blue-600 dark:text-blue-400 dark:border-blue-400'
                : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
            }`}
          >
            <div className="flex items-center">
              <Bell className="h-4 w-4 mr-2" />
              Notifications
            </div>
          </button>
        </div>

        {/* Tab Content */}
        <div className="p-6">
          {/* Database Settings */}
          {activeTab === 'database' && (
            <div className="space-y-4">
              <h2 className="text-lg font-medium text-gray-900 dark:text-white">Database Configuration</h2>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Configure the PostgreSQL/PostGIS database connection settings.
              </p>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Host
                  </label>
                  <input
                    type="text"
                    value={settings.database.host}
                    onChange={(e) => handleInputChange('database', 'host', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Port
                  </label>
                  <input
                    type="text"
                    value={settings.database.port}
                    onChange={(e) => handleInputChange('database', 'port', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Database Name
                  </label>
                  <input
                    type="text"
                    value={settings.database.database}
                    onChange={(e) => handleInputChange('database', 'database', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Username
                  </label>
                  <input
                    type="text"
                    value={settings.database.username}
                    onChange={(e) => handleInputChange('database', 'username', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Password
                  </label>
                  <input
                    type="password"
                    value="••••••••"
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="ssl"
                    checked={settings.database.ssl}
                    onChange={() => handleCheckboxChange('database', 'ssl')}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label htmlFor="ssl" className="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                    Enable SSL Connection
                  </label>
                </div>
              </div>

              <div className="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
                <button
                  onClick={() => {
                    // Test connection logic would go here
                    alert('Connection test successful!');
                  }}
                  className="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 dark:bg-gray-700 dark:text-gray-200 dark:hover:bg-gray-600 mr-2"
                >
                  Test Connection
                </button>
              </div>
            </div>
          )}

          {/* Cloud Settings */}
          {activeTab === 'cloud' && (
            <div className="space-y-4">
              <h2 className="text-lg font-medium text-gray-900 dark:text-white">Cloud Storage Configuration</h2>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Configure AWS S3 storage settings for satellite imagery and shapefiles.
              </p>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Cloud Provider
                  </label>
                  <select
                    value={settings.cloud.provider}
                    onChange={(e) => handleInputChange('cloud', 'provider', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="AWS">AWS</option>
                    <option value="Azure">Azure</option>
                    <option value="GCP">Google Cloud</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Region
                  </label>
                  <input
                    type="text"
                    value={settings.cloud.region}
                    onChange={(e) => handleInputChange('cloud', 'region', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Bucket Name
                  </label>
                  <input
                    type="text"
                    value={settings.cloud.bucketName}
                    onChange={(e) => handleInputChange('cloud', 'bucketName', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="autoSync"
                    checked={settings.cloud.enableAutoSync}
                    onChange={() => handleCheckboxChange('cloud', 'enableAutoSync')}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label htmlFor="autoSync" className="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                    Enable Automatic Synchronization
                  </label>
                </div>
              </div>
            </div>
          )}

          {/* Security Settings */}
          {activeTab === 'security' && (
            <div className="space-y-4">
              <h2 className="text-lg font-medium text-gray-900 dark:text-white">Security Settings</h2>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Configure authentication and security settings.
              </p>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="twoFactor"
                    checked={settings.security.enableTwoFactor}
                    onChange={() => handleCheckboxChange('security', 'enableTwoFactor')}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label htmlFor="twoFactor" className="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                    Enable Two-Factor Authentication
                  </label>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Session Timeout (minutes)
                  </label>
                  <input
                    type="number"
                    value={settings.security.sessionTimeout}
                    onChange={(e) => handleInputChange('security', 'sessionTimeout', parseInt(e.target.value))}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Password Expiry (days)
                  </label>
                  <input
                    type="number"
                    value={settings.security.passwordExpiry}
                    onChange={(e) => handleInputChange('security', 'passwordExpiry', parseInt(e.target.value))}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="ipRestriction"
                    checked={settings.security.ipRestriction}
                    onChange={() => handleCheckboxChange('security', 'ipRestriction')}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label htmlFor="ipRestriction" className="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                    Enable IP Address Restriction
                  </label>
                </div>
              </div>
            </div>
          )}

          {/* Notification Settings */}
          {activeTab === 'notifications' && (
            <div className="space-y-4">
              <h2 className="text-lg font-medium text-gray-900 dark:text-white">Notification Settings</h2>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Configure how you receive notifications and alerts.
              </p>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="emailNotif"
                    checked={settings.notifications.email}
                    onChange={() => handleCheckboxChange('notifications', 'email')}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label htmlFor="emailNotif" className="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                    Email Notifications
                  </label>
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="slackNotif"
                    checked={settings.notifications.slack}
                    onChange={() => handleCheckboxChange('notifications', 'slack')}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label htmlFor="slackNotif" className="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                    Slack Notifications
                  </label>
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="systemNotif"
                    checked={settings.notifications.system}
                    onChange={() => handleCheckboxChange('notifications', 'system')}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label htmlFor="systemNotif" className="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                    System Notifications
                  </label>
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="mobileNotif"
                    checked={settings.notifications.mobileApp}
                    onChange={() => handleCheckboxChange('notifications', 'mobileApp')}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label htmlFor="mobileNotif" className="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                    Mobile App Notifications
                  </label>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-200 dark:border-gray-700 flex justify-between items-center">
          <div>
            {saved && (
              <span className="text-sm text-green-600 dark:text-green-400">
                Settings saved successfully!
              </span>
            )}
          </div>
          <div className="flex space-x-2">
            <button
              onClick={() => {
                // Reset logic would go here
              }}
              className="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 dark:bg-gray-700 dark:text-gray-200 dark:hover:bg-gray-600"
            >
              Reset
            </button>
            <button
              onClick={handleSave}
              className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              <Save className="h-4 w-4 mr-2" />
              Save Changes
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

