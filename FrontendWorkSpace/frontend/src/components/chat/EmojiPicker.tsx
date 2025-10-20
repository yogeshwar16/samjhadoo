import { useEffect, useRef } from 'react'
import { Card } from '@/components/ui/card'

interface EmojiPickerProps {
  onSelect: (emoji: string) => void
  onClose: () => void
}

const EMOJI_CATEGORIES = {
  'Smileys': ['😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘'],
  'Gestures': ['👍', '👎', '👌', '✌️', '🤞', '🤟', '🤘', '🤙', '👏', '🙌', '👐', '🤲', '🤝', '🙏'],
  'Hearts': ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖'],
  'Objects': ['🎉', '🎊', '🎈', '🎁', '🏆', '🥇', '🥈', '🥉', '⭐', '🌟', '💫', '✨', '🔥', '💯'],
}

export default function EmojiPicker({ onSelect, onClose }: EmojiPickerProps) {
  const pickerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (pickerRef.current && !pickerRef.current.contains(event.target as Node)) {
        onClose()
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [onClose])

  return (
    <Card ref={pickerRef} className="w-80 p-4 shadow-lg">
      <div className="space-y-4">
        {Object.entries(EMOJI_CATEGORIES).map(([category, emojis]) => (
          <div key={category}>
            <h4 className="mb-2 text-xs font-semibold text-muted-foreground">{category}</h4>
            <div className="grid grid-cols-8 gap-2">
              {emojis.map((emoji) => (
                <button
                  key={emoji}
                  onClick={() => onSelect(emoji)}
                  className="flex h-8 w-8 items-center justify-center rounded hover:bg-accent transition-colors"
                >
                  <span className="text-xl">{emoji}</span>
                </button>
              ))}
            </div>
          </div>
        ))}
      </div>
    </Card>
  )
}
