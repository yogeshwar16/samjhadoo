import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Home } from 'lucide-react'

export default function NotFoundPage() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center text-center">
      <h1 className="mb-4 text-6xl font-bold">404</h1>
      <p className="mb-8 text-xl text-muted-foreground">Page not found</p>
      <Button asChild>
        <Link to="/">
          <Home className="mr-2 h-4 w-4" />
          Go Home
        </Link>
      </Button>
    </div>
  )
}
