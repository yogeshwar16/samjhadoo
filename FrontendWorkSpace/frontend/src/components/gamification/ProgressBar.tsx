import { motion } from 'framer-motion'
import { Zap } from 'lucide-react'

interface ProgressBarProps {
  level: number
  xp: number
  xpToNextLevel: number
  className?: string
}

export default function ProgressBar({ level, xp, xpToNextLevel, className }: ProgressBarProps) {
  const percentage = (xp / xpToNextLevel) * 100

  return (
    <div className={`flex items-center gap-3 ${className}`}>
      {/* Level badge */}
      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-primary-600 to-accent-500 text-sm font-bold text-white shadow-lg">
        {level}
      </div>

      {/* Progress bar */}
      <div className="flex-1">
        <div className="mb-1 flex items-center justify-between text-xs">
          <span className="font-medium">Level {level}</span>
          <span className="text-muted-foreground">
            {xp} / {xpToNextLevel} XP
          </span>
        </div>
        <div className="h-2 overflow-hidden rounded-full bg-muted">
          <motion.div
            className="h-full bg-gradient-to-r from-primary-600 to-accent-500"
            initial={{ width: 0 }}
            animate={{ width: `${percentage}%` }}
            transition={{ duration: 0.5, ease: 'easeOut' }}
          />
        </div>
      </div>

      {/* XP icon */}
      <Zap className="h-5 w-5 text-yellow-500" />
    </div>
  )
}
