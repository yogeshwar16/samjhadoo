// Gamification Service - XP, Badges, Achievements

export interface UserProgress {
  level: number
  xp: number
  xpToNextLevel: number
  totalXP: number
  badges: Badge[]
  achievements: Achievement[]
  streak: number
  lastLoginDate: string
}

export interface Badge {
  id: string
  name: string
  description: string
  icon: string
  rarity: 'common' | 'rare' | 'epic' | 'legendary'
  unlockedAt?: Date
  locked: boolean
}

export interface Achievement {
  id: string
  name: string
  description: string
  progress: number
  target: number
  reward: number
  icon: string
  completed: boolean
}

export interface LeaderboardEntry {
  userId: string
  userName: string
  avatar?: string
  level: number
  xp: number
  rank: number
}

// XP Rewards
export const XP_REWARDS = {
  SESSION_COMPLETED: 100,
  SESSION_CREATED: 50,
  FIRST_SESSION: 200,
  MENTOR_FAVORITED: 10,
  PROFILE_COMPLETED: 50,
  DAILY_LOGIN: 5,
  WEEK_STREAK: 100,
  MONTH_STREAK: 500,
  SESSION_RATED: 20,
  CHAT_MESSAGE: 1,
  REACTION_SENT: 2,
}

// Level calculation
const calculateLevel = (totalXP: number): number => {
  return Math.floor(Math.sqrt(totalXP / 100)) + 1
}

const calculateXPForNextLevel = (level: number): number => {
  return Math.pow(level, 2) * 100
}

class GamificationService {
  private storageKey = 'gamification_progress'

  // Get user progress
  getUserProgress(userId: string): UserProgress {
    const stored = localStorage.getItem(`${this.storageKey}_${userId}`)
    
    if (stored) {
      return JSON.parse(stored)
    }

    // Initialize new user
    return {
      level: 1,
      xp: 0,
      xpToNextLevel: 100,
      totalXP: 0,
      badges: this.getAllBadges().map(b => ({ ...b, locked: true })),
      achievements: this.getAllAchievements(),
      streak: 0,
      lastLoginDate: new Date().toISOString(),
    }
  }

  // Award XP
  awardXP(userId: string, amount: number, reason: string): UserProgress {
    const progress = this.getUserProgress(userId)
    
    progress.xp += amount
    progress.totalXP += amount

    // Check for level up
    while (progress.xp >= progress.xpToNextLevel) {
      progress.xp -= progress.xpToNextLevel
      progress.level += 1
      progress.xpToNextLevel = calculateXPForNextLevel(progress.level)
      
      // Trigger level up event
      this.onLevelUp(userId, progress.level)
    }

    this.saveProgress(userId, progress)
    return progress
  }

  // Unlock badge
  unlockBadge(userId: string, badgeId: string): boolean {
    const progress = this.getUserProgress(userId)
    const badge = progress.badges.find(b => b.id === badgeId)

    if (badge && badge.locked) {
      badge.locked = false
      badge.unlockedAt = new Date()
      this.saveProgress(userId, progress)
      return true
    }

    return false
  }

  // Update achievement progress
  updateAchievement(userId: string, achievementId: string, progressValue: number): boolean {
    const progress = this.getUserProgress(userId)
    const achievement = progress.achievements.find(a => a.id === achievementId)

    if (achievement && !achievement.completed) {
      achievement.progress = Math.min(progressValue, achievement.target)

      if (achievement.progress >= achievement.target) {
        achievement.completed = true
        this.awardXP(userId, achievement.reward, `Achievement: ${achievement.name}`)
      }

      this.saveProgress(userId, progress)
      return true
    }

    return false
  }

  // Update streak
  updateStreak(userId: string): UserProgress {
    const progress = this.getUserProgress(userId)
    const today = new Date().toDateString()
    const lastLogin = new Date(progress.lastLoginDate).toDateString()

    if (today !== lastLogin) {
      const yesterday = new Date()
      yesterday.setDate(yesterday.getDate() - 1)
      const yesterdayStr = yesterday.toDateString()

      if (lastLogin === yesterdayStr) {
        // Consecutive day
        progress.streak += 1
        
        // Award streak bonuses
        if (progress.streak === 7) {
          this.awardXP(userId, XP_REWARDS.WEEK_STREAK, '7-day streak!')
          this.unlockBadge(userId, 'streak_7')
        } else if (progress.streak === 30) {
          this.awardXP(userId, XP_REWARDS.MONTH_STREAK, '30-day streak!')
          this.unlockBadge(userId, 'streak_30')
        }
      } else {
        // Streak broken
        progress.streak = 1
      }

      progress.lastLoginDate = new Date().toISOString()
      this.awardXP(userId, XP_REWARDS.DAILY_LOGIN, 'Daily login')
      this.saveProgress(userId, progress)
    }

    return progress
  }

