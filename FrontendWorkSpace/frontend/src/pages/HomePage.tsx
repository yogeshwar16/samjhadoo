import { Link } from 'react-router-dom'
import { ArrowRight, Sparkles, Users, Video, Zap } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { motion } from 'framer-motion'

const features = [
  {
    icon: Video,
    title: 'Live Sessions',
    description: 'Connect with mentors in real-time through immersive video sessions',
    color: 'from-blue-500 to-cyan-500',
  },
  {
    icon: Sparkles,
    title: 'AI-Powered Matching',
    description: 'Smart algorithms find the perfect mentor for your learning goals',
    color: 'from-purple-500 to-pink-500',
  },
  {
    icon: Users,
    title: 'Global Community',
    description: 'Join thousands of learners and mentors from around the world',
    color: 'from-orange-500 to-red-500',
  },
  {
    icon: Zap,
    title: 'Instant Access',
    description: 'Start learning immediately with on-demand and scheduled sessions',
    color: 'from-green-500 to-emerald-500',
  },
]

const container = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
    },
  },
}

const item = {
  hidden: { opacity: 0, y: 20 },
  show: { opacity: 1, y: 0 },
}

export default function HomePage() {
  return (
    <div className="space-y-16">
      {/* Hero Section */}
      <motion.section
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="text-center"
      >
        <div className="mx-auto max-w-3xl space-y-6">
          <motion.div
            initial={{ scale: 0.9 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.5 }}
            className="inline-flex items-center gap-2 rounded-full bg-primary-50 px-4 py-2 text-sm font-medium text-primary-600 dark:bg-primary-950 dark:text-primary-400"
          >
            <Sparkles className="h-4 w-4" />
            Bridging Minds, Building Futures
          </motion.div>
          
          <h1 className="text-5xl font-bold tracking-tight sm:text-6xl lg:text-7xl">
            Learn from the{' '}
            <span className="gradient-text">Best Mentors</span>
            <br />
            Anywhere, Anytime
          </h1>
          
          <p className="text-xl text-muted-foreground">
            Connect with expert mentors for personalized learning experiences.
            Live sessions, AI-powered matching, and a global community.
          </p>
          
          <div className="flex flex-col gap-4 sm:flex-row sm:justify-center">
            <Button size="lg" variant="gradient" asChild className="group">
              <Link to="/sessions">
                Explore Sessions
                <ArrowRight className="ml-2 h-4 w-4 transition-transform group-hover:translate-x-1" />
              </Link>
            </Button>
            <Button size="lg" variant="outline" asChild>
              <Link to="/register">Become a Mentor</Link>
            </Button>
          </div>
        </div>
      </motion.section>

      {/* Features Grid */}
      <motion.section
        variants={container}
        initial="hidden"
        animate="show"
        className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4"
      >
        {features.map((feature) => (
          <motion.div key={feature.title} variants={item}>
            <Card className="group relative overflow-hidden border-2 transition-all hover:border-primary-200 hover:shadow-lg dark:hover:border-primary-800">
              <CardContent className="p-6">
                <div
                  className={`mb-4 inline-flex h-12 w-12 items-center justify-center rounded-lg bg-gradient-to-br ${feature.color} text-white shadow-lg`}
                >
                  <feature.icon className="h-6 w-6" />
                </div>
                <h3 className="mb-2 text-lg font-semibold">{feature.title}</h3>
                <p className="text-sm text-muted-foreground">{feature.description}</p>
              </CardContent>
              <div className="absolute inset-0 -z-10 bg-gradient-to-br from-primary-50 to-accent-50 opacity-0 transition-opacity group-hover:opacity-100 dark:from-primary-950 dark:to-accent-950" />
            </Card>
          </motion.div>
        ))}
      </motion.section>

      {/* Stats Section */}
      <section className="rounded-2xl bg-gradient-to-br from-primary-600 to-accent-500 p-8 text-white shadow-2xl">
        <div className="grid gap-8 sm:grid-cols-3">
          <div className="text-center">
            <div className="text-4xl font-bold">10K+</div>
            <div className="mt-2 text-primary-100">Active Users</div>
          </div>
          <div className="text-center">
            <div className="text-4xl font-bold">500+</div>
            <div className="mt-2 text-primary-100">Expert Mentors</div>
          </div>
          <div className="text-center">
            <div className="text-4xl font-bold">50K+</div>
            <div className="mt-2 text-primary-100">Sessions Completed</div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="text-center">
        <Card className="glass border-2">
          <CardContent className="p-12">
            <h2 className="mb-4 text-3xl font-bold">Ready to Start Learning?</h2>
            <p className="mb-6 text-lg text-muted-foreground">
              Join thousands of learners and mentors on Samjhadoo today
            </p>
            <Button size="lg" variant="gradient" asChild>
              <Link to="/register">Get Started Free</Link>
            </Button>
          </CardContent>
        </Card>
      </section>
    </div>
  )
}
