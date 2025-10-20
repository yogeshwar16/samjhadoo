import axios, { AxiosError, AxiosInstance } from 'axios'
import type { ApiError } from '@/types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

class ApiService {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    // Request interceptor
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor
    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError<ApiError>) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('token')
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
    )
  }

  // Auth - Updated to use /v1/auth/* endpoints
  async login(email: string, password: string) {
    const response = await this.client.post('/v1/auth/login', { email, password })
    return response.data
  }

  async register(data: { email: string; password: string; name: string; role: string }) {
    const response = await this.client.post('/v1/auth/register', data)
    return response.data
  }

  async logout() {
    localStorage.removeItem('token')
  }

  // Live Sessions
  async getSessions(params?: any) {
    const response = await this.client.get('/live-sessions', { params })
    return response.data
  }

  async getSession(id: string) {
    const response = await this.client.get(`/live-sessions/${id}`)
    return response.data
  }

  async createSession(data: any) {
    const response = await this.client.post('/live-sessions', data)
    return response.data
  }

  async updateSession(id: string, data: any) {
    const response = await this.client.put(`/live-sessions/${id}`, data)
    return response.data
  }

  async joinSession(id: string) {
    const response = await this.client.post(`/live-sessions/${id}/join`)
    return response.data
  }

  async leaveSession(id: string) {
    const response = await this.client.post(`/live-sessions/${id}/leave`)
    return response.data
  }

  // Favorites
  async getFavorites() {
    const response = await this.client.get('/favorites')
    return response.data
  }

  async addFavorite(mentorId: string, tags?: string[]) {
    const response = await this.client.post('/favorites', { mentorId, tags })
    return response.data
  }

  async removeFavorite(id: string) {
    const response = await this.client.delete(`/favorites/${id}`)
    return response.data
  }

  async updateFavoriteTags(id: string, tags: string[]) {
    const response = await this.client.put(`/favorites/${id}/tags`, { tags })
    return response.data
  }

  // Users
  async getProfile(id: string) {
    const response = await this.client.get(`/users/${id}`)
    return response.data
  }

  async updateProfile(id: string, data: any) {
    const response = await this.client.put(`/users/${id}`, data)
    return response.data
  }

  // Notifications
  async getNotifications() {
    const response = await this.client.get('/notifications')
    return response.data
  }

  async markNotificationRead(id: string) {
    const response = await this.client.put(`/notifications/${id}/read`)
    return response.data
  }
}

export const api = new ApiService()
export default api
