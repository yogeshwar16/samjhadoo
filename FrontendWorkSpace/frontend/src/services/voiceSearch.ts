// Voice Search Service using Web Speech API

class VoiceSearchService {
  private recognition: any = null
  private isListening = false
  private onResultCallback: ((text: string) => void) | null = null
  private onErrorCallback: ((error: string) => void) | null = null

  constructor() {
    const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition

    if (SpeechRecognition) {
      this.recognition = new SpeechRecognition()
      this.setupRecognition()
    }
  }

  private setupRecognition() {
    if (!this.recognition) return

    this.recognition.continuous = false
    this.recognition.interimResults = true
    this.recognition.maxAlternatives = 1
    this.recognition.lang = 'en-US'

    this.recognition.onresult = (event: any) => {
      const result = event.results[event.results.length - 1]
      const transcript = result[0].transcript

      if (result.isFinal && this.onResultCallback) {
        this.onResultCallback(transcript)
        this.stopListening()
      }
    }

    this.recognition.onerror = (event: any) => {
      console.error('Speech recognition error:', event.error)
      if (this.onErrorCallback) {
        this.onErrorCallback(event.error)
      }
      this.stopListening()
    }

    this.recognition.onend = () => {
      this.isListening = false
    }
  }

  startListening(): boolean {
    if (!this.recognition) {
      console.error('Speech recognition not supported')
      return false
    }

    if (this.isListening) {
      return false
    }

    try {
      this.isListening = true
      this.recognition.start()
      return true
    } catch (error) {
      console.error('Failed to start voice recognition:', error)
      this.isListening = false
      return false
    }
  }

  stopListening() {
    if (this.recognition && this.isListening) {
      this.isListening = false
      this.recognition.stop()
    }
  }

  onResult(callback: (text: string) => void) {
    this.onResultCallback = callback
  }

  onError(callback: (error: string) => void) {
    this.onErrorCallback = callback
  }

  isSupported(): boolean {
    return this.recognition !== null
  }

  getIsListening(): boolean {
    return this.isListening
  }
}

export const voiceSearchService = new VoiceSearchService()
export default voiceSearchService
