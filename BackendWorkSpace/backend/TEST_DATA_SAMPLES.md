# Test Data Samples & API Testing Guide

**Project**: Samjhadoo Backend  
**Purpose**: Sample data for testing all features  
**Last Updated**: October 16, 2025

---

## 📋 Table of Contents

1. [Test Users](#test-users)
2. [API Test Collections](#api-test-collections)
3. [Feature Testing Scenarios](#feature-testing-scenarios)
4. [Sample Requests](#sample-requests)

---

## 👥 Test Users

### User Accounts

| Username | Email | Password | Role | Purpose |
|----------|-------|----------|------|---------|
| admin_user | admin@samjhadoo.com | Admin@123 | ADMIN | Admin operations |
| mentor_john | john@mentor.com | Mentor@123 | MENTOR | Verified mentor |
| mentor_sarah | sarah@mentor.com | Mentor@123 | MENTOR | Premium mentor |
| user_alice | alice@user.com | User@123 | USER | Regular user |
| user_bob | bob@user.com | User@123 | USER | Premium user |
| mentor_raj | raj@mentor.com | Mentor@123 | MENTOR | Indian mentor |

---

## 🧪 API Test Collections

### 1. Authentication APIs

#### Register User
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Test@123",
  "name": "Test User",
  "role": "USER"
}
```

#### Login
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "mentor_john",
  "password": "Mentor@123"
}
```

**Response**: Save the JWT token for subsequent requests

---

### 2. Live Sessions APIs

#### Create Live Session
```http
POST http://localhost:8080/api/friendly-talk/live-sessions
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "title": "Night Radio - Chill Vibes",
  "description": "Late night relaxing session with music and stories",
  "type": "NIGHT_RADIO",
  "scheduledStartTime": "2025-10-17T22:00:00",
  "scheduledDurationMinutes": 60,
  "maxParticipants": 50,
  "tags": ["relaxation", "music", "stories"],
  "thumbnailUrl": "https://example.com/thumbnail.jpg",
  "isFeatured": true
}
```

#### Get All Live Sessions
```http
GET http://localhost:8080/api/friendly-talk/live-sessions?status=LIVE
Authorization: Bearer {JWT_TOKEN}
```

#### Join Session
```http
POST http://localhost:8080/api/friendly-talk/live-sessions/{sessionId}/join
Authorization: Bearer {JWT_TOKEN}
```

#### Start Session (Mentor Only)
```http
POST http://localhost:8080/api/friendly-talk/live-sessions/{sessionId}/start
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "meetingUrl": "https://meet.google.com/abc-defg-hij"
}
```

---

### 3. Favorites APIs

#### Add Favorite
```http
POST http://localhost:8080/api/friendly-talk/favorites
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "favoriteUserId": 2,
  "tag": "Career Mentor",
  "notes": "Great advice on tech career",
  "notifyWhenOnline": true
}
```

#### Get My Favorites
```http
GET http://localhost:8080/api/friendly-talk/favorites/my-favorites
Authorization: Bearer {JWT_TOKEN}
```

#### Get Mutual Favorites
```http
GET http://localhost:8080/api/friendly-talk/favorites/mutual
Authorization: Bearer {JWT_TOKEN}
```

#### Remove Favorite
```http
DELETE http://localhost:8080/api/friendly-talk/favorites/{favoriteId}
Authorization: Bearer {JWT_TOKEN}
```

---

### 4. AI Gateway APIs

#### Master AI Request
```http
POST http://localhost:8080/api/ai/master
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "requestType": "CHAT",
  "prompt": "How can I improve my communication skills?",
  "context": "I'm a software engineer looking to lead teams"
}
```

#### Agentic AI Request
```http
POST http://localhost:8080/api/ai/agentic
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "requestType": "MENTOR_MATCH",
  "prompt": "Find me a mentor for career transition to AI/ML",
  "preferences": {
    "expertise": ["AI", "Machine Learning"],
    "experience": "5+ years"
  }
}
```

#### Generate Session Prep
```http
POST http://localhost:8080/api/ai/session-prep
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "menteeQuery": "How to prepare for FAANG interviews?",
  "mentorExpertise": "Software Engineering, System Design",
  "sessionDuration": 60
}
```

---

### 5. Wallet APIs

#### Get Wallet Balance
```http
GET http://localhost:8080/api/wallet/balance
Authorization: Bearer {JWT_TOKEN}
```

#### Add Funds
```http
POST http://localhost:8080/api/wallet/add-funds
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "amount": 1000.00
}
```

#### Transfer Funds
```http
POST http://localhost:8080/api/wallet/transfer
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "recipientId": 2,
  "amount": 100.00,
  "note": "Payment for session"
}
```

---

### 6. Pricing APIs

#### Calculate Session Price
```http
POST http://localhost:8080/api/pricing/calculate
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "mentorId": 2,
  "duration": 60,
  "topicId": 1,
  "sessionType": "VIDEO"
}
```

#### Apply Promo Code
```http
POST http://localhost:8080/api/pricing/apply-promo
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "promoCode": "FIRST50",
  "basePrice": 500.00
}
```

---

### 7. Gamification APIs

#### Get User Points
```http
GET http://localhost:8080/api/gamification/points
Authorization: Bearer {JWT_TOKEN}
```

#### Get User Badges
```http
GET http://localhost:8080/api/gamification/badges
Authorization: Bearer {JWT_TOKEN}
```

#### Get Leaderboard
```http
GET http://localhost:8080/api/gamification/leaderboard?period=WEEKLY
Authorization: Bearer {JWT_TOKEN}
```

---

## 🎯 Feature Testing Scenarios

### Scenario 1: Complete User Journey

1. **Register** → Create new user account
2. **Login** → Get JWT token
3. **Add Funds** → Add ₹1000 to wallet
4. **Browse Sessions** → Get all live sessions
5. **Join Session** → Join a live session
6. **Add Favorite** → Mark mentor as favorite
7. **AI Request** → Get AI-powered recommendations

---

### Scenario 2: Mentor Journey

1. **Login as Mentor** → Get JWT token
2. **Create Session** → Schedule a live session
3. **Start Session** → Go live with meeting URL
4. **View Participants** → Check who joined
5. **End Session** → Complete the session
6. **Check Earnings** → View wallet balance

---

### Scenario 3: Live Session Flow

1. **Mentor creates session** → SCHEDULED status
2. **Users browse sessions** → Discover upcoming sessions
3. **Users join session** → Add to participants list
4. **Mentor starts session** → Status changes to LIVE
5. **WebSocket notifications** → Real-time updates to all users
6. **Session ends** → Status changes to ENDED
7. **Recording available** → Users can access recording

---

### Scenario 4: Favorites & Notifications

1. **User adds mentor to favorites** → Create favorite relationship
2. **Check if mutual** → System detects mutual favorites
3. **Mentor goes online** → Trigger notification
4. **User receives notification** → WebSocket push
5. **Mentor starts session** → Notify all favorites

---

## 📝 Sample Requests by Feature

### Live Sessions - All Types

#### Night Radio Session
```json
{
  "title": "Midnight Melodies - Night Radio",
  "description": "Relaxing music and stories to help you unwind",
  "type": "NIGHT_RADIO",
  "scheduledStartTime": "2025-10-17T23:00:00",
  "scheduledDurationMinutes": 120,
  "maxParticipants": 100,
  "tags": ["relaxation", "music", "night"],
  "isFeatured": true
}
```

#### Night Lori (Lullaby Session)
```json
{
  "title": "Peaceful Dreams - Lori Session",
  "description": "Soothing lullabies and bedtime stories",
  "type": "NIGHT_LORI",
  "scheduledStartTime": "2025-10-17T22:30:00",
  "scheduledDurationMinutes": 45,
  "maxParticipants": 50,
  "tags": ["lullaby", "sleep", "relaxation"]
}
```

#### Motivation Talk
```json
{
  "title": "Rise and Shine - Morning Motivation",
  "description": "Start your day with positive energy",
  "type": "LIVE_MOTIVATION_TALK",
  "scheduledStartTime": "2025-10-17T06:00:00",
  "scheduledDurationMinutes": 30,
  "maxParticipants": 200,
  "tags": ["motivation", "morning", "inspiration"],
  "isFeatured": true
}
```

#### Topic Session - Career
```json
{
  "title": "Breaking into Tech - Career Guidance",
  "description": "How to transition into software engineering",
  "type": "LIVE_TOPIC_SESSION",
  "scheduledStartTime": "2025-10-17T19:00:00",
  "scheduledDurationMinutes": 60,
  "maxParticipants": 30,
  "tags": ["career", "tech", "guidance"]
}
```

#### Love & Life Tips
```json
{
  "title": "Relationship Wisdom - Love & Life",
  "description": "Navigate relationships with confidence",
  "type": "LOVE_LIFE_TIPS",
  "scheduledStartTime": "2025-10-17T20:00:00",
  "scheduledDurationMinutes": 45,
  "maxParticipants": 50,
  "tags": ["relationships", "love", "life"]
}
```

---

### Favorites - Different Tags

#### Career Mentor
```json
{
  "favoriteUserId": 2,
  "tag": "Career Mentor",
  "notes": "Excellent guidance on tech career paths",
  "notifyWhenOnline": true
}
```

#### Calm Vibes
```json
{
  "favoriteUserId": 3,
  "tag": "Calm Vibes",
  "notes": "Always helps me relax during stressful times",
  "notifyWhenOnline": true
}
```

#### Always Replies
```json
{
  "favoriteUserId": 4,
  "tag": "Always Replies",
  "notes": "Very responsive and helpful",
  "notifyWhenOnline": false
}
```

---

### AI Requests - Different Types

#### Career Advice
```json
{
  "requestType": "CHAT",
  "prompt": "I'm a mechanical engineer wanting to switch to software development. What steps should I take?",
  "context": "5 years experience in automotive industry"
}
```

#### Mentor Matching
```json
{
  "requestType": "MENTOR_MATCH",
  "prompt": "Find me a mentor for learning data science",
  "preferences": {
    "expertise": ["Data Science", "Python", "Machine Learning"],
    "availability": "weekends",
    "language": "English"
  }
}
```

#### Session Prep
```json
{
  "menteeQuery": "How to negotiate salary for a senior developer role?",
  "mentorExpertise": "Career Coaching, Tech Leadership",
  "sessionDuration": 45,
  "menteeBackground": "7 years experience, currently at mid-level"
}
```

---

## 🔧 WebSocket Testing

### Connect to WebSocket

```javascript
// JavaScript example
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': 'Bearer ' + jwtToken
}, function(frame) {
  console.log('Connected: ' + frame);
  
  // Subscribe to session updates
  stompClient.subscribe('/topic/sessions', function(message) {
    console.log('Session update:', JSON.parse(message.body));
  });
  
  // Subscribe to personal notifications
  stompClient.subscribe('/user/queue/notifications', function(message) {
    console.log('Notification:', JSON.parse(message.body));
  });
});
```

---

## 📊 Expected Responses

### Successful Live Session Creation
```json
{
  "id": 1,
  "title": "Night Radio - Chill Vibes",
  "mentorId": 2,
  "mentorName": "John Mentor",
  "status": "SCHEDULED",
  "type": "NIGHT_RADIO",
  "scheduledStartTime": "2025-10-17T22:00:00",
  "maxParticipants": 50,
  "currentParticipants": 0,
  "tags": ["relaxation", "music", "stories"],
  "isFeatured": true,
  "canJoin": false
}
```

### Successful Favorite Addition
```json
{
  "id": 1,
  "userId": 4,
  "favoriteUserId": 2,
  "favoriteUserName": "John Mentor",
  "tag": "Career Mentor",
  "notes": "Great advice on tech career",
  "notifyWhenOnline": true,
  "isMutual": false,
  "createdAt": "2025-10-16T10:30:00"
}
```

---

## 🚨 Error Responses

### Insufficient Funds
```json
{
  "timestamp": "2025-10-16T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient funds in wallet",
  "path": "/api/wallet/transfer"
}
```

### Session Full
```json
{
  "timestamp": "2025-10-16T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Session is full. Maximum participants reached.",
  "path": "/api/friendly-talk/live-sessions/1/join"
}
```

### Rate Limit Exceeded
```json
{
  "timestamp": "2025-10-16T10:30:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "AI request rate limit exceeded. Try again later.",
  "path": "/api/ai/master"
}
```

---

## 📦 Postman Collection

Import this collection into Postman for easy testing:

**File**: `samjhadoo-api-tests.postman_collection.json` (see separate file)

---

## ✅ Testing Checklist

### Authentication
- [ ] Register new user
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] Access protected endpoint without token
- [ ] Access protected endpoint with expired token

### Live Sessions
- [ ] Create session as mentor
- [ ] Get all sessions
- [ ] Filter sessions by status
- [ ] Filter sessions by type
- [ ] Join session as user
- [ ] Start session as mentor
- [ ] End session as mentor
- [ ] Leave session as user
- [ ] Try joining full session

### Favorites
- [ ] Add favorite
- [ ] Get my favorites
- [ ] Get mutual favorites
- [ ] Update favorite tag
- [ ] Remove favorite
- [ ] Check notification preferences

### AI Gateway
- [ ] Send master AI request
- [ ] Send agentic AI request
- [ ] Generate session prep
- [ ] Check rate limits
- [ ] Verify token usage tracking

### Wallet
- [ ] Get balance
- [ ] Add funds
- [ ] Transfer funds
- [ ] Check insufficient funds error
- [ ] View transaction history

---

**Next**: See `sample_data.sql` for database population scripts.
