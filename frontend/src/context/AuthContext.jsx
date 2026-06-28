import { createContext, useContext, useMemo, useState, useCallback } from 'react';
import { tokenStore } from '../api/client';
import { authApi } from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => tokenStore.getUser());

  const applySession = useCallback((session) => {
    tokenStore.set(session.token, session.user);
    setUser(session.user);
    return session.user;
  }, []);

  const login = useCallback(
    async (username, password) => {
      const session = await authApi.login(username, password);
      return applySession(session);
    },
    [applySession]
  );

  const signup = useCallback(
    async (payload) => {
      const session = await authApi.signup(payload);
      return applySession(session);
    },
    [applySession]
  );

  const logout = useCallback(() => {
    tokenStore.clear();
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: !!user,
      isAdmin: user?.role === 'ADMIN',
      login,
      signup,
      logout,
    }),
    [user, login, signup, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
