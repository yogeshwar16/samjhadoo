import { Routes, Route } from 'react-router-dom'
import { Toaster } from '@/components/ui/toaster'
import { ThemeProvider } from '@/components/providers/theme-provider'
import MainLayout from '@/components/layout/MainLayout'
import HomePage from '@/pages/HomePage'
import SessionsPage from '@/pages/SessionsPage'
import SessionDetailPage from '@/pages/SessionDetailPage'
import FavoritesPage from '@/pages/FavoritesPage'
import ProfilePage from '@/pages/ProfilePage'
import DashboardPage from '@/pages/DashboardPage'
import LoginPage from '@/pages/auth/LoginPage'
import RegisterPage from '@/pages/auth/RegisterPage'
import NotFoundPage from '@/pages/NotFoundPage'

function App() {
  return (
    <ThemeProvider defaultTheme="light" storageKey="samjhadoo-theme">
      <Routes>
        {/* Auth routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Main app routes */}
        <Route path="/" element={<MainLayout />}>
          <Route index element={<HomePage />} />
          <Route path="sessions" element={<SessionsPage />} />
          <Route path="sessions/:id" element={<SessionDetailPage />} />
          <Route path="favorites" element={<FavoritesPage />} />
          <Route path="profile/:id" element={<ProfilePage />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
      <Toaster />
    </ThemeProvider>
  )
}

export default App
