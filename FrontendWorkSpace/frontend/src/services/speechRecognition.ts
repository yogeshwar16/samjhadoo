// Speech Recognition Service for Live Captions

export interface Caption {
  id: string
  text: string
  timestamp: Date
  isFinal: boolean
  language: string
}

export type SupportedLanguage = 'en-US' | 'es-ES' | 'fr-FR' | 'de-DE' | 'hi-IN' | 'zh-CN'

class SpeechRecognitionService {
  private recognition: any = null
  private isListening = false
  private currentLanguage: SupportedLanguage = 'en-US'
  private onCaptionCallback: ((caption: Caption) => void) | null = null

  constructor() {
    // Check if browser supports Speech Recognition
    const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition

    if (SpeechRecognition) {
      this.recognition = new SpeechRecognition()
      this.setupRecognition()
    }
  }

  private setupRecognition() {
    if (!this.recognition) return

    // Configuration
    this.recognition.continuous = true
    this.recognition.interimResults = true
    this.recognition.maxAlternatives = 1
    this.recognition.lang = this.currentLanguage

    // Event handlers
    this.recognition.onresult = (event: any) => {
      const result = event.results[event.results.length - 1]
      const transcript = result[0].transcript
      const isFinal = result.isFinal

      const caption: Caption = {
        id: Date.now().toString(),
        text: transcript,
        timestamp: new Date(),
        isFinal,
        language: this.currentLanguage,
      }

      if (this.onCaptionCallback) {
        this.onCaptionCallback(caption)
      }
    }

    this.recognition.onerror = (event: any) => {
      console.error('Speech recognition error:', event.error)
      
      // Auto-restart on certain errors
      if (event.error === 'no-speech' || event.error === 'audio-capture') {
        setTimeout(() => {
          if (this.isListening) {
            this.start()
          }
        }, 1000)
      }
    }

    this.recognition.onend = () => {
      // Auto-restart if still supposed to be listening
      if (this.isListening) {
        this.recognition.start()
      }
    }
  }

  start(language: SupportedLanguage = 'en-US') {
    if (!this.recognition) {
      console.error('Speech recognition not supported in this browser')
      return false
    }

    try {
      this.currentLanguage = language
      this.recognition.lang = language
      this.isListening = true
      this.recognition.start()
      return true
    } catch (error) {
      console.error('Failed to start speech recognition:', error)
      return false
    }
  }

  stop() {
    if (this.recognition && this.isListening) {
      this.isListening = false
      this.recognition.stop()
    }
  }

  setLanguage(language: SupportedLanguage) {
    this.currentLanguage = language
    if (this.recognition) {
      this.recognition.lang = language
    }
  }

  onCaption(callback: (caption: Caption) => void) {
    this.onCaptionCallback = callback
  }

  isSupported(): boolean {
    return this.recognition !== null
  }

  getIsListening(): boolean {
    return this.isListening
  }

  // Get list of supported languages
  static getSupportedLanguages() {
    return [
      { code: 'en-US' as SupportedLanguage, name: 'English (US)', flag: '🇺🇸' },
      { code: 'es-ES' as SupportedLanguage, name: 'Spanish', flag: '🇪🇸' },
      { code: 'fr-FR' as SupportedLanguage, name: 'French', flag: '🇫🇷' },
      { code: 'de-DE' as SupportedLanguage, name: 'German', flag: '🇩🇪' },
      { code: 'hi-IN' as SupportedLanguage, name: 'Hindi', flag: '🇮🇳' },
      { code: 'zh-CN' as SupportedLanguage, name: 'Chinese', flag: '🇨🇳' },
    ]
  }
}

export const speechRecognitionService = new SpeechRecognitionService()
export default speechRecognitionService
