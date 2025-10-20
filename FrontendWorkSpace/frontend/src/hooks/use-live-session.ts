import { useState, useEffect, useCallback, useRef } from 'react'
import { useAuthStore } from '@/store/authStore'
import socketService from '@/services/socket'
import webrtcService, { PeerConnection } from '@/services/webrtc'
import { useToast } from './use-toast'

interface Participant {
  userId: string
  userName: string
  stream: MediaStream | null
  isLocal?: boolean
  isMuted?: boolean
  isVideoOff?: boolean
  isSpeaking?: boolean
}

interface Message {
  id: string
  userId: string
  userName: string
  userAvatar?: string
  content: string
  timestamp: Date
  isOwn: boolean
}

export function useLiveSession(sessionId: string) {
  const { user, token } = useAuthStore()
  const { toast } = useToast()

  const [participants, setParticipants] = useState<Participant[]>([])
  const [messages, setMessages] = useState<Message[]>([])
  const [typingUsers, setTypingUsers] = useState<string[]>([])
  const [isConnected, setIsConnected] = useState(false)
  const [isAudioEnabled, setIsAudioEnabled] = useState(true)
  const [isVideoEnabled, setIsVideoEnabled] = useState(true)
  const [isScreenSharing, setIsScreenSharing] = useState(false)

  const localStreamRef = useRef<MediaStream | null>(null)
  const peersRef = useRef<Map<string, PeerConnection>>(new Map())

  // Initialize session
  const joinSession = useCallback(async () => {
    try {
      // Connect socket
      if (token) {
        socketService.connect(token)
      }

      // Get local stream
      const stream = await webrtcService.getLocalStream(true, true)
      localStreamRef.current = stream

      // Add local participant
      setParticipants((prev) => [
        ...prev,
        {
          userId: user?.id || 'local',
          userName: user?.name || 'You',
          stream,
          isLocal: true,
          isMuted: false,
          isVideoOff: false,
        },
      ])

      // Join session room
      socketService.joinSession(sessionId)
      setIsConnected(true)

      toast({
        title: 'Joined session',
        description: 'You are now connected to the session',
      })
    } catch (error) {
      console.error('Failed to join session:', error)
      toast({
        title: 'Failed to join',
        description: 'Could not access camera/microphone',
        variant: 'destructive',
      })
    }
  }, [sessionId, token, user, toast])

  // Leave session
  const leaveSession = useCallback(() => {
    socketService.leaveSession(sessionId)
    webrtcService.cleanup()
    socketService.disconnect()
    setIsConnected(false)
    setParticipants([])
  }, [sessionId])

  // Toggle audio
  const toggleAudio = useCallback(() => {
    webrtcService.toggleAudio(!isAudioEnabled)
    setIsAudioEnabled(!isAudioEnabled)

    setParticipants((prev) =>
      prev.map((p) =>
        p.isLocal ? { ...p, isMuted: isAudioEnabled } : p
      )
    )
  }, [isAudioEnabled])

  // Toggle video
  const toggleVideo = useCallback(() => {
    webrtcService.toggleVideo(!isVideoEnabled)
    setIsVideoEnabled(!isVideoEnabled)

    setParticipants((prev) =>
      prev.map((p) =>
        p.isLocal ? { ...p, isVideoOff: isVideoEnabled } : p
      )
    )
  }, [isVideoEnabled])

  // Toggle screen share
  const toggleScreenShare = useCallback(async () => {
    try {
      if (isScreenSharing) {
        webrtcService.stopScreenSharing()
        setIsScreenSharing(false)
      } else {
        await webrtcService.startScreenSharing()
        setIsScreenSharing(true)
      }
    } catch (error) {
      console.error('Screen share error:', error)
      toast({
        title: 'Screen share failed',
        description: 'Could not start screen sharing',
        variant: 'destructive',
      })
    }
  }, [isScreenSharing, toast])

  // Send message
  const sendMessage = useCallback(
    (content: string) => {
      const message: Message = {
        id: Date.now().toString(),
        userId: user?.id || '',
        userName: user?.name || '',
        content,
        timestamp: new Date(),
        isOwn: true,
      }

      setMessages((prev) => [...prev, message])
      socketService.sendMessage(sessionId, content)
    },
    [sessionId, user]
  )

  // Send reaction
  const sendReaction = useCallback(
    (emoji: string) => {
      socketService.sendReaction(sessionId, emoji)
    },
    [sessionId]
  )

  // Send typing indicator
  const sendTyping = useCallback(
    (isTyping: boolean) => {
      socketService.sendTyping(sessionId, isTyping)
    },
    [sessionId]
  )

  // Setup socket listeners
  useEffect(() => {
    if (!isConnected) return

    // Handle new participant
    const handleParticipantJoined = (data: any) => {
      console.log('Participant joined:', data)

      if (localStreamRef.current) {
        const peer = webrtcService.createPeer(
          data.userId,
          true,
          localStreamRef.current,
          (signal) => {
            socketService.sendSignal(sessionId, data.userId, signal)
          },
          (stream) => {
            setParticipants((prev) => [
              ...prev,
              {
                userId: data.userId,
                userName: data.userName,
                stream,
                isMuted: false,
                isVideoOff: false,
              },
            ])
          },
          (error) => {
            console.error('Peer error:', error)
          }
        )

        peersRef.current.set(data.userId, { userId: data.userId, peer, stream: undefined })
      }
    }

    // Handle participant left
    const handleParticipantLeft = (data: any) => {
      console.log('Participant left:', data)
      webrtcService.removePeer(data.userId)
      peersRef.current.delete(data.userId)
      setParticipants((prev) => prev.filter((p) => p.userId !== data.userId))
    }

    // Handle WebRTC signal
    const handleSignal = (data: any) => {
      if (data.signal.type === 'offer') {
        if (localStreamRef.current) {
          const peer = webrtcService.createPeer(
            data.userId,
            false,
            localStreamRef.current,
            (signal) => {
              socketService.sendSignal(sessionId, data.userId, signal)
            },
            (stream) => {
              setParticipants((prev) => {
                const exists = prev.find((p) => p.userId === data.userId)
                if (exists) {
                  return prev.map((p) =>
                    p.userId === data.userId ? { ...p, stream } : p
                  )
                }
                return [
                  ...prev,
                  {
                    userId: data.userId,
                    userName: data.userName || 'Unknown',
                    stream,
                    isMuted: false,
                    isVideoOff: false,
                  },
                ]
              })
            },
            (error) => {
              console.error('Peer error:', error)
            }
          )

          peer.signal(data.signal)
          peersRef.current.set(data.userId, { userId: data.userId, peer, stream: undefined })
        }
      } else {
        webrtcService.handleSignal(data.userId, data.signal)
      }
    }

    // Handle chat message
    const handleMessage = (data: any) => {
      const message: Message = {
        id: data.id || Date.now().toString(),
        userId: data.userId,
        userName: data.userName,
        content: data.message,
        timestamp: new Date(data.timestamp),
        isOwn: data.userId === user?.id,
      }
      setMessages((prev) => [...prev, message])
    }

    // Handle typing
    const handleTyping = (data: any) => {
      if (data.isTyping) {
        setTypingUsers((prev) => [...new Set([...prev, data.userName])])
      } else {
        setTypingUsers((prev) => prev.filter((name) => name !== data.userName))
      }
    }

    socketService.onParticipantJoined(handleParticipantJoined)
    socketService.onParticipantLeft(handleParticipantLeft)
    socketService.onSignal(handleSignal)
    socketService.onMessage(handleMessage)
    socketService.onTyping(handleTyping)

    return () => {
      socketService.offParticipantJoined(handleParticipantJoined)
      socketService.offParticipantLeft(handleParticipantLeft)
      socketService.offSignal(handleSignal)
      socketService.offMessage(handleMessage)
      socketService.offTyping(handleTyping)
    }
  }, [isConnected, sessionId, user])

  return {
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
  }
}
