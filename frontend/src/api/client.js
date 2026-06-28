import axios from 'axios';

const TOKEN_KEY = 'tf_token';
const USER_KEY = 'tf_user';

export const tokenStore = {
  getToken: () => localStorage.getItem(TOKEN_KEY),
  getUser: () => {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  },
  set: (token, user) => {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  },
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
};

const api = axios.create({
  baseURL: '/api',
});

// Calls that should NOT trigger the 401 redirect (the auth screens themselves).
const AUTH_PATHS = ['/auth/login', '/auth/signup'];

function isAuthCall(config) {
  const url = config?.url || '';
  return AUTH_PATHS.some((p) => url.includes(p));
}

// Request interceptor: attach bearer token.
api.interceptors.request.use((config) => {
  const token = tokenStore.getToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: unwrap success envelope, normalize errors.
api.interceptors.response.use(
  (response) => {
    // Success envelope: { success: true, data: <payload> }
    if (response.data && typeof response.data === 'object' && 'data' in response.data) {
      response.data = response.data.data;
    }
    return response;
  },
  (error) => {
    const status = error.response?.status;
    const backendMessage = error.response?.data?.error?.message;

    if (status === 401 && !isAuthCall(error.config)) {
      tokenStore.clear();
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }

    const message =
      backendMessage ||
      error.message ||
      '요청을 처리하는 중 오류가 발생했습니다.';

    return Promise.reject(Object.assign(error, { displayMessage: message }));
  }
);

export default api;
