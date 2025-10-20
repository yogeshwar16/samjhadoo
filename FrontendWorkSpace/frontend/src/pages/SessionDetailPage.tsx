import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Calendar, Clock, Users, Video, MessageSquare, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import api from '@/services/api'
import { formatDate, formatTime } from '@/lib/utils'
import { useLiveSession } from '@/hooks/use-live-session'
import ParticipantGrid from '@/components/video/ParticipantGrid'
import VideoControls from '@/components/video/VideoControls'
import ChatPanel from '@/components/chat/ChatPanel'
import EmojiReactions from '@/components/chat/EmojiReactions'
import LiveCaptions from '@/components/video/LiveCaptions'
import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'

export default function SessionDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [showChat, setShowChat] = useState(true)
  const [inSession, setInSession] = useState(false)
  const [captionsEnabled, setCaptionsEnabled] = useState(false)

  const { data: session, isLoading } = useQuery({
    queryKey: ['session', id],
    queryFn: () => api.getSession(id!),
    enabled: !!id,
  })

  const {
    participants,
    messages,
    typingUsers,
    isConnected,
    isAudioEnabled,
    isVideoEnabled,
    isScreenSharing,
    joinSession,
    leaveSession,
    toggleAudio,
    toggleVideo,
    toggleScreenShare,
    sendMessage,
    sendReaction,
    sendTyping,
  } = useLiveSession(id || '')

  const handleJoinSession = async () => {
    await joinSession()
    setInSession(true)
  }

  const handleLeaveSession = () => {
    leaveSession()
    setInSession(false)
    navigate('/sessions')
  }

  if (isLoading) {
    return <div>Loading...</div>
  }

  if (!session) {
    return <div>Session not found</div>
  }

  // Live session view
  if (inSession && isConnected) {
    return (
      <div className="fixed inset-0 z-50 bg-gray-900">
        {/* Main video grid */}
        <div className="relative h-full">
          <ParticipantGrid participants={participants} />

          {/* Video controls */}
          <div className="absolute bottom-6 left-1/2 -translate-x-1/2">
            <VideoControls
              isAudioEnabled={isAudioEnabled}
              isVideoEnabled={isVideoEnabled}
              isScreenSharing={isScreenSharing}
              isCaptionsEnabled={captionsEnabled}
              onToggleAudio={toggleAudio}
              onToggleVideo={toggleVideo}
              onToggleScreenShare={toggleScreenShare}
              onToggleCaptions={() => setCaptionsEnabled(!captionsEnabled)}
              onLeaveSession={handleLeaveSession}
            />
          </div>

          {/* Live Captions */}
          <div className="absolute bottom-32 left-1/2 -translate-x-1/2">
            <LiveCaptions
              isEnabled={captionsEnabled}
              onToggle={() => setCaptionsEnabled(!captionsEnabled)}
            />
          </div>

          {/* Chat toggle button */}
          <Button
            size="icon"
            variant="outline"
            className="absolute right-6 top-6 h-12 w-12 rounded-full bg-white/10 backdrop-blur-md"
            onClick={() => setShowChat(!showChat)}
          >
            {showChat ? <X className="h-6 w-6" /> : <MessageSquare className="h-6 w-6" />}
          </Button>

          {/* Chat sidebar */}
          <AnimatePresence>
            {showChat && (
              <motion.div
                initial={{ x: 400 }}
                animate={{ x: 0 }}
                exit={{ x: 400 }}
                className="absolute right-0 top-0 h-full w-96 bg-white dark:bg-gray-800"
              >
                <ChatPanel
                  messages={messages}
                  onSendMessage={sendMessage}
                  onTyping={sendTyping}
                  typingUsers={typingUsers}
                  className="h-full"
                />
              </motion.div>
            )}
          </AnimatePresence>

          {/* Reactions */}
          <div className="absolute bottom-24 left-6">
            <EmojiReactions onReact={sendReaction} />
          </div>
        </div>
      </div>
    )
  }

  // Session detail view
  return (
    <div className="space-y-6">
      {/* Hero Section */}
      <Card className="overflow-hidden">
        <div className="h-64 bg-gradient-to-br from-primary-600 to-accent-500" />
        <CardContent className="p-6">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div className="flex-1">
              <h1 className="mb-2 text-3xl font-bold">{session.title}</h1>
              <p className="text-muted-foreground">{session.description}</p>
            </div>
            <Button size="lg" variant="gradient" onClick={handleJoinSession}>
              {session.status === 'LIVE' ? 'Join Live Session' : 'Join Session'}
            </Button>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Main Content */}
        <div className="space-y-6 lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>About This Session</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground">{session.description}</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Session Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-3">
                <Calendar className="h-5 w-5 text-muted-foreground" />
                <span>{formatDate(session.scheduledStartTime)}</span>
              </div>
              <div className="flex items-center gap-3">
                <Clock className="h-5 w-5 text-muted-foreground" />
                <span>{formatTime(session.scheduledStartTime)}</span>
              </div>
              <div className="flex items-center gap-3">
                <Users className="h-5 w-5 text-muted-foreground" />
                <span>
                  {session.currentParticipants} / {session.maxParticipants} participants
                </span>
              </div>
              <div className="flex items-center gap-3">
                <Video className="h-5 w-5 text-muted-foreground" />
                <span>{session.type}</span>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Mentor</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-3">
                <div className="h-12 w-12 rounded-full bg-gradient-to-br from-primary-600 to-accent-500" />
                <div>
                  <p className="font-semibold">{session.mentorName}</p>
                  <p className="text-sm text-muted-foreground">Expert Mentor</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Tags</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                {session.tags?.map((tag: string) => (
                  <span
                    key={tag}
                    className="rounded-full bg-primary-50 px-3 py-1 text-sm text-primary-600 dark:bg-primary-950 dark:text-primary-400"
                  >
                    {tag}
                  </span>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
