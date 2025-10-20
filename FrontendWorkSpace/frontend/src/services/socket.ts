import { io, Socket } from 'socket.io-client'

const SOCKET_URL = import.meta.env.VITE_SOCKET_URL || 'http://localhost:8081'

class SocketService {
  private socket: Socket | null = null
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5

  connect(token: string): Socket {
    if (this.socket?.connected) {
      return this.socket
    }

    this.socket = io(SOCKET_URL, {
      auth: {
        token,
      },
      transports: ['websocket', 'polling'],
      reconnection: true,
      reconnectionDelay: 1000,
      reconnectionDelayMax: 5000,
      reconnectionAttempts: this.maxReconnectAttempts,
    })

    this.setupEventListeners()
    return this.socket
  }

  private setupEventListeners() {
    if (!this.socket) return

    this.socket.on('connect', () => {
      console.log('✅ Socket connected:', this.socket?.id)
      this.reconnectAttempts = 0
    })

    this.socket.on('disconnect', (reason) => {
      console.log('❌ Socket disconnected:', reason)
    })

    this.socket.on('connect_error', (error) => {
      console.error('Socket connection error:', error)
      this.reconnectAttempts++
    })

    this.socket.on('reconnect', (attemptNumber) => {
      console.log('🔄 Socket reconnected after', attemptNumber, 'attempts')
    })

    this.socket.on('reconnect_failed', () => {
      console.error('❌ Socket reconnection failed after max attempts')
    })
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect()
      this.socket = null
    }
  }

  getSocket(): Socket | null {
    return this.socket
  }

  isConnected(): boolean {
    return this.socket?.connected ?? false
  }

  // Session events
  joinSession(sessionId: string) {
    this.socket?.emit('session:join', { sessionId })
  }

  leaveSession(sessionId: string) {
    this.socket?.emit('session:leave', { sessionId })
  }

  // Chat events
  sendMessage(sessionId: string, message: string) {
    this.socket?.emit('chat:message', { sessionId, message })
  }

  onMessage(callback: (data: any) => void) {
    this.socket?.on('chat:message', callback)
  }

  offMessage(callback: (data: any) => void) {
    this.socket?.off('chat:message', callback)
  }

  // Typing indicator
  sendTyping(sessionId: string, isTyping: boolean) {
    this.socket?.emit('chat:typing', { sessionId, isTyping })
  }

  onTyping(callback: (data: any) => void) {
    this.socket?.on('chat:typing', callback)
  }

  offTyping(callback: (data: any) => void) {
    this.socket?.off('chat:typing', callback)
  }

  // Reactions
  sendReaction(sessionId: string, emoji: string) {
    this.socket?.emit('session:reaction', { sessionId, emoji })
  }

  onReaction(callback: (data: any) => void) {
    this.socket?.on('session:reaction', callback)
  }

  offReaction(callback: (data: any) => void) {
    this.socket?.off('session:reaction', callback)
  }

  // Participant events
  onParticipantJoined(callback: (data: any) => void) {
    this.socket?.on('session:participant-joined', callback)
  }

  onParticipantLeft(callback: (data: any) => void) {
    this.socket?.on('session:participant-left', callback)
  }

  offParticipantJoined(callback: (data: any) => void) {
    this.socket?.off('session:participant-joined', callback)
  }

  offParticipantLeft(callback: (data: any) => void) {
    this.socket?.off('session:participant-left', callback)
  }

  // WebRTC signaling
  sendSignal(sessionId: string, targetUserId: string, signal: any) {
    this.socket?.emit('webrtc:signal', { sessionId, targetUserId, signal })
  }

  onSignal(callback: (data: any) => void) {
    this.socket?.on('webrtc:signal', callback)
  }

  offSignal(callback: (data: any) => void) {
    this.socket?.off('webrtc:signal', callback)
  }

  // Notifications
  onNotification(callback: (data: any) => void) {
    this.socket?.on('notification', callback)
  }

  offNotification(callback: (data: any) => void) {
    this.socket?.off('notification', callback)
  }
}

export const socketService = new SocketService()
export default socketService
