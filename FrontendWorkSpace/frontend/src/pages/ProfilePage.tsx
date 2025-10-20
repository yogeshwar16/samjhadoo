import { useParams } from 'react-router-dom'
import { Card, CardContent } from '@/components/ui/card'

export default function ProfilePage() {
  const { id } = useParams<{ id: string }>()

  return (
    <div className="space-y-6">
      <Card>
        <CardContent className="p-6">
          <h1 className="text-2xl font-bold">Profile Page</h1>
          <p className="text-muted-foreground">User ID: {id}</p>
        </CardContent>
      </Card>
    </div>
  )
}
