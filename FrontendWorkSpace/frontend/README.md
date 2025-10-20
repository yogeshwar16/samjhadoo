# Samjhadoo Web Application

Modern React web application for Samjhadoo - Bridging Minds, Building Futures.

## 🚀 Features

- **Modern UI/UX**: Built with React, TypeScript, and TailwindCSS
- **Glassmorphism Design**: Beautiful frosted glass effects
- **Smooth Animations**: Framer Motion for delightful interactions
- **Dark Mode**: Seamless theme switching
- **Responsive**: Mobile-first design approach
- **Type-Safe**: Full TypeScript support
- **State Management**: Zustand for efficient state handling
- **API Integration**: React Query for data fetching
- **Component Library**: Radix UI primitives

## 📦 Tech Stack

- **Framework**: React 18 + Vite
- **Language**: TypeScript
- **Styling**: TailwindCSS
- **UI Components**: Radix UI
- **Animations**: Framer Motion
- **State Management**: Zustand
- **Data Fetching**: TanStack Query (React Query)
- **Routing**: React Router v6
- **Icons**: Lucide React
- **Form Handling**: React Hook Form + Zod

## 🛠 Getting Started

### Prerequisites

- Node.js 18+ and npm/yarn/pnpm

### Installation

1. Install dependencies:
```bash
npm install
```

2. Create environment file:
```bash
cp .env.example .env
```

3. Update `.env` with your API URL:
```
VITE_API_BASE_URL=http://localhost:8080/api
```

### Development

Start the development server:
```bash
npm run dev
```

The app will be available at `http://localhost:3000`

### Build

Build for production:
```bash
npm run build
```

Preview production build:
```bash
npm run preview
```

## 📁 Project Structure

```
src/
├── components/       # Reusable UI components
│   ├── layout/      # Layout components (Header, Sidebar)
│   ├── providers/   # Context providers
│   ├── sessions/    # Session-related components
│   └── ui/          # Base UI components
├── pages/           # Page components
│   └── auth/        # Authentication pages
├── hooks/           # Custom React hooks
├── lib/             # Utility functions
├── services/        # API services
├── store/           # State management
├── types/           # TypeScript types
└── assets/          # Static assets

## 🎨 Design System

### Colors
- **Primary**: Blue (#2563EB) - Trust and professionalism
- **Secondary**: Orange (#F59E0B) - Energy and enthusiasm
- **Accent**: Teal (#0D9488) - Growth and learning

### Typography
- **Headings**: Poppins (SemiBold 600)
- **Body**: Inter (Regular 400)

### Components
All components follow the design system with consistent spacing, colors, and animations.

## 🔗 API Integration

The app connects to the backend API at `http://localhost:8080/api` by default.

Key endpoints:
- `/auth/login` - User authentication
- `/auth/register` - User registration
- `/live-sessions` - Session management
- `/favorites` - Favorites management
- `/users` - User profiles

## 🌐 Environment Variables

- `VITE_API_BASE_URL` - Backend API base URL

## 📝 Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run format` - Format code with Prettier

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Run linting and formatting
4. Submit a pull request

## 📄 License

Copyright © 2025 Samjhadoo
