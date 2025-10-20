import { useState } from 'react'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Check, X } from 'lucide-react'
import { motion } from 'framer-motion'
import virtualBackgroundService, { BackgroundType } from '@/services/virtualBackground'

interface BackgroundOption {
  id: string
  name: string
  type: BackgroundType
  blurAmount?: number
  imageUrl?: string
  thumbnail: string
}

interface BackgroundSelectorProps {
  onClose: () => void
  onApply: (backgroundId: string) => void
}

export default function BackgroundSelector({ onClose, onApply }: BackgroundSelectorProps) {
  const [selectedId, setSelectedId] = useState<string>('none')
  const backgrounds = [
    {
      id: 'none',
      name: 'No Effect',
      type: 'none' as BackgroundType,
      thumbnail: '/backgrounds/none.jpg',
    },
    ...virtualBackgroundService.getDefaultBackgrounds(),
  ]

  const handleApply = () => {
    const selected = backgrounds.find((bg) => bg.id === selectedId)
    if (selected) {
      virtualBackgroundService.applyBackground({
        type: selected.type,
        blurAmount: selected.blurAmount,
        imageUrl: selected.imageUrl,
      })
      onApply(selectedId)
    }
    onClose()
  }

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.9 }}
      animate={{ opacity: 1, scale: 1 }}
      exit={{ opacity: 0, scale: 0.9 }}
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={onClose}
    >
      <Card
        className="w-full max-w-2xl p-6"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-xl font-semibold">Choose Background</h3>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>

        <div className="mb-6 grid grid-cols-3 gap-4 sm:grid-cols-4">
          {backgrounds.map((bg) => (
            <motion.div
              key={bg.id}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className="relative cursor-pointer"
              onClick={() => setSelectedId(bg.id)}
            >
              <div
                className={`relative aspect-video overflow-hidden rounded-lg border-2 transition-all ${
                  selectedId === bg.id
                    ? 'border-primary-500 ring-2 ring-primary-500'
                    : 'border-gray-300 hover:border-primary-300'
                }`}
              >
                {/* Placeholder for thumbnail */}
                <div className="flex h-full w-full items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-800 dark:to-gray-900">
                  {bg.type === 'none' && (
                    <span className="text-sm text-muted-foreground">None</span>
                  )}
                  {bg.type === 'blur' && (
                    <div
                      className="h-full w-full bg-gradient-to-br from-blue-200 to-purple-200"
                      style={{ filter: `blur(${bg.blurAmount || 10}px)` }}
                    />
                  )}
                  {bg.type === 'image' && (
                    <div className="h-full w-full bg-gradient-to-br from-green-200 to-blue-200" />
                  )}
                </div>

                {/* Selected indicator */}
                {selectedId === bg.id && (
                  <div className="absolute inset-0 flex items-center justify-center bg-primary-500/20">
                    <div className="rounded-full bg-primary-500 p-1">
                      <Check className="h-4 w-4 text-white" />
                    </div>
                  </div>
                )}
              </div>

              <p className="mt-2 text-center text-xs font-medium">{bg.name}</p>
            </motion.div>
          ))}
        </div>

        <div className="flex justify-end gap-2">
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="gradient" onClick={handleApply}>
            Apply Background
          </Button>
        </div>
      </Card>
    </motion.div>
  )
}
