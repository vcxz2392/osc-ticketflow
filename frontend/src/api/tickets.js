import api from './client';

export const ticketsApi = {
  list: (params = {}) => api.get('/tickets', { params }).then((r) => r.data),
  stats: () => api.get('/tickets/stats').then((r) => r.data),
  get: (id) => api.get(`/tickets/${id}`).then((r) => r.data),
  create: (payload) => api.post('/tickets', payload).then((r) => r.data),
  updateStatus: (id, status) =>
    api.patch(`/tickets/${id}/status`, { status }).then((r) => r.data),
  assign: (id, assigneeId) =>
    api.patch(`/tickets/${id}/assignee`, { assigneeId }).then((r) => r.data),
  addComment: (id, message) =>
    api.post(`/tickets/${id}/comments`, { message }).then((r) => r.data),
  remove: (id) => api.delete(`/tickets/${id}`),
};

export const usersApi = {
  admins: () => api.get('/users', { params: { role: 'ADMIN' } }).then((r) => r.data),
};
