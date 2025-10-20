import { motion } from 'framer-motion'
import { Lock } from 'lucide-react'
import { Card } from '@/components/ui/card'
import type { Badge } from '@/services/gamification'

interface BadgeCardProps {
  badge: Badge
  onClick?: () => void
}

const rarityColors = {
  common: 'from-gray-400 to-gray-600',
  rare: 'from-blue-400 to-blue-600',
  epic: 'from-purple-400 to-purple-600',
  legendary: 'from-yellow-400 to-orange-600',
}

export default function BadgeCard({ badge, onClick }: BadgeCardProps) {
  return (
    <motion.div
      whileHover={{ scale: badge.locked ? 1 : 1.05 }}
      whileTap={{ scale: badge.locked ? 1 : 0.95 }}
    >
      <Card
        className={`relative cursor-pointer overflow-hidden p-4 transition-all ${
          badge.locked ? 'opacity-50 grayscale' : 'shadow-lg'
        }`}
        onClick={onClick}
      >
        {/* Rarity gradient background */}
        <div
          className={`absolute inset-0 bg-gradient-to-br ${
            rarityColors[badge.rarity]
          } opacity-10`}
        />

        <div className="relative flex flex-col items-center gap-2">
          {/* Badge icon */}
          <div className="relative">
            <div className="text-4xl">{badge.icon}</div>
            {badge.locked && (
              <div className="absolute inset-0 flex items-center justify-center">
                <Lock className="h-6 w-6 text-gray-500" />
              </div>
            )}
          </div>

          {/* Badge name */}
          <h4 className="text-center text-sm font-semibold">{badge.name}</h4>

          {/* Badge description */}
          <p className="text-center text-xs text-muted-foreground">{badge.description}</p>

          {/* Rarity indicator */}
          <span
            className={`text-xs font-medium capitalize ${
              badge.rarity === 'legendary'
                ? 'text-yellow-600'
                : badge.rarity === 'epic'
                ? 'text-purple-600'
                : badge.rarity === 'rare'
                ? 'text-blue-600'
                : 'text-gray-600'
            }`}
          >
            {badge.rarity}
          </span>

          {/* Unlock date */}
          {badge.unlockedAt && (
            <span className="text-xs text-muted-foreground">
              Unlocked {new Date(badge.unlockedAt).toLocaleDateString()}
            </span>
          )}
        </div>
      </Card>
    </motion.div>
  )
}
