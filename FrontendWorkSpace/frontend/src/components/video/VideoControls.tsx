import { useState } from 'react'
import { Mic, MicOff, Video, VideoOff, Monitor, MonitorOff, Phone, Settings, Image, Subtitles } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { motion, AnimatePresence } from 'framer-motion'
import { cn } from '@/lib/utils'
import BackgroundSelector from './BackgroundSelector'

interface VideoControlsProps {
  isAudioEnabled: boolean
  isVideoEnabled: boolean
  isScreenSharing: boolean
  isCaptionsEnabled?: boolean
  onToggleAudio: () => void
  onToggleVideo: () => void
  onToggleScreenShare: () => void
  onToggleCaptions?: () => void
  onLeaveSession: () => void
  className?: string
}

export default function VideoControls({
  isAudioEnabled,
  isVideoEnabled,
  isScreenSharing,
  isCaptionsEnabled = false,
  onToggleAudio,
  onToggleVideo,
  onToggleScreenShare,
  onToggleCaptions,
  onLeaveSession,
  className,
}: VideoControlsProps) {
  const [showSettings, setShowSettings] = useState(false)
  const [showBackgroundSelector, setShowBackgroundSelector] = useState(false)

  return (
    <motion.div
      initial={{ y: 100, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      className={cn(
        'flex items-center justify-center gap-3 rounded-full bg-gray-900/90 p-4 backdrop-blur-md',
        className
      )}
    >
      {/* Audio toggle */}
      <Button
        size="lg"
        variant={isAudioEnabled ? 'default' : 'destructive'}
        className="h-14 w-14 rounded-full"
        onClick={onToggleAudio}
      >
        {isAudioEnabled ? (
          <Mic className="h-6 w-6" />
        ) : (
          <MicOff className="h-6 w-6" />
        )}
      </Button>

      {/* Video toggle */}
      <Button
        size="lg"
        variant={isVideoEnabled ? 'default' : 'destructive'}
        className="h-14 w-14 rounded-full"
        onClick={onToggleVideo}
      >
        {isVideoEnabled ? (
          <Video className="h-6 w-6" />
        ) : (
          <VideoOff className="h-6 w-6" />
        )}
      </Button>

      {/* Screen share toggle */}
      <Button
        size="lg"
        variant={isScreenSharing ? 'secondary' : 'outline'}
        className="h-14 w-14 rounded-full"
        onClick={onToggleScreenShare}
      >
        {isScreenSharing ? (
          <MonitorOff className="h-6 w-6" />
        ) : (
          <Monitor className="h-6 w-6" />
        )}
      </Button>

      {/* Virtual Background */}
      <Button
        size="lg"
        variant="outline"
        className="h-14 w-14 rounded-full"
        onClick={() => setShowBackgroundSelector(true)}
      >
        <Image className="h-6 w-6" />
      </Button>

      {/* Live Captions */}
      {onToggleCaptions && (
        <Button
          size="lg"
          variant={isCaptionsEnabled ? 'secondary' : 'outline'}
          className="h-14 w-14 rounded-full"
          onClick={onToggleCaptions}
        >
          <Subtitles className="h-6 w-6" />
        </Button>
      )}

      {/* Settings */}
      <Button
        size="lg"
        variant="outline"
        className="h-14 w-14 rounded-full"
        onClick={() => setShowSettings(!showSettings)}
      >
        <Settings className="h-6 w-6" />
      </Button>

      {/* Leave session */}
      <Button
        size="lg"
        variant="destructive"
        className="h-14 w-14 rounded-full bg-red-600 hover:bg-red-700"
        onClick={onLeaveSession}
      >
        <Phone className="h-6 w-6 rotate-135" />
      </Button>

      {/* Background Selector Modal */}
      <AnimatePresence>
        {showBackgroundSelector && (
          <BackgroundSelector
            onClose={() => setShowBackgroundSelector(false)}
            onApply={(backgroundId) => {
              console.log('Applied background:', backgroundId)
            }}
          />
        )}
      </AnimatePresence>
    </motion.div>
  )
}
