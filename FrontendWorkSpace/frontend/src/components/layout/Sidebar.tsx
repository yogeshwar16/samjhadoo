import { Link, useLocation } from 'react-router-dom'
import { Home, Video, Heart, BarChart3, Settings, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

interface SidebarProps {
  open: boolean
  onClose: () => void
}

const navigation = [
  { name: 'Home', href: '/', icon: Home },
  { name: 'Sessions', href: '/sessions', icon: Video },
  { name: 'Favorites', href: '/favorites', icon: Heart },
  { name: 'Dashboard', href: '/dashboard', icon: BarChart3 },
  { name: 'Settings', href: '/settings', icon: Settings },
]

export default function Sidebar({ open, onClose }: SidebarProps) {
  const location = useLocation()

  return (
    <>
      {/* Mobile overlay */}
      {open && (
        <div
          className="fixed inset-0 z-40 bg-black/50 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
          'fixed left-0 top-0 z-50 h-full w-64 transform border-r bg-white/80 backdrop-blur-md transition-transform duration-300 dark:bg-gray-900/80 lg:translate-x-0 lg:pt-16',
          open ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        <div className="flex h-16 items-center justify-between px-4 lg:hidden">
          <span className="text-xl font-bold gradient-text">Samjhadoo</span>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>

        <nav className="space-y-1 p-4">
          {navigation.map((item) => {
            const isActive = location.pathname === item.href
            return (
              <Link
                key={item.name}
                to={item.href}
                onClick={onClose}
                className={cn(
                  'flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-primary-50 text-primary-600 dark:bg-primary-950 dark:text-primary-400'
                    : 'text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-800'
                )}
              >
                <item.icon className="h-5 w-5" />
                {item.name}
              </Link>
            )
          })}
        </nav>

        {/* Quick stats */}
        <div className="absolute bottom-0 left-0 right-0 border-t p-4">
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Sessions</span>
              <span className="font-semibold">12</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Favorites</span>
              <span className="font-semibold">8</span>
            </div>
          </div>
        </div>
      </aside>
    </>
  )
}
