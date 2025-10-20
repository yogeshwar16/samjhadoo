import { useEffect, useRef, useState } from 'react'
import { motion } from 'framer-motion'
import { Mic, MicOff, Video, VideoOff, Volume2, VolumeX } from 'lucide-react'
import { cn } from '@/lib/utils'

interface VideoPlayerProps {
  stream: MediaStream | null
  userId: string
  userName: string
  isLocal?: boolean
  isMuted?: boolean
  isVideoOff?: boolean
  isSpeaking?: boolean
  className?: string
}

export default function VideoPlayer({
  stream,
  userId,
  userName,
  isLocal = false,
  isMuted = false,
  isVideoOff = false,
  isSpeaking = false,
  className,
}: VideoPlayerProps) {
  const videoRef = useRef<HTMLVideoElement>(null)
  const [audioLevel, setAudioLevel] = useState(0)

  useEffect(() => {
    if (videoRef.current && stream) {
      videoRef.current.srcObject = stream
    }
  }, [stream])

  // Audio level detection
  useEffect(() => {
    if (!stream || isLocal) return

    const audioContext = new AudioContext()
    const analyser = audioContext.createAnalyser()
    const microphone = audioContext.createMediaStreamSource(stream)
    const dataArray = new Uint8Array(analyser.frequencyBinCount)

    analyser.smoothingTimeConstant = 0.8
    analyser.fftSize = 1024
    microphone.connect(analyser)

    const detectAudio = () => {
      analyser.getByteFrequencyData(dataArray)
      const average = dataArray.reduce((a, b) => a + b) / dataArray.length
      setAudioLevel(average)
      requestAnimationFrame(detectAudio)
    }

    detectAudio()

    return () => {
      microphone.disconnect()
      audioContext.close()
    }
  }, [stream, isLocal])

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.9 }}
      animate={{ opacity: 1, scale: 1 }}
      exit={{ opacity: 0, scale: 0.9 }}
      className={cn(
        'relative overflow-hidden rounded-lg bg-gray-900',
        isSpeaking && 'ring-4 ring-primary-500',
        className
      )}
    >
      {/* Video element */}
      <video
        ref={videoRef}
        autoPlay
        playsInline
        muted={isLocal}
        className={cn(
          'h-full w-full object-cover',
          isVideoOff && 'hidden'
        )}
      />

      {/* Avatar placeholder when video is off */}
      {isVideoOff && (
        <div className="flex h-full w-full items-center justify-center bg-gradient-to-br from-primary-600 to-accent-500">
          <div className="text-6xl font-bold text-white">
            {userName.charAt(0).toUpperCase()}
          </div>
        </div>
      )}

      {/* User info overlay */}
      <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 to-transparent p-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium text-white">
              {userName}
              {isLocal && ' (You)'}
            </span>
            {isSpeaking && (
              <motion.div
                animate={{ scale: [1, 1.2, 1] }}
                transition={{ repeat: Infinity, duration: 1 }}
                className="h-2 w-2 rounded-full bg-green-500"
              />
            )}
          </div>

          <div className="flex items-center gap-2">
            {isMuted ? (
              <MicOff className="h-4 w-4 text-red-500" />
            ) : (
              <Mic className="h-4 w-4 text-white" />
            )}
            {isVideoOff && <VideoOff className="h-4 w-4 text-red-500" />}
          </div>
        </div>

        {/* Audio level indicator */}
        {!isMuted && audioLevel > 10 && (
          <div className="mt-2 h-1 w-full overflow-hidden rounded-full bg-gray-700">
            <motion.div
              className="h-full bg-green-500"
              style={{ width: `${Math.min(audioLevel, 100)}%` }}
              transition={{ duration: 0.1 }}
            />
          </div>
        )}
      </div>

      {/* Connection quality indicator */}
      {!isLocal && (
        <div className="absolute right-3 top-3">
          <div className="flex gap-1">
            {[...Array(3)].map((_, i) => (
              <div
                key={i}
                className={cn(
                  'h-2 w-1 rounded-full',
                  i < 2 ? 'bg-green-500' : 'bg-gray-500'
                )}
              />
            ))}
          </div>
        </div>
      )}
    </motion.div>
  )
}
