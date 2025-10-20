# Samjhadoo Frontend Setup Guide

## 🎉 Quick Start

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Configure Environment

Create a `.env` file:
```bash
cp .env.example .env
```

Update the API URL if needed:
```
VITE_API_BASE_URL=http://localhost:8080/api
```

### 3. Start Development Server

```bash
npm run dev
```

The application will be available at `http://localhost:3000`

## 📦 What's Included

### ✅ Core Features Implemented

1. **Authentication System**
   - Login page with form validation
   - Registration page with role selection
   - JWT token management
   - Protected routes

2. **Session Discovery**
   - Beautiful 3D session cards with hover effects
   - Advanced filtering (type, status, difficulty)
   - Search functionality
   - Responsive grid layout

3. **Layout Components**
   - Responsive header with theme toggle
   - Collapsible sidebar navigation
   - Mobile-friendly design
   - Glassmorphism effects

4. **Pages**
   - Home page with hero section and features
   - Sessions listing with filters
   - Session detail view
   - Favorites management
   - User dashboard with analytics
   - Profile pages

5. **UI Components**
   - Button with multiple variants
   - Card components
   - Input fields
   - Toast notifications
   - Theme provider (light/dark mode)

6. **State Management**
   - Zustand for auth state
   - React Query for server state
   - Persistent storage

7. **API Integration**
   - Axios client with interceptors
   - JWT token handling
   - Error handling
   - Type-safe endpoints

## 🎨 Design System

### Color Palette
- **Primary Blue**: #2563EB (Trust, professionalism)
- **Secondary Orange**: #F59E0B (Energy, enthusiasm)
- **Accent Teal**: #0D9488 (Growth, learning)

### Typography
- **Headings**: Poppins SemiBold
- **Body**: Inter Regular

### Effects
- Glassmorphism (frosted glass)
- Neumorphism (soft shadows)
- Smooth animations with Framer Motion
- Gradient backgrounds

## 🛠 Development Commands

```bash
# Start dev server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint

# Format code
npm run format
```

## 📁 Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── layout/          # Header, Sidebar, MainLayout
│   │   ├── providers/       # ThemeProvider
│   │   ├── sessions/        # SessionCard, SessionFilters
│   │   └── ui/              # Button, Card, Input, Toast
│   ├── pages/
│   │   ├── auth/            # Login, Register
│   │   ├── HomePage.tsx
│   │   ├── SessionsPage.tsx
│   │   ├── SessionDetailPage.tsx
│   │   ├── FavoritesPage.tsx
│   │   ├── ProfilePage.tsx
│   │   ├── DashboardPage.tsx
│   │   └── NotFoundPage.tsx
│   ├── hooks/               # use-toast
│   ├── lib/                 # utils.ts
│   ├── services/            # api.ts
│   ├── store/               # authStore.ts
│   ├── types/               # index.ts
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css
├── public/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.js
└── README.md
```

## 🔗 API Endpoints Used

- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `GET /api/live-sessions` - Get sessions list
- `GET /api/live-sessions/:id` - Get session details
- `POST /api/live-sessions` - Create session
- `POST /api/live-sessions/:id/join` - Join session
- `GET /api/favorites` - Get favorites
- `POST /api/favorites` - Add favorite
- `DELETE /api/favorites/:id` - Remove favorite

## 🎯 Next Steps

### Phase 2 Features to Implement

1. **Advanced Session Features**
   - WebRTC video integration
   - Live chat during sessions
   - Screen sharing
   - Recording playback

2. **AI-Powered Features**
   - Smart session recommendations
   - Voice search
   - Real-time translation
   - Emotion detection

3. **Community Features**
   - User profiles with badges
   - Reviews and ratings
   - Discussion forums
   - Mentor matching algorithm

4. **Monetization**
   - Wallet integration
   - Payment processing
   - Subscription plans
   - Gift cards

5. **Analytics**
   - Learning progress tracking
   - Session analytics
   - Engagement metrics
   - Custom reports

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Kill process on port 3000
npx kill-port 3000
```

### Module Not Found
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Build Errors
```bash
# Clear Vite cache
rm -rf node_modules/.vite
npm run dev
```

## 📝 Notes

- Make sure the backend is running on `http://localhost:8080`
- The app uses JWT tokens stored in localStorage
- Dark mode preference is saved in localStorage
- All API calls include automatic token injection

## 🚀 Deployment

For production deployment:

1. Build the app:
```bash
npm run build
```

2. The `dist` folder contains the production build

3. Deploy to:
   - Vercel: `vercel deploy`
   - Netlify: `netlify deploy`
   - AWS S3 + CloudFront
   - Any static hosting service

## 📞 Support

For issues or questions, refer to:
- Backend API documentation
- Enhanced Web Roadmap
- Project specifications
