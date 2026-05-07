import { Outlet } from 'react-router-dom';
import { Suspense } from 'react';
import AppSidebar from './AppSidebar';

function ShellLoading() {
  return (
    <div className="state-panel state-panel-inline">
      <div className="loading">
        <div className="spinner" />
      </div>
    </div>
  );
}

export default function AppShell({ role, children }) {
  return (
    <div className="admin-layout">
      <AppSidebar role={role} />
      <main className="main-content">
        <div className="content-shell">
          {children || (
            <Suspense fallback={<ShellLoading />}>
              <Outlet />
            </Suspense>
          )}
        </div>
      </main>
    </div>
  );
}
