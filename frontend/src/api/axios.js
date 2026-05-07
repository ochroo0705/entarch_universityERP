import axios from 'axios';

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
const normalizedBaseUrl = configuredBaseUrl
  ? configuredBaseUrl.replace(/\/+$/, '')
  : '/api';

const api = axios.create({
  baseURL: normalizedBaseUrl,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('edusys_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('edusys_user');
      localStorage.removeItem('edusys_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
