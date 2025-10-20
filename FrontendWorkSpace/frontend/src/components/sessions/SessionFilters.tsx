import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import type { SessionFilter } from '@/types'

interface SessionFiltersProps {
  filters: SessionFilter
  onFiltersChange: (filters: SessionFilter) => void
}

const sessionTypes = ['ONE_ON_ONE', 'GROUP', 'WEBINAR']
const sessionStatuses = ['SCHEDULED', 'LIVE', 'COMPLETED']
const difficulties = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED']

export default function SessionFilters({ filters, onFiltersChange }: SessionFiltersProps) {
  const handleTypeToggle = (type: string) => {
    const currentTypes = filters.type ? [filters.type] : []
    const newType = currentTypes.includes(type as any) ? undefined : (type as any)
    onFiltersChange({ ...filters, type: newType })
  }

  const handleStatusToggle = (status: string) => {
    const newStatus = filters.status === status ? undefined : (status as any)
    onFiltersChange({ ...filters, status: newStatus })
  }

  const handleDifficultyToggle = (difficulty: string) => {
    const newDifficulty = filters.difficulty === difficulty ? undefined : (difficulty as any)
    onFiltersChange({ ...filters, difficulty: newDifficulty })
  }

  const clearFilters = () => {
    onFiltersChange({})
  }

  return (
    <Card>
      <CardContent className="p-6">
        <div className="space-y-6">
          {/* Session Type */}
          <div>
            <h3 className="mb-3 text-sm font-medium">Session Type</h3>
            <div className="flex flex-wrap gap-2">
              {sessionTypes.map((type) => (
                <Button
                  key={type}
                  variant={filters.type === type ? 'default' : 'outline'}
                  size="sm"
                  onClick={() => handleTypeToggle(type)}
                >
                  {type.replace('_', ' ')}
                </Button>
              ))}
            </div>
          </div>

          {/* Status */}
          <div>
            <h3 className="mb-3 text-sm font-medium">Status</h3>
            <div className="flex flex-wrap gap-2">
              {sessionStatuses.map((status) => (
                <Button
                  key={status}
                  variant={filters.status === status ? 'default' : 'outline'}
                  size="sm"
                  onClick={() => handleStatusToggle(status)}
                >
                  {status}
                </Button>
              ))}
            </div>
          </div>

          {/* Difficulty */}
          <div>
            <h3 className="mb-3 text-sm font-medium">Difficulty</h3>
            <div className="flex flex-wrap gap-2">
              {difficulties.map((difficulty) => (
                <Button
                  key={difficulty}
                  variant={filters.difficulty === difficulty ? 'default' : 'outline'}
                  size="sm"
                  onClick={() => handleDifficultyToggle(difficulty)}
                >
                  {difficulty}
                </Button>
              ))}
            </div>
          </div>

          {/* Clear Filters */}
          <Button variant="ghost" onClick={clearFilters} className="w-full">
            Clear All Filters
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}
