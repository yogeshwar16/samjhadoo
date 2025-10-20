import { useMemo } from 'react'
import VideoPlayer from './VideoPlayer'
import { motion } from 'framer-motion'
import { cn } from '@/lib/utils'

interface Participant {
  userId: string
  userName: string
  stream: MediaStream | null
  isLocal?: boolean
  isMuted?: boolean
  isVideoOff?: boolean
  isSpeaking?: boolean
}

interface ParticipantGridProps {
  participants: Participant[]
  className?: string
}

export default function ParticipantGrid({ participants, className }: ParticipantGridProps) {
  const gridLayout = useMemo(() => {
    const count = participants.length
    if (count === 1) return 'grid-cols-1'
    if (count === 2) return 'grid-cols-2'
    if (count <= 4) return 'grid-cols-2 grid-rows-2'
    if (count <= 6) return 'grid-cols-3 grid-rows-2'
    if (count <= 9) return 'grid-cols-3 grid-rows-3'
    return 'grid-cols-4'
  }, [participants.length])

  const videoHeight = useMemo(() => {
    const count = participants.length
    if (count === 1) return 'h-full'
    if (count === 2) return 'h-full'
    if (count <= 4) return 'h-[calc(50vh-4rem)]'
    if (count <= 9) return 'h-[calc(33vh-3rem)]'
    return 'h-[calc(25vh-2rem)]'
  }, [participants.length])

  return (
    <div className={cn('h-full w-full p-4', className)}>
      <div className={cn('grid h-full w-full gap-4', gridLayout)}>
        {participants.map((participant, index) => (
          <motion.div
            key={participant.userId}
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.8 }}
            transition={{ delay: index * 0.1 }}
            className={videoHeight}
          >
            <VideoPlayer
              stream={participant.stream}
              userId={participant.userId}
              userName={participant.userName}
              isLocal={participant.isLocal}
              isMuted={participant.isMuted}
              isVideoOff={participant.isVideoOff}
              isSpeaking={participant.isSpeaking}
              className="h-full w-full"
            />
          </motion.div>
        ))}
      </div>

      {/* Participant count */}
      <div className="absolute left-4 top-4 rounded-full bg-black/50 px-4 py-2 text-sm text-white backdrop-blur-sm">
        {participants.length} {participants.length === 1 ? 'Participant' : 'Participants'}
      </div>
    </div>
  )
}
