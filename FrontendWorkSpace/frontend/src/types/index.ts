export interface User {
  id: string
  email: string
  name: string
  avatar?: string
  role: 'MENTEE' | 'MENTOR' | 'ADMIN'
  bio?: string
  expertise?: string[]
  rating?: number
  totalSessions?: number
  verified?: boolean
  createdAt: string
}

export interface LiveSession {
  id: string
  title: string
  description: string
  mentorId: string
  mentorName: string
  mentorAvatar?: string
  mentorRating?: number
  status: 'SCHEDULED' | 'LIVE' | 'COMPLETED' | 'CANCELLED'
  type: 'ONE_ON_ONE' | 'GROUP' | 'WEBINAR'
  tags: string[]
  scheduledStartTime: string
  scheduledEndTime: string
  actualStartTime?: string
  actualEndTime?: string
  maxParticipants: number
  currentParticipants: number
  price?: number
  currency?: string
  meetingUrl?: string
  recordingUrl?: string
  isFeatured: boolean
  isRecorded: boolean
  thumbnail?: string
  language?: string
  difficulty?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'
}

export interface Favorite {
  id: string
  userId: string
  mentorId: string
  mentor: User
  tags?: string[]
  notes?: string
  isMutual: boolean
  createdAt: string
}

export interface Notification {
  id: string
  userId: string
  type: 'SESSION_REMINDER' | 'SESSION_STARTED' | 'MESSAGE' | 'SYSTEM'
  title: string
  message: string
  read: boolean
  actionUrl?: string
  createdAt: string
}

export interface Message {
  id: string
  senderId: string
  receiverId: string
  content: string
  read: boolean
  createdAt: string
}

export interface SessionFilter {
  search?: string
  type?: LiveSession['type']
  status?: LiveSession['status']
  tags?: string[]
  difficulty?: LiveSession['difficulty']
  language?: string
  priceMin?: number
  priceMax?: number
  dateFrom?: string
  dateTo?: string
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface ApiError {
  message: string
  status: number
  errors?: Record<string, string[]>
}
