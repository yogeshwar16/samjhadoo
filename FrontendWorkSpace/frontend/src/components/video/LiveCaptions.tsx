import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Subtitles, X, Globe } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import speechRecognitionService, {
  Caption,
  SupportedLanguage,
} from '@/services/speechRecognition'

interface LiveCaptionsProps {
  isEnabled: boolean
  onToggle: () => void
  className?: string
}

export default function LiveCaptions({ isEnabled, onToggle, className }: LiveCaptionsProps) {
  const [captions, setCaptions] = useState<Caption[]>([])
  const [currentLanguage, setCurrentLanguage] = useState<SupportedLanguage>('en-US')
  const [showLanguageSelector, setShowLanguageSelector] = useState(false)

  useEffect(() => {
    if (!speechRecognitionService.isSupported()) {
      console.warn('Speech recognition not supported in this browser')
      return
    }

    speechRecognitionService.onCaption((caption) => {
      setCaptions((prev) => {
        // Keep only last 3 captions
        const newCaptions = [...prev, caption].slice(-3)
        
        // Remove interim results when final result arrives
        if (caption.isFinal) {
          return newCaptions.filter((c) => c.isFinal || c.id === caption.id)
        }
        
        return newCaptions
      })

      // Auto-remove captions after 5 seconds
      setTimeout(() => {
        setCaptions((prev) => prev.filter((c) => c.id !== caption.id))
      }, 5000)
    })
  }, [])

  useEffect(() => {
    if (isEnabled) {
      speechRecognitionService.start(currentLanguage)
    } else {
      speechRecognitionService.stop()
      setCaptions([])
    }

    return () => {
      speechRecognitionService.stop()
    }
  }, [isEnabled, currentLanguage])

  const handleLanguageChange = (language: SupportedLanguage) => {
    setCurrentLanguage(language)
    speechRecognitionService.setLanguage(language)
    setShowLanguageSelector(false)
  }

  if (!isEnabled) {
    return null
  }

  const languages = speechRecognitionService.getSupportedLanguages()
  const currentLang = languages.find((l) => l.code === currentLanguage)

  return (
    <>
      {/* Captions Display */}
      <div className={`pointer-events-none ${className}`}>
        <AnimatePresence>
          {captions.map((caption) => (
            <motion.div
              key={caption.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="mb-2"
            >
              <div className="inline-block rounded-lg bg-black/80 px-4 py-2 backdrop-blur-sm">
                <p
                  className={`text-sm text-white ${
                    caption.isFinal ? 'font-medium' : 'font-normal opacity-70'
                  }`}
                >
                  {caption.text}
                </p>
              </div>
            </motion.div>
          ))}
        </AnimatePresence>
      </div>

      {/* Controls */}
      <div className="pointer-events-auto absolute right-4 top-20 flex flex-col gap-2">
        <Button
          size="icon"
          variant="outline"
          className="h-10 w-10 rounded-full bg-white/10 backdrop-blur-md"
          onClick={() => setShowLanguageSelector(!showLanguageSelector)}
        >
          <Globe className="h-5 w-5" />
        </Button>

        <Button
          size="icon"
          variant="outline"
          className="h-10 w-10 rounded-full bg-white/10 backdrop-blur-md"
          onClick={onToggle}
        >
          {isEnabled ? <X className="h-5 w-5" /> : <Subtitles className="h-5 w-5" />}
        </Button>
      </div>

      {/* Language Selector */}
      <AnimatePresence>
        {showLanguageSelector && (
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: 20 }}
            className="pointer-events-auto absolute right-16 top-20"
          >
            <Card className="p-2">
              <div className="space-y-1">
                {languages.map((lang) => (
                  <Button
                    key={lang.code}
                    variant={currentLanguage === lang.code ? 'default' : 'ghost'}
                    size="sm"
                    className="w-full justify-start"
                    onClick={() => handleLanguageChange(lang.code)}
                  >
                    <span className="mr-2">{lang.flag}</span>
                    {lang.name}
                  </Button>
                ))}
              </div>
            </Card>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  )
}
