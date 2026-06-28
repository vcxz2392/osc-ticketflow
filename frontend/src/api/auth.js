import api from './client';

export const authApi = {
  login: (username, password) =>
    api.post('/auth/login', { username, password }).then((r) => r.data),
  signup: (payload) => api.post('/auth/signup', payload).then((r) => r.data),
};
