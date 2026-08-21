import { NavLink, Outlet } from 'react-router-dom';

const TABS = [
  { to: '/discover', label: 'Discover' },
  { to: '/matches', label: 'Matches' },
  { to: '/chat', label: 'Chat' },
  { to: '/profile', label: 'Profile' },
];

export function AppLayout() {
  return (
    <div className="app-shell">
      <main className="app-content">
        <Outlet />
      </main>
      <nav className="tab-bar" aria-label="Primary">
        {TABS.map((tab) => (
          <NavLink
            key={tab.to}
            to={tab.to}
            className={({ isActive }) => `tab-bar__item${isActive ? ' tab-bar__item--active' : ''}`}
          >
            {tab.label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
