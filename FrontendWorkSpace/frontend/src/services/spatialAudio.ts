// Spatial Audio Service using Web Audio API

export interface ParticipantPosition {
  userId: string
  x: number // -1 to 1 (left to right)
  y: number // -1 to 1 (back to front)
  z: number // -1 to 1 (down to up)
}

class SpatialAudioService {
  private audioContext: AudioContext | null = null
  private listener: AudioListener | null = null
  private panners: Map<string, PannerNode> = new Map()
  private sources: Map<string, MediaStreamAudioSourceNode> = new Map()

  initialize() {
    if (!this.audioContext) {
      this.audioContext = new AudioContext()
      this.listener = this.audioContext.listener

      // Set listener position (user's position)
      if (this.listener.positionX) {
        this.listener.positionX.value = 0
        this.listener.positionY.value = 0
        this.listener.positionZ.value = 0
      } else {
        // Fallback for older browsers
        this.listener.setPosition(0, 0, 0)
      }

      // Set listener orientation (facing forward)
      if (this.listener.forwardX) {
        this.listener.forwardX.value = 0
        this.listener.forwardY.value = 0
        this.listener.forwardZ.value = -1
        this.listener.upX.value = 0
        this.listener.upY.value = 1
        this.listener.upZ.value = 0
      } else {
        // Fallback for older browsers
        this.listener.setOrientation(0, 0, -1, 0, 1, 0)
      }
    }
  }

  addParticipant(userId: string, stream: MediaStream, position: ParticipantPosition) {
    if (!this.audioContext) {
      this.initialize()
    }

    if (!this.audioContext) return

    // Create audio source from stream
    const audioTrack = stream.getAudioTracks()[0]
    if (!audioTrack) return

    const mediaStream = new MediaStream([audioTrack])
    const source = this.audioContext.createMediaStreamSource(mediaStream)

    // Create panner node for spatial audio
    const panner = this.audioContext.createPanner()
    
    // Configure panner
    panner.panningModel = 'HRTF' // Head-Related Transfer Function for realistic 3D audio
    panner.distanceModel = 'inverse'
    panner.refDistance = 1
    panner.maxDistance = 10
    panner.rolloffFactor = 1
    panner.coneInnerAngle = 360
    panner.coneOuterAngle = 0
    panner.coneOuterGain = 0

    // Set initial position
    this.updatePosition(userId, position, panner)

    // Connect nodes: source -> panner -> destination
    source.connect(panner)
    panner.connect(this.audioContext.destination)

    // Store references
    this.sources.set(userId, source)
    this.panners.set(userId, panner)
  }

  updatePosition(userId: string, position: ParticipantPosition, panner?: PannerNode) {
    const pannerNode = panner || this.panners.get(userId)
    if (!pannerNode) return

    // Update panner position
    if (pannerNode.positionX) {
      pannerNode.positionX.value = position.x
      pannerNode.positionY.value = position.y
      pannerNode.positionZ.value = position.z
    } else {
      // Fallback for older browsers
      pannerNode.setPosition(position.x, position.y, position.z)
    }
  }

  removeParticipant(userId: string) {
    const source = this.sources.get(userId)
    const panner = this.panners.get(userId)

    if (source) {
      source.disconnect()
      this.sources.delete(userId)
    }

    if (panner) {
      panner.disconnect()
      this.panners.delete(userId)
    }
  }

  // Calculate position based on grid layout
  calculateGridPosition(index: number, totalParticipants: number): ParticipantPosition {
    const cols = Math.ceil(Math.sqrt(totalParticipants))
    const row = Math.floor(index / cols)
    const col = index % cols

    // Map to -1 to 1 range
    const x = (col / (cols - 1)) * 2 - 1 || 0
    const y = 0 // Keep at same height
    const z = (row / Math.ceil(totalParticipants / cols)) * 2 - 1 || 0

    return { userId: '', x, y, z }
  }

  // Adjust volume based on distance
  setVolume(userId: string, volume: number) {
    const panner = this.panners.get(userId)
    if (!panner || !this.audioContext) return

    const gainNode = this.audioContext.createGain()
    gainNode.gain.value = Math.max(0, Math.min(1, volume))

    // Reconnect with gain
    const source = this.sources.get(userId)
    if (source) {
      source.disconnect()
      source.connect(gainNode)
      gainNode.connect(panner)
    }
  }

  cleanup() {
    this.sources.forEach((source) => source.disconnect())
    this.panners.forEach((panner) => panner.disconnect())
    this.sources.clear()
    this.panners.clear()

    if (this.audioContext) {
      this.audioContext.close()
      this.audioContext = null
    }
  }

  isSupported(): boolean {
    return typeof AudioContext !== 'undefined' || typeof (window as any).webkitAudioContext !== 'undefined'
  }
}

export const spatialAudioService = new SpatialAudioService()
export default spatialAudioService
