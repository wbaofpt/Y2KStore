import { defineStore } from 'pinia'
import axios from 'axios'

axios.defaults.baseURL = 'http://localhost:8080'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('y2k_user')) || null,
    token: localStorage.getItem('y2k_token') || null,
    loadingProfile: false
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => ['ADMIN', 'ROLE_ADMIN'].includes(state.user?.role),
    firstName: (state) => state.user?.fullName?.trim()?.split(/\s+/)?.slice(-1)?.[0] || 'Guest'
  },
  actions: {
    persistSession() {
      if (this.token) {
        localStorage.setItem('y2k_token', this.token)
        axios.defaults.headers.common.Authorization = `Bearer ${this.token}`
      }
      if (this.user) {
        localStorage.setItem('y2k_user', JSON.stringify(this.user))
      }
    },

    initAuth() {
      if (this.token) {
        axios.defaults.headers.common.Authorization = `Bearer ${this.token}`
      }
    },

    async login(email, password) {
      const previousToken = this.token
      const previousUser = this.user
      delete axios.defaults.headers.common.Authorization

      try {
        const response = await axios.post('/api/auth/login', { email, password })
        const { token, ...userData } = response.data

        this.token = token
        this.user = userData
        this.persistSession()
        return { success: true }
      } catch (error) {
        if (previousToken) {
          this.token = previousToken
          this.user = previousUser
          this.persistSession()
        }
        return {
          success: false,
          message: error.response?.data?.message || 'Email or password is incorrect.'
        }
      }
    },

    async register(userData) {
      try {
        await axios.post('/api/auth/register', userData)
        return { success: true }
      } catch (error) {
        return {
          success: false,
          message: error.response?.data?.message || 'Unable to create account.'
        }
      }
    },

    async requestRegistrationCode(email) {
      const response = await axios.post('/api/auth/register/request-code', { email })
      return response.data
    },

    async requestPasswordReset(email) {
      const response = await axios.post('/api/auth/forgot-password', { email })
      return response.data
    },

    async resetPassword(token, password) {
      const response = await axios.post('/api/auth/reset-password', { token, password })
      return response.data
    },

    async fetchProfile() {
      if (!this.token) return null

      this.loadingProfile = true
      try {
        const response = await axios.get('/api/auth/profile')
        this.user = response.data
        this.persistSession()
        return this.user
      } catch (error) {
        if (error.response?.status === 401) this.logout()
        return null
      } finally {
        this.loadingProfile = false
      }
    },

    async updateProfile(payload) {
      const response = await axios.put('/api/auth/profile', payload)
      this.user = response.data
      this.persistSession()
      return this.user
    },

    logout() {
      this.token = null
      this.user = null
      this.loadingProfile = false
      localStorage.removeItem('y2k_token')
      localStorage.removeItem('y2k_user')
      delete axios.defaults.headers.common.Authorization
    }
  }
})
