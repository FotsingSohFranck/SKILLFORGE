import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Proxy /api vers le backend local (cible surchargée par SF_API_URL si besoin).
const apiTarget = process.env.SF_API_URL ?? 'http://localhost:8080'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: apiTarget, changeOrigin: true },
    },
  },
})
