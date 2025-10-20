// Co-Browsing Service for shared navigation and annotations

export interface CursorPosition {
  userId: string
  userName: string
  x: number
  y: number
  color: string
}

export interface Annotation {
  id: string
  userId: string
  type: 'draw' | 'highlight' | 'text'
  points?: { x: number; y: number }[]
  text?: string
  color: string
  timestamp: Date
}

export interface SharedUrl {
  url: string
  userId: string
  timestamp: Date
}

class CoBrowsingService {
  private isSharing = false
  private isControlling = false
  private cursors: Map<string, CursorPosition> = new Map()
  private annotations: Annotation[] = []
  private currentUrl: string = ''
  private onCursorMoveCallback: ((cursor: CursorPosition) => void) | null = null
  private onAnnotationCallback: ((annotation: Annotation) => void) | null = null
  private onUrlChangeCallback: ((url: SharedUrl) => void) | null = null

  // Start sharing current tab
  startSharing(): boolean {
    if (this.isSharing) return false

    this.isSharing = true
    this.currentUrl = window.location.href

    // Listen to navigation events
    window.addEventListener('popstate', this.handleNavigation)
    
    // Intercept link clicks
    document.addEventListener('click', this.handleLinkClick, true)

    return true
  }

  // Stop sharing
  stopSharing() {
    this.isSharing = false
    window.removeEventListener('popstate', this.handleNavigation)
    document.removeEventListener('click', this.handleLinkClick, true)
  }

  // Handle navigation
  private handleNavigation = () => {
    if (this.isSharing && this.onUrlChangeCallback) {
      const sharedUrl: SharedUrl = {
        url: window.location.href,
        userId: 'current-user', // Replace with actual user ID
        timestamp: new Date(),
      }
      this.onUrlChangeCallback(sharedUrl)
    }
  }

  // Handle link clicks
  private handleLinkClick = (event: MouseEvent) => {
    if (!this.isSharing) return

    const target = event.target as HTMLElement
    const link = target.closest('a')

    if (link && link.href && this.onUrlChangeCallback) {
      const sharedUrl: SharedUrl = {
        url: link.href,
        userId: 'current-user',
        timestamp: new Date(),
      }
      this.onUrlChangeCallback(sharedUrl)
    }
  }

  // Navigate to shared URL
  navigateToUrl(url: string) {
    if (this.isControlling) {
      window.location.href = url
    }
  }

  // Track cursor movement
  trackCursor(userId: string, userName: string, x: number, y: number, color: string) {
    const cursor: CursorPosition = { userId, userName, x, y, color }
    this.cursors.set(userId, cursor)

    if (this.onCursorMoveCallback) {
      this.onCursorMoveCallback(cursor)
    }
  }

  // Send cursor position
  sendCursorPosition(x: number, y: number) {
    // This would be sent via socket
    return {
      x: (x / window.innerWidth) * 100, // Convert to percentage
      y: (y / window.innerHeight) * 100,
    }
  }

  // Add annotation
  addAnnotation(annotation: Omit<Annotation, 'id' | 'timestamp'>): Annotation {
    const newAnnotation: Annotation = {
      ...annotation,
      id: Date.now().toString(),
      timestamp: new Date(),
    }

    this.annotations.push(newAnnotation)

    if (this.onAnnotationCallback) {
      this.onAnnotationCallback(newAnnotation)
    }

    return newAnnotation
  }

  // Remove annotation
  removeAnnotation(id: string) {
    this.annotations = this.annotations.filter((a) => a.id !== id)
  }

  // Clear all annotations
  clearAnnotations() {
    this.annotations = []
  }

  // Get all annotations
  getAnnotations(): Annotation[] {
    return this.annotations
  }

  // Get cursor by user ID
  getCursor(userId: string): CursorPosition | undefined {
    return this.cursors.get(userId)
  }

  // Get all cursors
  getAllCursors(): CursorPosition[] {
    return Array.from(this.cursors.values())
  }

  // Remove cursor
  removeCursor(userId: string) {
    this.cursors.delete(userId)
  }

  // Request control
  requestControl(): boolean {
    this.isControlling = true
    return true
  }

  // Release control
  releaseControl() {
    this.isControlling = false
  }

  // Check if sharing
  getIsSharing(): boolean {
    return this.isSharing
  }

  // Check if controlling
  getIsControlling(): boolean {
    return this.isControlling
  }

  // Event listeners
  onCursorMove(callback: (cursor: CursorPosition) => void) {
    this.onCursorMoveCallback = callback
  }

  onAnnotation(callback: (annotation: Annotation) => void) {
    this.onAnnotationCallback = callback
  }

  onUrlChange(callback: (url: SharedUrl) => void) {
    this.onUrlChangeCallback = callback
  }

  // Cleanup
  cleanup() {
    this.stopSharing()
    this.cursors.clear()
    this.annotations = []
    this.isControlling = false
  }

  // Generate random color for cursor
  static generateCursorColor(): string {
    const colors = [
      '#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A',
      '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E2',
    ]
    return colors[Math.floor(Math.random() * colors.length)]
  }
}

export const coBrowsingService = new CoBrowsingService()
export default coBrowsingService
