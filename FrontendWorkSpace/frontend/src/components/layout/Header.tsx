import { Link } from 'react-router-dom'
import { Bell, Menu, Moon, Search, Sun, User } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { useTheme } from '@/components/providers/theme-provider'
import { useAuthStore } from '@/store/authStore'
import { useState } from 'react'

interface HeaderProps {
  onMenuClick: () => void
}

export default function Header({ onMenuClick }: HeaderProps) {
  const { theme, setTheme } = useTheme()
  const { user, isAuthenticated } = useAuthStore()
  const [searchOpen, setSearchOpen] = useState(false)

  return (
    <header className="fixed top-0 z-50 w-full border-b bg-white/80 backdrop-blur-md dark:bg-gray-900/80">
      <div className="flex h-16 items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Left section */}
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="icon"
            className="lg:hidden"
            onClick={onMenuClick}
          >
            <Menu className="h-5 w-5" />
          </Button>
          <Link to="/" className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-primary-600 to-accent-500">
              <span className="text-lg font-bold text-white">S</span>
            </div>
            <span className="hidden text-xl font-bold gradient-text sm:inline">Samjhadoo</span>
          </Link>
        </div>

        {/* Center section - Search */}
        <div className="hidden flex-1 max-w-xl mx-8 md:block">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              type="search"
              placeholder="Search sessions, mentors..."
              className="pl-10 glass"
            />
          </div>
        </div>

        {/* Right section */}
        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="icon"
            className="md:hidden"
            onClick={() => setSearchOpen(!searchOpen)}
          >
            <Search className="h-5 w-5" />
          </Button>
          
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
          >
            {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
          </Button>

          {isAuthenticated ? (
            <>
              <Button variant="ghost" size="icon" asChild>
                <Link to="/notifications">
                  <Bell className="h-5 w-5" />
                </Link>
              </Button>
              <Button variant="ghost" size="icon" asChild>
                <Link to={`/profile/${user?.id}`}>
                  <User className="h-5 w-5" />
                </Link>
              </Button>
            </>
          ) : (
            <>
              <Button variant="ghost" asChild className="hidden sm:inline-flex">
                <Link to="/login">Login</Link>
              </Button>
              <Button variant="gradient" asChild>
                <Link to="/register">Get Started</Link>
              </Button>
            </>
          )}
        </div>
      </div>

      {/* Mobile search */}
      {searchOpen && (
        <div className="border-t p-4 md:hidden">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              type="search"
              placeholder="Search sessions, mentors..."
              className="pl-10"
            />
          </div>
        </div>
      )}
    </header>
  )
}
