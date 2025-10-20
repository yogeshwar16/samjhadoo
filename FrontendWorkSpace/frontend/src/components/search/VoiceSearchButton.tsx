import { useState, useEffect } from 'react'
import { Mic } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { motion } from 'framer-motion'
import voiceSearchService from '@/services/voiceSearch'
import { useToast } from '@/hooks/use-toast'

interface VoiceSearchButtonProps {
  onResult: (text: string) => void
  className?: string
}

export default function VoiceSearchButton({ onResult, className }: VoiceSearchButtonProps) {
  const [isListening, setIsListening] = useState(false)
  const [isSupported, setIsSupported] = useState(true)
  const { toast } = useToast()

  useEffect(() => {
    setIsSupported(voiceSearchService.isSupported())

    voiceSearchService.onResult((text) => {
      onResult(text)
      setIsListening(false)
      toast({
        title: 'Voice search',
        description: `Searching for: "${text}"`,
      })
    })

    voiceSearchService.onError((error) => {
      setIsListening(false)
      toast({
        title: 'Voice search error',
        description: error === 'no-speech' ? 'No speech detected' : 'Please try again',
        variant: 'destructive',
      })
    })
  }, [onResult, toast])

  const handleClick = () => {
    if (isListening) {
      voiceSearchService.stopListening()
      setIsListening(false)
    } else {
      const started = voiceSearchService.startListening()
      if (started) {
        setIsListening(true)
        toast({
          title: 'Listening...',
          description: 'Speak now to search',
        })
      }
    }
  }

  if (!isSupported) {
    return null
  }

  return (
    <Button
      variant={isListening ? 'default' : 'ghost'}
      size="icon"
      onClick={handleClick}
      className={className}
    >
      {isListening ? (
        <motion.div
          animate={{ scale: [1, 1.2, 1] }}
          transition={{ repeat: Infinity, duration: 1 }}
        >
          <Mic className="h-5 w-5 text-red-500" />
        </motion.div>
      ) : (
        <Mic className="h-5 w-5" />
      )}
    </Button>
  )
}
