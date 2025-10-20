import { motion } from 'framer-motion'
import { formatTime } from '@/lib/utils'
import { cn } from '@/lib/utils'

interface Message {
  id: string
  userId: string
  userName: string
  userAvatar?: string
  content: string
  timestamp: Date
  isOwn: boolean
}

interface MessageBubbleProps {
  message: Message
}

export default function MessageBubble({ message }: MessageBubbleProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      className={cn(
        'flex gap-3',
        message.isOwn && 'flex-row-reverse'
      )}
    >
      {/* Avatar */}
      <div className="flex-shrink-0">
        {message.userAvatar ? (
          <img
            src={message.userAvatar}
            alt={message.userName}
            className="h-8 w-8 rounded-full"
          />
        ) : (
          <div className="flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-br from-primary-600 to-accent-500 text-sm font-semibold text-white">
            {message.userName.charAt(0).toUpperCase()}
          </div>
        )}
      </div>

      {/* Message content */}
      <div className={cn('flex flex-col gap-1', message.isOwn && 'items-end')}>
        <div className="flex items-center gap-2 text-xs text-muted-foreground">
          {!message.isOwn && <span className="font-medium">{message.userName}</span>}
          <span>{formatTime(message.timestamp)}</span>
        </div>

        <div
          className={cn(
            'max-w-xs rounded-2xl px-4 py-2',
            message.isOwn
              ? 'bg-primary-600 text-white'
              : 'bg-muted text-foreground'
          )}
        >
          <p className="break-words text-sm">{message.content}</p>
        </div>
      </div>
    </motion.div>
  )
}
