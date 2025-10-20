import { Link } from 'react-router-dom'
import { Calendar, Clock, Users, Star } from 'lucide-react'
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { formatDate, formatTime } from '@/lib/utils'
import type { LiveSession } from '@/types'
import { motion } from 'framer-motion'

interface SessionCardProps {
  session: LiveSession
}

export default function SessionCard({ session }: SessionCardProps) {
  const statusColors = {
    SCHEDULED: 'bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-400',
    LIVE: 'bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-400',
    COMPLETED: 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-400',
    CANCELLED: 'bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-400',
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      whileHover={{ y: -5 }}
      transition={{ duration: 0.2 }}
    >
      <Card className="group relative h-full overflow-hidden border-2 transition-all hover:border-primary-200 hover:shadow-xl dark:hover:border-primary-800">
        {/* Thumbnail */}
        <div className="relative h-48 overflow-hidden bg-gradient-to-br from-primary-600 to-accent-500">
          {session.thumbnail && (
            <img
              src={session.thumbnail}
              alt={session.title}
              className="h-full w-full object-cover transition-transform group-hover:scale-110"
            />
          )}
          <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
          
          {/* Status Badge */}
          <div className="absolute right-3 top-3">
            <span className={`rounded-full px-3 py-1 text-xs font-medium ${statusColors[session.status]}`}>
              {session.status}
            </span>
          </div>

          {/* Featured Badge */}
          {session.isFeatured && (
            <div className="absolute left-3 top-3">
              <span className="flex items-center gap-1 rounded-full bg-yellow-400 px-3 py-1 text-xs font-medium text-yellow-900">
                <Star className="h-3 w-3 fill-current" />
                Featured
              </span>
            </div>
          )}
        </div>

        <CardHeader className="pb-3">
          <h3 className="line-clamp-2 text-lg font-semibold group-hover:text-primary-600">
            {session.title}
          </h3>
          <p className="line-clamp-2 text-sm text-muted-foreground">{session.description}</p>
        </CardHeader>

        <CardContent className="space-y-3 pb-3">
          {/* Mentor Info */}
          <div className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-full bg-gradient-to-br from-primary-600 to-accent-500" />
            <div className="flex-1 overflow-hidden">
              <p className="truncate text-sm font-medium">{session.mentorName}</p>
              {session.mentorRating && (
                <div className="flex items-center gap-1">
                  <Star className="h-3 w-3 fill-yellow-400 text-yellow-400" />
                  <span className="text-xs text-muted-foreground">{session.mentorRating}</span>
                </div>
              )}
            </div>
          </div>

          {/* Session Info */}
          <div className="space-y-2 text-sm text-muted-foreground">
            <div className="flex items-center gap-2">
              <Calendar className="h-4 w-4" />
              <span>{formatDate(session.scheduledStartTime)}</span>
            </div>
            <div className="flex items-center gap-2">
              <Clock className="h-4 w-4" />
              <span>{formatTime(session.scheduledStartTime)}</span>
            </div>
            <div className="flex items-center gap-2">
              <Users className="h-4 w-4" />
              <span>
                {session.currentParticipants}/{session.maxParticipants} participants
              </span>
            </div>
          </div>

          {/* Tags */}
          <div className="flex flex-wrap gap-2">
            {session.tags?.slice(0, 3).map((tag) => (
              <span
                key={tag}
                className="rounded-full bg-primary-50 px-2 py-1 text-xs text-primary-600 dark:bg-primary-950 dark:text-primary-400"
              >
                {tag}
              </span>
            ))}
          </div>
        </CardContent>

        <CardFooter className="pt-0">
          <Button asChild className="w-full" variant="gradient">
            <Link to={`/sessions/${session.id}`}>View Details</Link>
          </Button>
        </CardFooter>

        {/* Hover effect overlay */}
        <div className="absolute inset-0 -z-10 bg-gradient-to-br from-primary-50 to-accent-50 opacity-0 transition-opacity group-hover:opacity-100 dark:from-primary-950 dark:to-accent-950" />
      </Card>
    </motion.div>
  )
}
