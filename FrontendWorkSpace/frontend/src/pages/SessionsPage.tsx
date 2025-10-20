import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Filter, Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import SessionCard from '@/components/sessions/SessionCard'
import SessionFilters from '@/components/sessions/SessionFilters'
import api from '@/services/api'
import type { SessionFilter } from '@/types'

export default function SessionsPage() {
  const [filters, setFilters] = useState<SessionFilter>({})
  const [showFilters, setShowFilters] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')

  const { data: sessions, isLoading } = useQuery({
    queryKey: ['sessions', filters],
    queryFn: () => api.getSessions(filters),
  })

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold">Discover Sessions</h1>
          <p className="text-muted-foreground">Find the perfect learning experience</p>
        </div>
        <Button variant="gradient">Create Session</Button>
      </div>

      {/* Search and Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="flex gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Search sessions..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10"
              />
            </div>
            <Button
              variant="outline"
              onClick={() => setShowFilters(!showFilters)}
              className="gap-2"
            >
              <Filter className="h-4 w-4" />
              Filters
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Filters Panel */}
      {showFilters && (
        <SessionFilters filters={filters} onFiltersChange={setFilters} />
      )}

      {/* Sessions Grid */}
      {isLoading ? (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {[...Array(6)].map((_, i) => (
            <Card key={i} className="h-96 animate-pulse">
              <CardHeader>
                <div className="h-4 w-3/4 rounded bg-gray-200 dark:bg-gray-700" />
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="h-3 w-full rounded bg-gray-200 dark:bg-gray-700" />
                  <div className="h-3 w-5/6 rounded bg-gray-200 dark:bg-gray-700" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : sessions?.content?.length > 0 ? (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {sessions.content.map((session: any) => (
            <SessionCard key={session.id} session={session} />
          ))}
        </div>
      ) : (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-16">
            <p className="text-lg text-muted-foreground">No sessions found</p>
            <p className="text-sm text-muted-foreground">Try adjusting your filters</p>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
