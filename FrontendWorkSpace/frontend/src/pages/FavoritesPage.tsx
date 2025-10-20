import { useQuery } from '@tanstack/react-query'
import { Heart } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import api from '@/services/api'

export default function FavoritesPage() {
  const { data: favorites, isLoading } = useQuery({
    queryKey: ['favorites'],
    queryFn: () => api.getFavorites(),
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Favorites</h1>
        <p className="text-muted-foreground">Your saved mentors and sessions</p>
      </div>

      {isLoading ? (
        <div>Loading...</div>
      ) : favorites?.length > 0 ? (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {favorites.map((favorite: any) => (
            <Card key={favorite.id}>
              <CardContent className="p-6">
                <div className="flex items-center gap-3">
                  <div className="h-12 w-12 rounded-full bg-gradient-to-br from-primary-600 to-accent-500" />
                  <div className="flex-1">
                    <p className="font-semibold">{favorite.mentor.name}</p>
                    <p className="text-sm text-muted-foreground">{favorite.mentor.expertise?.[0]}</p>
                  </div>
                  <Heart className="h-5 w-5 fill-red-500 text-red-500" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-16">
            <Heart className="mb-4 h-12 w-12 text-muted-foreground" />
            <p className="text-lg text-muted-foreground">No favorites yet</p>
            <p className="text-sm text-muted-foreground">Start adding mentors to your favorites</p>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
