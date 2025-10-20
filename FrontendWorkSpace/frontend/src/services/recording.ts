// Session Recording Service using MediaRecorder API

export interface RecordingOptions {
  videoBitsPerSecond?: number
  audioBitsPerSecond?: number
  mimeType?: string
}

export interface Recording {
  id: string
  sessionId: string
  startTime: Date
  endTime?: Date
  duration: number
  size: number
  blob?: Blob
  url?: string
}

class RecordingService {
  private mediaRecorder: MediaRecorder | null = null
  private recordedChunks: Blob[] = []
  private startTime: number = 0
  private recording: Recording | null = null

  async startRecording(
    stream: MediaStream,
    sessionId: string,
    options?: RecordingOptions
  ): Promise<boolean> {
    try {
      // Check if MediaRecorder is supported
      if (!MediaRecorder.isTypeSupported) {
        console.error('MediaRecorder not supported')
        return false
      }

      // Determine best mime type
      const mimeType = this.getBestMimeType(options?.mimeType)
      
      const recordingOptions: MediaRecorderOptions = {
        mimeType,
        videoBitsPerSecond: options?.videoBitsPerSecond || 2500000, // 2.5 Mbps
        audioBitsPerSecond: options?.audioBitsPerSecond || 128000,  // 128 kbps
      }

      this.mediaRecorder = new MediaRecorder(stream, recordingOptions)
      this.recordedChunks = []
      this.startTime = Date.now()

      // Initialize recording metadata
      this.recording = {
        id: Date.now().toString(),
        sessionId,
        startTime: new Date(),
        duration: 0,
        size: 0,
      }

      // Handle data available
      this.mediaRecorder.ondataavailable = (event) => {
        if (event.data && event.data.size > 0) {
          this.recordedChunks.push(event.data)
        }
      }

      // Handle recording stop
      this.mediaRecorder.onstop = () => {
        this.finalizeRecording()
      }

      // Handle errors
      this.mediaRecorder.onerror = (event) => {
        console.error('MediaRecorder error:', event)
      }

      // Start recording (collect data every second)
      this.mediaRecorder.start(1000)

      return true
    } catch (error) {
      console.error('Failed to start recording:', error)
      return false
    }
  }

  stopRecording(): Recording | null {
    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.stop()
      return this.recording
    }
    return null
  }

  pauseRecording() {
    if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
      this.mediaRecorder.pause()
    }
  }

  resumeRecording() {
    if (this.mediaRecorder && this.mediaRecorder.state === 'paused') {
      this.mediaRecorder.resume()
    }
  }

  private finalizeRecording() {
    if (!this.recording) return

    // Create blob from chunks
    const blob = new Blob(this.recordedChunks, {
      type: this.mediaRecorder?.mimeType || 'video/webm',
    })

    // Create URL for playback
    const url = URL.createObjectURL(blob)

    // Update recording metadata
    this.recording.endTime = new Date()
    this.recording.duration = Date.now() - this.startTime
    this.recording.size = blob.size
    this.recording.blob = blob
    this.recording.url = url
  }

  getRecording(): Recording | null {
    return this.recording
  }

  downloadRecording(filename?: string) {
    if (!this.recording || !this.recording.url) {
      console.error('No recording available')
      return
    }

    const a = document.createElement('a')
    a.href = this.recording.url
    a.download = filename || `recording-${this.recording.id}.webm`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  }

  async uploadRecording(uploadUrl: string): Promise<boolean> {
    if (!this.recording || !this.recording.blob) {
      console.error('No recording available')
      return false
    }

    try {
      const formData = new FormData()
      formData.append('recording', this.recording.blob, `recording-${this.recording.id}.webm`)
      formData.append('sessionId', this.recording.sessionId)
      formData.append('duration', this.recording.duration.toString())

      const response = await fetch(uploadUrl, {
        method: 'POST',
        body: formData,
      })

      return response.ok
    } catch (error) {
      console.error('Failed to upload recording:', error)
      return false
    }
  }

  private getBestMimeType(preferredType?: string): string {
    const types = [
      preferredType,
      'video/webm;codecs=vp9,opus',
      'video/webm;codecs=vp8,opus',
      'video/webm;codecs=h264,opus',
      'video/webm',
      'video/mp4',
    ].filter(Boolean) as string[]

    for (const type of types) {
      if (MediaRecorder.isTypeSupported(type)) {
        return type
      }
    }

    return 'video/webm'
  }

  isRecording(): boolean {
    return this.mediaRecorder?.state === 'recording'
  }

  isPaused(): boolean {
    return this.mediaRecorder?.state === 'paused'
  }

  getDuration(): number {
    if (!this.startTime) return 0
    return Date.now() - this.startTime
  }

  cleanup() {
    if (this.mediaRecorder) {
      if (this.mediaRecorder.state !== 'inactive') {
        this.mediaRecorder.stop()
      }
      this.mediaRecorder = null
    }

    this.recordedChunks = []
    this.recording = null
    this.startTime = 0
  }

  static formatDuration(ms: number): string {
    const seconds = Math.floor(ms / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)

    const s = seconds % 60
    const m = minutes % 60

    if (hours > 0) {
      return `${hours}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
    }
    return `${m}:${s.toString().padStart(2, '0')}`
  }

  static formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
  }
}

export const recordingService = new RecordingService()
export default recordingService
