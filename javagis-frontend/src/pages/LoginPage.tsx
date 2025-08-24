import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import {
  Eye,
  EyeOff,
  User,
  Lock,
  Key,
  Shield,
  Search,
  BarChart
} from 'lucide-react';
import './LoginPage.css';
import BasfLogo from '../assets/java-gis-logo.svg';

export const LoginPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const { login, isAuthenticated } = useAuth();

  // Animation for title typing effect
  const [displayTitle, setDisplayTitle] = useState('');
  const title = "JavaGIS Enterprise";

  useEffect(() => {
    let index = 0;
    const timer = setInterval(() => {
      setDisplayTitle(title.substring(0, index + 1));
      index++;

      if (index >= title.length) {
        clearInterval(timer);
      }
    }, 100);

    return () => clearInterval(timer);
  }, []);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!username || !password) {
      alert('Please enter both username and password');
      return;
    }

    setIsLoading(true);

    try {
      const success = await login(username, password);
      if (!success) {
        alert('Invalid credentials. Please try again.');
      }
    } catch (error) {
      alert('Login failed. Please check your connection.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      {/* Animated background circles */}
      <div className="animated-background">
        <div className="animated-circle circle-1"></div>
        <div className="animated-circle circle-2"></div>
        <div className="animated-circle circle-3"></div>
      </div>

      <div className="login-card">
        <div className="logo-container">
          <img src={BasfLogo} alt="BASF Logo" className="logo" />
        </div>

        <h1 className="form-title">
          <span className="typing-animation">{displayTitle}</span>
        </h1>
        <p className="form-subtitle">Geospatial Intelligence Platform</p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username" className="form-label">Username</label>
            <input
              id="username"
              type="text"
              className="form-input"
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
            <User size={18} className="input-icon" />
          </div>

          <div className="form-group">
            <label htmlFor="password" className="form-label">Password</label>
            <input
              id="password"
              type={showPassword ? 'text' : 'password'}
              className="form-input"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <Lock size={18} className="input-icon" />
            <button
              type="button"
              className="password-toggle"
              onClick={() => setShowPassword(!showPassword)}
              tabIndex={-1}
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          <div className="form-options">
            <label className="remember-me">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={() => setRememberMe(!rememberMe)}
              />
              Remember me
            </label>

            <a href="#" className="forgot-password">Forgot password?</a>
          </div>

          <button type="submit" className="submit-button" disabled={isLoading}>
            {isLoading ? <div className="spinner"></div> : 'Sign in'}
          </button>
        </form>

        <div className="demo-credentials">
          <h3 className="demo-title">
            <Key size={16} />
            Demo Credentials
          </h3>

          <div className="credential-item admin">
            <div className="credential-info">
              <div className="credential-icon admin">
                <Shield size={16} />
              </div>
              <div className="credential-text">
                <div className="role">Administrator</div>
                <div className="access">Full system access</div>
              </div>
            </div>
            <div className="credential-login">
              admin / admin123
            </div>
          </div>

          <div className="credential-item analyst">
            <div className="credential-info">
              <div className="credential-icon analyst">
                <BarChart size={16} />
              </div>
              <div className="credential-text">
                <div className="role">Analyst</div>
                <div className="access">Data analysis access</div>
              </div>
            </div>
            <div className="credential-login">
              analyst / analyst123
            </div>
          </div>

          <div className="credential-item viewer">
            <div className="credential-info">
              <div className="credential-icon viewer">
                <Search size={16} />
              </div>
              <div className="credential-text">
                <div className="role">Viewer</div>
                <div className="access">Read-only access</div>
              </div>
            </div>
            <div className="credential-login">
              viewer / viewer123
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

