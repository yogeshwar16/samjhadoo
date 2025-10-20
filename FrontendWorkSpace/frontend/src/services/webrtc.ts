import SimplePeer from 'simple-peer'
import 'webrtc-adapter'

export interface PeerConnection {
  userId: string
  peer: SimplePeer.Instance
  stream?: MediaStream
}

class WebRTCService {
  private localStream: MediaStream | null = null
  private peers: Map<string, PeerConnection> = new Map()
  private screenStream: MediaStream | null = null

  async getLocalStream(audio = true, video = true): Promise<MediaStream> {
    try {
      if (this.localStream) {
        return this.localStream
      }

      this.localStream = await navigator.mediaDevices.getUserMedia({
        audio: audio
          ? {
              echoCancellation: true,
              noiseSuppression: true,
              autoGainControl: true,
            }
          : false,
        video: video
          ? {
              width: { ideal: 1280 },
              height: { ideal: 720 },
              frameRate: { ideal: 30 },
            }
          : false,
      })

      return this.localStream
    } catch (error) {
      console.error('Error accessing media devices:', error)
      throw new Error('Failed to access camera/microphone')
    }
  }

  async getScreenStream(): Promise<MediaStream> {
    try {
      this.screenStream = await navigator.mediaDevices.getDisplayMedia({
        video: {
          cursor: 'always',
        },
        audio: false,
      })

      // Stop screen sharing when user clicks "Stop sharing" button
      this.screenStream.getVideoTracks()[0].onended = () => {
        this.stopScreenSharing()
      }

      return this.screenStream
    } catch (error) {
      console.error('Error accessing screen:', error)
      throw new Error('Failed to access screen')
    }
  }

  createPeer(
    userId: string,
    initiator: boolean,
    stream: MediaStream,
    onSignal: (signal: any) => void,
    onStream: (stream: MediaStream) => void,
    onError: (error: Error) => void
  ): SimplePeer.Instance {
    const peer = new SimplePeer({
      initiator,
      trickle: true,
      stream,
      config: {
        iceServers: [
          { urls: 'stun:stun.l.google.com:19302' },
          { urls: 'stun:stun1.l.google.com:19302' },
          { urls: 'stun:stun2.l.google.com:19302' },
        ],
      },
    })

    peer.on('signal', (signal) => {
      onSignal(signal)
    })

    peer.on('stream', (remoteStream) => {
      onStream(remoteStream)
    })

    peer.on('error', (error) => {
      console.error('Peer error:', error)
      onError(error)
    })

    peer.on('close', () => {
      console.log('Peer connection closed for user:', userId)
      this.removePeer(userId)
    })

    this.peers.set(userId, { userId, peer, stream })
    return peer
  }

  handleSignal(userId: string, signal: any) {
    const peerConnection = this.peers.get(userId)
    if (peerConnection) {
      peerConnection.peer.signal(signal)
    }
  }

  removePeer(userId: string) {
    const peerConnection = this.peers.get(userId)
    if (peerConnection) {
      peerConnection.peer.destroy()
      this.peers.delete(userId)
    }
  }

  getPeer(userId: string): PeerConnection | undefined {
    return this.peers.get(userId)
  }

  getAllPeers(): PeerConnection[] {
    return Array.from(this.peers.values())
  }

  toggleAudio(enabled: boolean) {
    if (this.localStream) {
      this.localStream.getAudioTracks().forEach((track) => {
        track.enabled = enabled
      })
    }
  }

  toggleVideo(enabled: boolean) {
    if (this.localStream) {
      this.localStream.getVideoTracks().forEach((track) => {
        track.enabled = enabled
      })
    }
  }

  async startScreenSharing(): Promise<MediaStream> {
    const screenStream = await this.getScreenStream()
    
    // Replace video track in all peer connections
    const videoTrack = screenStream.getVideoTracks()[0]
    this.peers.forEach((peerConnection) => {
      const sender = peerConnection.peer._pc
        ?.getSenders()
        .find((s: RTCRtpSender) => s.track?.kind === 'video')
      if (sender) {
        sender.replaceTrack(videoTrack)
      }
    })

    return screenStream
  }

  stopScreenSharing() {
    if (this.screenStream) {
      this.screenStream.getTracks().forEach((track) => track.stop())
      this.screenStream = null

      // Restore camera video track
      if (this.localStream) {
        const videoTrack = this.localStream.getVideoTracks()[0]
        this.peers.forEach((peerConnection) => {
          const sender = peerConnection.peer._pc
            ?.getSenders()
            .find((s: RTCRtpSender) => s.track?.kind === 'video')
          if (sender && videoTrack) {
            sender.replaceTrack(videoTrack)
          }
        })
      }
    }
  }

  stopLocalStream() {
    if (this.localStream) {
      this.localStream.getTracks().forEach((track) => track.stop())
      this.localStream = null
    }
  }

  cleanup() {
    // Stop all streams
    this.stopLocalStream()
    this.stopScreenSharing()

    // Close all peer connections
    this.peers.forEach((peerConnection) => {
      peerConnection.peer.destroy()
    })
    this.peers.clear()
  }

  isAudioEnabled(): boolean {
    return this.localStream?.getAudioTracks()[0]?.enabled ?? false
  }

  isVideoEnabled(): boolean {
    return this.localStream?.getVideoTracks()[0]?.enabled ?? false
  }

  isScreenSharing(): boolean {
    return this.screenStream !== null
  }
}

export const webrtcService = new WebRTCService()
export default webrtcService
