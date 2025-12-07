import React, { useState, useEffect } from 'react';
import Login from './components/Login';
import Signup from './components/Signup';
import EventList from './components/EventList';
import CreateEvent from './components/CreateEvent';
import MyCoupons from './components/MyCoupons';
import CouponRedeemer from './components/CouponRedeemer';
import './styles.css';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [role, setRole] = useState(null);
  const [view, setView] = useState('list'); // 'list', 'create', 'signup', 'my-coupons', 'redeem'

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const storedRole = localStorage.getItem('role');
    if (token) {
      setIsAuthenticated(true);
      setRole(storedRole);
    }
  }, []);

  const handleLoginSuccess = () => {
    setIsAuthenticated(true);
    setRole(localStorage.getItem('role'));
    setView('list');
  };

  const handleLogout = () => {
    localStorage.clear();
    setIsAuthenticated(false);
    setRole(null);
    setView('list');
  };

  if (!isAuthenticated) {
    if (view === 'signup') {
      return <Signup onSignupSuccess={() => setView('login')} onCancel={() => setView('login')} />;
    }
    return (
      <div className="app-container">
        <header>
          <h1>Sirip Reward</h1>
        </header>
        <Login onLoginSuccess={handleLoginSuccess} onGoToSignup={() => setView('signup')} />
      </div>
    );
  }

  // Authenticated Views
  return (
    <div className="app-container">
      <header className="dashboard-header">
        <h1>{role === 'ADMIN' ? 'Admin Dashboard' : 'Sirip Reward Pass'}</h1>
        <div className="header-actions">
          <button onClick={() => setView('list')} className={view === 'list' ? 'active' : ''}>
            {role === 'ADMIN' ? 'Manage Events' : 'Browse Events'}
          </button>

          {role === 'ADMIN' && (
            <>
              <button onClick={() => setView('create')} className={view === 'create' ? 'active' : ''}>Create Event</button>
              <button onClick={() => setView('redeem')} className={view === 'redeem' ? 'active' : ''}>Scanner</button>
            </>
          )}

          {role === 'USER' && (
            <button onClick={() => setView('my-coupons')} className={view === 'my-coupons' ? 'active' : ''}>My Coupons</button>
          )}

          <button onClick={handleLogout} className="logout-btn">Logout</button>
        </div>
      </header>

      <main>
        {view === 'list' && <EventList role={role} />}

        {view === 'create' && role === 'ADMIN' && (
          <CreateEvent
            onEventCreated={() => setView('list')}
            onCancel={() => setView('list')}
          />
        )}

        {view === 'redeem' && role === 'ADMIN' && <CouponRedeemer />}

        {view === 'my-coupons' && role === 'USER' && <MyCoupons />}
      </main>
    </div>
  );
}

export default App;
