import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Bell, Check, CheckCheck, Trash2, X, Filter } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { formatTime } from '@/lib/utils'

interface Notification {
  id: string
  type: 'SESSION_REMINDER' | 'SESSION_STARTED' | 'MESSAGE' | 'SYSTEM'
  title: string
  message: string
  read: boolean
  actionUrl?: string
  createdAt: Date
}

interface NotificationCenterProps {
  notifications: Notification[]
  onMarkAsRead: (id: string) => void
  onMarkAllAsRead: () => void
  onDelete: (id: string) => void
  onClearAll: () => void
  className?: string
}

export default function NotificationCenter({
  notifications,
  onMarkAsRead,
  onMarkAllAsRead,
  onDelete,
  onClearAll,
  className = '',
}: NotificationCenterProps) {
  const [isOpen, setIsOpen] = useState(false)
  const [filter, setFilter] = useState<'all' | 'unread'>('all')

  const unreadCount = notifications.filter((n) => !n.read).length

  const filteredNotifications =
    filter === 'unread' ? notifications.filter((n) => !n.read) : notifications

  const getNotificationIcon = (type: Notification['type']) => {
    switch (type) {
      case 'SESSION_REMINDER':
        return '📅'
      case 'SESSION_STARTED':
        return '🎥'
      case 'MESSAGE':
        return '💬'
      case 'SYSTEM':
        return '⚙️'
      default:
        return '🔔'
    }
  }

  return (
    <>
      {/* Notification Bell */}
      <div className="relative">
        <Button
          variant="ghost"
          size="icon"
          onClick={() => setIsOpen(!isOpen)}
          className="relative"
        >
          <Bell className="h-5 w-5" />
          {unreadCount > 0 && (
            <span className="absolute right-1 top-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-xs text-white">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          )}
        </Button>
      </div>

      {/* Notification Panel */}
      <AnimatePresence>
        {isOpen && (
          <>
            {/* Backdrop */}
            <div
              className="fixed inset-0 z-40"
              onClick={() => setIsOpen(false)}
            />

            {/* Panel */}
            <motion.div
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="absolute right-0 top-14 z-50 w-96"
            >
              <Card className="max-h-[600px] overflow-hidden shadow-xl">
                {/* Header */}
                <div className="border-b p-4">
                  <div className="flex items-center justify-between">
                    <h3 className="text-lg font-semibold">Notifications</h3>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => setIsOpen(false)}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>

                  {/* Actions */}
                  <div className="mt-3 flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setFilter(filter === 'all' ? 'unread' : 'all')}
                    >
                      <Filter className="mr-2 h-3 w-3" />
                      {filter === 'all' ? 'Show Unread' : 'Show All'}
                    </Button>

                    {unreadCount > 0 && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={onMarkAllAsRead}
                      >
                        <CheckCheck className="mr-2 h-3 w-3" />
                        Mark All Read
                      </Button>
                    )}

                    {notifications.length > 0 && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={onClearAll}
                      >
                        <Trash2 className="mr-2 h-3 w-3" />
                        Clear All
                      </Button>
                    )}
                  </div>
                </div>

                {/* Notifications List */}
                <div className="max-h-[480px] overflow-y-auto">
                  {filteredNotifications.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-12 text-center">
                      <Bell className="mb-4 h-12 w-12 text-muted-foreground" />
                      <p className="text-sm text-muted-foreground">
                        {filter === 'unread'
                          ? 'No unread notifications'
                          : 'No notifications yet'}
                      </p>
                    </div>
                  ) : (
                    <div className="divide-y">
                      {filteredNotifications.map((notification) => (
                        <motion.div
                          key={notification.id}
                          initial={{ opacity: 0 }}
                          animate={{ opacity: 1 }}
                          exit={{ opacity: 0 }}
                          className={`p-4 transition-colors hover:bg-accent ${
                            !notification.read ? 'bg-primary-50 dark:bg-primary-950' : ''
                          }`}
                        >
                          <div className="flex gap-3">
                            <div className="text-2xl">
                              {getNotificationIcon(notification.type)}
                            </div>

                            <div className="flex-1">
                              <div className="flex items-start justify-between">
                                <div>
                                  <h4 className="font-medium">{notification.title}</h4>
                                  <p className="mt-1 text-sm text-muted-foreground">
                                    {notification.message}
                                  </p>
                                  <p className="mt-2 text-xs text-muted-foreground">
                                    {formatTime(notification.createdAt)}
                                  </p>
                                </div>

                                <div className="flex gap-1">
                                  {!notification.read && (
                                    <Button
                                      variant="ghost"
                                      size="icon"
                                      className="h-8 w-8"
                                      onClick={() => onMarkAsRead(notification.id)}
                                    >
                                      <Check className="h-4 w-4" />
                                    </Button>
                                  )}
                                  <Button
                                    variant="ghost"
                                    size="icon"
                                    className="h-8 w-8"
                                    onClick={() => onDelete(notification.id)}
                                  >
                                    <Trash2 className="h-4 w-4" />
                                  </Button>
                                </div>
                              </div>
                            </div>
                          </div>
                        </motion.div>
                      ))}
                    </div>
                  )}
                </div>
              </Card>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </>
  )
}
