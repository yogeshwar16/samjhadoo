# Postman Collection Setup Guide

**Project**: Samjhadoo Backend API Testing  
**Last Updated**: October 16, 2025

---

## 📋 Quick Setup

### 1. Import Environment Variables

Create a new environment in Postman with these variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `base_url` | `http://localhost:8080` | |
| `jwt_token` | | (Auto-set after login) |
| `user_id` | | (Auto-set after login) |
| `session_id` | | (Set manually for testing) |
| `favorite_id` | | (Set manually for testing) |

---

## 🔐 Authentication Flow

### Step 1: Register User (Optional)
```
POST {{base_url}}/api/auth/register
```

**Body**:
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Test@123",
  "name": "Test User",
  "role": "USER"
}
```

### Step 2: Login
```
POST {{base_url}}/api/auth/login
```

**Body**:
```json
{
  "username": "mentor_john",
  "password": "Test@123"
}
```

**Tests Script** (Auto-save token):
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("jwt_token", jsonData.token);
    pm.environment.set("user_id", jsonData.userId);
}
```

---

## 📚 API Collections

### Collection 1: Live Sessions

#### 1.1 Get All Sessions
```
GET {{base_url}}/api/friendly-talk/live-sessions
Authorization: Bearer {{jwt_token}}
```

#### 1.2 Get Live Sessions Only
```
GET {{base_url}}/api/friendly-talk/live-sessions?status=LIVE
Authorization: Bearer {{jwt_token}}
```

#### 1.3 Get Sessions by Type
```
GET {{base_url}}/api/friendly-talk/live-sessions?type=NIGHT_RADIO
Authorization: Bearer {{jwt_token}}
```

#### 1.4 Create Session (Mentor Only)
```
POST {{base_url}}/api/friendly-talk/live-sessions
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body**:
```json
{
  "title": "Night Radio - Chill Vibes",
  "description": "Late night relaxing session",
  "type": "NIGHT_RADIO",
  "scheduledStartTime": "2025-10-17T22:00:00",
  "scheduledDurationMinutes": 60,
  "maxParticipants": 50,
  "tags": ["relaxation", "music"],
  "isFeatured": true
}
```

#### 1.5 Join Session
```
POST {{base_url}}/api/friendly-talk/live-sessions/{{session_id}}/join
Authorization: Bearer {{jwt_token}}
```

#### 1.6 Start Session (Mentor Only)
```
POST {{base_url}}/api/friendly-talk/live-sessions/{{session_id}}/start
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body**:
```json
{
  "meetingUrl": "https://meet.google.com/abc-defg-hij"
}
```

---

### Collection 2: Favorites

#### 2.1 Add Favorite
```
POST {{base_url}}/api/friendly-talk/favorites
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body**:
```json
{
  "favoriteUserId": 2,
  "tag": "Career Mentor",
  "notes": "Great advice on tech career",
  "notifyWhenOnline": true
}
```

#### 2.2 Get My Favorites
```
GET {{base_url}}/api/friendly-talk/favorites/my-favorites
Authorization: Bearer {{jwt_token}}
```

#### 2.3 Get Mutual Favorites
```
GET {{base_url}}/api/friendly-talk/favorites/mutual
Authorization: Bearer {{jwt_token}}
```

#### 2.4 Remove Favorite
```
DELETE {{base_url}}/api/friendly-talk/favorites/{{favorite_id}}
Authorization: Bearer {{jwt_token}}
```

---

### Collection 3: AI Gateway

#### 3.1 Master AI Request
```
POST {{base_url}}/api/ai/master
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body**:
```json
{
  "requestType": "CHAT",
  "prompt": "How can I improve my communication skills?",
  "context": "Software engineer looking to lead teams"
}
```

#### 3.2 Agentic AI Request
```
POST {{base_url}}/api/ai/agentic
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body**:
```json
{
  "requestType": "MENTOR_MATCH",
  "prompt": "Find me a mentor for career transition to AI/ML"
}
```

---

### Collection 4: Wallet

#### 4.1 Get Balance
```
GET {{base_url}}/api/wallet/balance
Authorization: Bearer {{jwt_token}}
```

#### 4.2 Add Funds
```
POST {{base_url}}/api/wallet/add-funds
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body**:
```json
{
  "amount": 1000.00
}
```

#### 4.3 Transfer Funds
```
POST {{base_url}}/api/wallet/transfer
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body**:
```json
{
  "recipientId": 2,
  "amount": 100.00,
  "note": "Payment for session"
}
```

---

## 🧪 Test Scenarios

### Scenario 1: Complete User Flow
1. Login as user
2. Get all live sessions
3. Join a session
4. Add mentor to favorites
5. Check wallet balance

### Scenario 2: Mentor Flow
1. Login as mentor
2. Create new session
3. Start session
4. View participants
5. End session

---

## ✅ Pre-request Scripts

Add to collection level:

```javascript
// Auto-refresh token if expired
if (pm.environment.get("jwt_token")) {
    // Add token validation logic
}
```

---

## 📊 Test Scripts

Add to collection level:

```javascript
// Validate response
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response time is less than 500ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

pm.test("Response has valid JSON", function () {
    pm.response.to.be.json;
});
```

---

## 🔧 How to Load Sample Data

1. Start PostgreSQL
2. Connect to database:
   ```bash
   psql -h localhost -U samjhadoo_user -d samjhadoo_dev
   ```
3. Run sample data script:
   ```bash
   \i backend/src/main/resources/sample_data.sql
   ```
4. Verify data:
   ```sql
   SELECT COUNT(*) FROM users;
   SELECT COUNT(*) FROM live_sessions;
   ```

---

## 📝 Notes

- All passwords in sample data: `Test@123`
- JWT tokens expire after 24 hours
- Use mentor accounts to create/manage sessions
- Use user accounts to join sessions and add favorites

---

**Ready to test!** Import the collection and start testing your APIs.
