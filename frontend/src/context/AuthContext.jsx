import { createContext, useContext, useState, useEffect } from 'react';
import { clearAiAccessibleStudentsCache } from '../api/endpoints';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('edusys_user');
    const savedToken = localStorage.getItem('edusys_token');
    if (savedUser && savedToken) {
      setUser(JSON.parse(savedUser));
      setToken(savedToken);
    }
    setLoading(false);
  }, []);

  const login = (userData, jwtToken) => {
    clearAiAccessibleStudentsCache();
    setUser(userData);
    setToken(jwtToken);
    localStorage.setItem('edusys_user', JSON.stringify(userData));
    localStorage.setItem('edusys_token', jwtToken);
  };

  const logout = () => {
    clearAiAccessibleStudentsCache();
    setUser(null);
    setToken(null);
    localStorage.removeItem('edusys_user');
    localStorage.removeItem('edusys_token');
  };

  if (loading) return null;

  return (
    <AuthContext.Provider value={{ user, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
