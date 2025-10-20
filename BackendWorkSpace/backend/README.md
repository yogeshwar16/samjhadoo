# Fullstack Application

This is a fullstack application with:
- Backend: Spring Boot 3.2.0
- Mobile: React Native (Android & iOS)

## Project Structure

- `backend/` - Spring Boot application
- `mobile/` - React Native mobile application

## Prerequisites

- Java 17+
- Node.js 18+
- Android Studio (for Android development)
- Xcode (for iOS development, macOS only)
- Maven 3.8+

## Getting Started

### Backend

```bash
cd backend
mvn spring-boot:run
```

The backend will be available at `http://localhost:8080`

### Mobile App

```bash
cd mobile
npm install

# For Android
npx react-native run-android

# For iOS (macOS only)
cd ios && pod install && cd ..
npx react-native run-ios
```

## API Documentation

API documentation will be available at `http://localhost:8080/swagger-ui.html` after starting the backend.
