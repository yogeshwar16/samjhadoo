import { motion, AnimatePresence } from 'framer-motion'
import { useState } from 'react'
import { Button } from '@/components/ui/button'

interface Reaction {
  emoji: string
  userId: string
  userName: string
  timestamp: Date
}

interface EmojiReactionsProps {
  onReact: (emoji: string) => void
  reactions?: Reaction[]
  className?: string
}

const QUICK_REACTIONS = ['👍', '❤️', '😂', '🎉', '👏', '🔥']

export default function EmojiReactions({
  onReact,
  reactions = [],
  className,
}: EmojiReactionsProps) {
  const [showFloating, setShowFloating] = useState<string | null>(null)

  const handleReact = (emoji: string) => {
    onReact(emoji)
    setShowFloating(emoji)
    setTimeout(() => setShowFloating(null), 2000)
  }

  // Group reactions by emoji
  const groupedReactions = reactions.reduce((acc, reaction) => {
    if (!acc[reaction.emoji]) {
      acc[reaction.emoji] = []
    }
    acc[reaction.emoji].push(reaction)
    return acc
  }, {} as Record<string, Reaction[]>)

  return (
    <div className={`relative ${className}`}>
      {/* Quick reaction buttons */}
      <div className="flex gap-2">
        {QUICK_REACTIONS.map((emoji) => (
          <Button
            key={emoji}
            variant="outline"
            size="sm"
            onClick={() => handleReact(emoji)}
            className="h-10 w-10 rounded-full p-0 text-xl hover:scale-110 transition-transform"
          >
            {emoji}
          </Button>
        ))}
      </div>

      {/* Reaction counts */}
      {Object.keys(groupedReactions).length > 0 && (
        <div className="mt-2 flex flex-wrap gap-2">
          {Object.entries(groupedReactions).map(([emoji, reactionList]) => (
            <motion.div
              key={emoji}
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              className="flex items-center gap-1 rounded-full bg-muted px-3 py-1"
            >
              <span className="text-lg">{emoji}</span>
              <span className="text-sm font-medium">{reactionList.length}</span>
            </motion.div>
          ))}
        </div>
      )}

      {/* Floating reactions */}
      <AnimatePresence>
        {showFloating && (
          <motion.div
            initial={{ y: 0, opacity: 1, scale: 1 }}
            animate={{ y: -100, opacity: 0, scale: 2 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 2 }}
            className="pointer-events-none absolute left-1/2 top-0 -translate-x-1/2 text-6xl"
          >
            {showFloating}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