  // Get leaderboard
  getLeaderboard(limit: number = 10): LeaderboardEntry[] {
    // In real app, this would fetch from backend
    // For now, return mock data
    return []
  }

  // Save progress
  private saveProgress(userId: string, progress: UserProgress) {
    localStorage.setItem(`${this.storageKey}_${userId}`, JSON.stringify(progress))
  }

  // Level up callback
  private onLevelUp(userId: string, level: number) {
    // Trigger level up animation/notification
    console.log(`🎉 Level up! Now level ${level}`)
    
    // Check for level-based badges
    if (level === 5) this.unlockBadge(userId, 'level_5')
    if (level === 10) this.unlockBadge(userId, 'level_10')
    if (level === 25) this.unlockBadge(userId, 'level_25')
    if (level === 50) this.unlockBadge(userId, 'level_50')
  }

  // Get all badges
  private getAllBadges(): Badge[] {
    return [
      {
        id: 'first_session',
        name: 'First Steps',
        description: 'Complete your first session',
        icon: '🎓',
        rarity: 'common',
        locked: true,
      },
      {
        id: 'streak_7',
        name: 'Week Warrior',
        description: '7-day login streak',
        icon: '🔥',
        rarity: 'rare',
        locked: true,
      },
      {
        id: 'streak_30',
        name: 'Monthly Master',
        description: '30-day login streak',
        icon: '⭐',
        rarity: 'epic',
        locked: true,
      },
      {
        id: 'level_5',
        name: 'Rising Star',
        description: 'Reach level 5',
        icon: '🌟',
        rarity: 'common',
        locked: true,
      },
      {
        id: 'level_10',
        name: 'Dedicated Learner',
        description: 'Reach level 10',
        icon: '💫',
        rarity: 'rare',
        locked: true,
      },
      {
        id: 'level_25',
        name: 'Expert',
        description: 'Reach level 25',
        icon: '👑',
        rarity: 'epic',
        locked: true,
      },
      {
        id: 'level_50',
        name: 'Legend',
        description: 'Reach level 50',
        icon: '💎',
        rarity: 'legendary',
        locked: true,
      },
      {
        id: 'sessions_10',
        name: 'Regular',
        description: 'Complete 10 sessions',
        icon: '📚',
        rarity: 'common',
        locked: true,
      },
      {
        id: 'sessions_50',
        name: 'Enthusiast',
        description: 'Complete 50 sessions',
        icon: '🎯',
        rarity: 'rare',
        locked: true,
      },
      {
        id: 'sessions_100',
        name: 'Champion',
        description: 'Complete 100 sessions',
        icon: '🏆',
        rarity: 'epic',
        locked: true,
      },
    ]
  }

  // Get all achievements
  private getAllAchievements(): Achievement[] {
    return [
      {
        id: 'complete_profile',
        name: 'Profile Complete',
        description: 'Fill out your complete profile',
        progress: 0,
        target: 1,
        reward: 50,
        icon: '👤',
        completed: false,
      },
      {
        id: 'first_favorite',
        name: 'First Favorite',
        description: 'Add your first favorite mentor',
        progress: 0,
        target: 1,
        reward: 20,
        icon: '❤️',
        completed: false,
      },
      {
        id: 'social_butterfly',
        name: 'Social Butterfly',
        description: 'Send 100 chat messages',
        progress: 0,
        target: 100,
        reward: 100,
        icon: '💬',
        completed: false,
      },
      {
        id: 'reaction_master',
        name: 'Reaction Master',
        description: 'Send 50 reactions',
        progress: 0,
        target: 50,
        reward: 50,
        icon: '😊',
        completed: false,
      },
      {
        id: 'early_bird',
        name: 'Early Bird',
        description: 'Join 10 morning sessions',
        progress: 0,
        target: 10,
        reward: 75,
        icon: '🌅',
        completed: false,
      },
      {
        id: 'night_owl',
        name: 'Night Owl',
        description: 'Join 10 evening sessions',
        progress: 0,
        target: 10,
        reward: 75,
        icon: '🌙',
        completed: false,
      },
    ]
  }
}

export const gamificationService = new GamificationService()
export default gamificationService
