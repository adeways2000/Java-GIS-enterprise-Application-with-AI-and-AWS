import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'sonner';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { ThemeProvider } from './contexts/ThemeContext';
import { Navbar } from './components/layout/Navbar';
import { Sidebar } from './components/layout/Sidebar';
import { LoginPage } from './pages/LoginPage';
import { Dashboard } from './pages/Dashboard';
import { MapViewer } from './pages/MapViewer';
import { SatelliteImages } from './pages/SatelliteImages';
import { Shapefiles } from './pages/Shapefiles';
import { StacCatalog } from './pages/StacCatalog';
import { AiWorkflows } from './pages/AiWorkflows';
import { UserManagement } from './pages/UserManagement';
import { Settings } from './pages/Settings';
import { LoadingSpinner } from './components/ui/LoadingSpinner';
import './App.css';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <LoadingSpinner />;
  }

  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

const AppLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(true);

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Navbar onMenuClick={() => setSidebarOpen(!sidebarOpen)} />
      <div className="flex">
        <Sidebar isOpen={sidebarOpen} />
        <main className={`flex-1 transition-all duration-300 ${sidebarOpen ? 'ml-64' : 'ml-16'}`}>
          <div className="p-6">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
};

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <Router>
          <div className="App">
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route
                path="/*"
                element={
                  <ProtectedRoute>
                    <AppLayout>
                      <Routes>
                        <Route path="/" element={<Navigate to="/dashboard" replace />} />
                        <Route path="/dashboard" element={<Dashboard />} />
                        <Route path="/map" element={<MapViewer />} />
                        <Route path="/satellite-images" element={<SatelliteImages />} />
                        <Route path="/shapefiles" element={<Shapefiles />} />
                        <Route path="/stac-catalog" element={<StacCatalog />} />
                        <Route path="/ai-workflows" element={<AiWorkflows />} />
                        <Route path="/users" element={<UserManagement />} />
                        <Route path="/settings" element={<Settings />} />
                      </Routes>
                    </AppLayout>
                  </ProtectedRoute>
                }
              />
            </Routes>
            <Toaster position="top-right" />
          </div>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;

