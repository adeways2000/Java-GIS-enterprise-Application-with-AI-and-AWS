import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Map,
  Satellite,
  FileImage,
  Database,
  Bot,
  Users,
  Settings,
  ChevronRight
} from 'lucide-react';

interface SidebarProps {
  isOpen: boolean;
}

interface NavItem {
  name: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  description: string;
}

const navigation: NavItem[] = [
  {
    name: 'Dashboard',
    href: '/dashboard',
    icon: LayoutDashboard,
    description: 'Overview and analytics'
  },
  {
    name: 'Map Viewer',
    href: '/map',
    icon: Map,
    description: 'Interactive geospatial map'
  },
  {
    name: 'Satellite Images',
    href: '/satellite-images',
    icon: Satellite,
    description: 'Manage satellite imagery'
  },
  {
    name: 'Shapefiles',
    href: '/shapefiles',
    icon: FileImage,
    description: 'Vector data management'
  },
  {
    name: 'STAC Catalog',
    href: '/stac-catalog',
    icon: Database,
    description: 'Spatio-temporal assets'
  },
  {
    name: 'AI Workflows',
    href: '/ai-workflows',
    icon: Bot,
    description: 'Automated analysis'
  },
  {
    name: 'User Management',
    href: '/users',
    icon: Users,
    description: 'Manage users and roles'
  },
  {
    name: 'Settings',
    href: '/settings',
    icon: Settings,
    description: 'Application settings'
  },
];

export const Sidebar: React.FC<SidebarProps> = ({ isOpen }) => {
  return (
    <div className={`fixed left-0 top-16 h-full bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 transition-all duration-300 z-40 ${
      isOpen ? 'w-64' : 'w-16'
    }`}>
      <div className="flex flex-col h-full">
        <div className="flex-1 flex flex-col pt-5 pb-4 overflow-y-auto">
          <nav className="mt-5 flex-1 px-2 space-y-1">
            {navigation.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.name}
                  to={item.href}
                  className={({ isActive }) =>
                    `group flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors duration-200 ${
                      isActive
                        ? 'bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100'
                        : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-gray-700 dark:hover:text-white'
                    }`
                  }
                >
                  <Icon className={`${isOpen ? 'mr-3' : 'mx-auto'} h-6 w-6 flex-shrink-0`} />
                  {isOpen && (
                    <>
                      <span className="flex-1">{item.name}</span>
                      <ChevronRight className="ml-auto h-4 w-4 opacity-0 group-hover:opacity-100 transition-opacity" />
                    </>
                  )}
                  {!isOpen && (
                    <div className="absolute left-16 ml-2 px-2 py-1 bg-gray-900 text-white text-xs rounded-md opacity-0 group-hover:opacity-100 transition-opacity z-50 whitespace-nowrap">
                      {item.name}
                      <div className="text-xs text-gray-300">{item.description}</div>
                    </div>
                  )}
                </NavLink>
              );
            })}
          </nav>
        </div>

        {isOpen && (
          <div className="flex-shrink-0 flex border-t border-gray-200 dark:border-gray-700 p-4">
            <div className="flex-shrink-0 w-full group block">
              <div className="flex items-center">
                <div className="ml-3">
                  <p className="text-xs font-medium text-gray-700 dark:text-gray-300">
                    BASF GmbH Enterprise
                  </p>
                  <p className="text-xs font-medium text-gray-500 dark:text-gray-400">
                    Geospatial Intelligence Platform
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

