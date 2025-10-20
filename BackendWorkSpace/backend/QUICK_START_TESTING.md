# Quick Start Testing Guide

**Project**: Samjhadoo Backend  
**Purpose**: Get up and running in 10 minutes  
**Last Updated**: October 16, 2025

---

## ⚡ 10-Minute Quick Start

### Step 1: Start Services (2 minutes)

```bash
# Start PostgreSQL
sudo systemctl start postgresql

# Start Redis
redis-server

# Start RabbitMQ
sudo systemctl start rabbitmq-server

# OR use Docker Compose (recommended)
cd samjhadoo
docker-compose up -d
```

---

### Step 2: Load Sample Data (1 minute)

```bash
# Connect to PostgreSQL
psql -h localhost -U samjhadoo_user -d samjhadoo_dev

# Run sample data script
\i backend/src/main/resources/sample_data.sql

# Verify
SELECT COUNT(*) FROM users;
# Should return: 8

\q
```

---

### Step 3: Start Backend (2 minutes)

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Wait for**: `Started SamjhadooApplication in X seconds`

---

### Step 4: Verify APIs (5 minutes)

#### Test 1: Health Check
```bash
curl http://localhost:8080/actuator/health
```

**Expected**: `{"status":"UP"}`

---

#### Test 2: Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mentor_john",
    "password": "Test@123"
  }'
```

**Expected**: JSON with `token` field

**Save the token**: `export JWT_TOKEN="your-token-here"`

---

#### Test 3: Get Live Sessions
```bash
curl http://localhost:8080/api/friendly-talk/live-sessions \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Array of live sessions

---

#### Test 4: Get Favorites
```bash
curl http://localhost:8080/api/friendly-talk/favorites/my-favorites \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Array of favorites

---

## 🎯 Test All Features

### Feature 1: Live Sessions ✅

```bash
# 1. Get all sessions
curl http://localhost:8080/api/friendly-talk/live-sessions \
  -H "Authorization: Bearer $JWT_TOKEN"

# 2. Get only LIVE sessions
curl "http://localhost:8080/api/friendly-talk/live-sessions?status=LIVE" \
  -H "Authorization: Bearer $JWT_TOKEN"

# 3. Join a session (use session_id from step 1)
curl -X POST http://localhost:8080/api/friendly-talk/live-sessions/1/join \
  -H "Authorization: Bearer $JWT_TOKEN"

# 4. Get featured sessions
curl "http://localhost:8080/api/friendly-talk/live-sessions?featured=true" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

### Feature 2: Favorites ✅

```bash
# 1. Add a favorite
curl -X POST http://localhost:8080/api/friendly-talk/favorites \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "favoriteUserId": 2,
    "tag": "Career Mentor",
    "notes": "Great advice",
    "notifyWhenOnline": true
  }'

# 2. Get my favorites
curl http://localhost:8080/api/friendly-talk/favorites/my-favorites \
  -H "Authorization: Bearer $JWT_TOKEN"

# 3. Get mutual favorites
curl http://localhost:8080/api/friendly-talk/favorites/mutual \
  -H "Authorization: Bearer $JWT_TOKEN"

# 4. Check if favorite
curl "http://localhost:8080/api/friendly-talk/favorites/is-favorite?userId=2" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

### Feature 3: AI Gateway ✅

```bash
# 1. Master AI request
curl -X POST http://localhost:8080/api/ai/master \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "requestType": "CHAT",
    "prompt": "How can I improve my communication skills?"
  }'

# 2. Check rate limit
curl http://localhost:8080/api/ai/rate-limit/MASTER \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

### Feature 4: Wallet ✅

```bash
# 1. Get balance
curl http://localhost:8080/api/wallet/balance \
  -H "Authorization: Bearer $JWT_TOKEN"

# 2. Add funds
curl -X POST http://localhost:8080/api/wallet/add-funds \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount": 500.00}'

# 3. Check balance again
curl http://localhost:8080/api/wallet/balance \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 👥 Test Users

| Username | Password | Role | Use Case |
|----------|----------|------|----------|
| `mentor_john` | `Test@123` | MENTOR | Create/manage sessions |
| `mentor_sarah` | `Test@123` | MENTOR | Life coaching sessions |
| `user_alice` | `Test@123` | USER | Join sessions, add favorites |
| `user_bob` | `Test@123` | USER | Regular user testing |
| `admin_user` | `Test@123` | ADMIN | Admin operations |

---

## 🔍 Verify Sample Data

### Check Users
```sql
SELECT id, username, name, role FROM users;
```

### Check Live Sessions
```sql
SELECT id, title, status, type, current_participants, max_participants 
FROM live_sessions 
ORDER BY created_at DESC;
```

### Check Favorites
```sql
SELECT f.id, u1.name as user_name, u2.name as favorite_name, f.tag, f.is_mutual
FROM favorites f
JOIN users u1 ON f.user_id = u1.id
JOIN users u2 ON f.favorite_user_id = u2.id;
```

### Check Wallets
```sql
SELECT u.name, w.balance, w.currency 
FROM wallets w 
JOIN users u ON w.user_id = u.id
ORDER BY w.balance DESC;
```

---

## 🌐 Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **Backend API** | http://localhost:8080 | - |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **Actuator** | http://localhost:8080/actuator | - |
| **RabbitMQ Management** | http://localhost:15672 | guest/guest |
| **PostgreSQL** | localhost:5432 | samjhadoo_user/password |
| **Redis** | localhost:6379 | - |

---

## 🐛 Common Issues

### Issue 1: Database Connection Failed
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Check connection
psql -h localhost -U samjhadoo_user -d samjhadoo_dev
```

### Issue 2: Port 8080 Already in Use
```bash
# Find process
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use different port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Issue 3: Redis Not Running
```bash
# Start Redis
redis-server

# Test connection
redis-cli ping
# Should return: PONG
```

### Issue 4: Sample Data Not Loading
```bash
# Check if tables exist
psql -h localhost -U samjhadoo_user -d samjhadoo_dev -c "\dt"

# If tables don't exist, run the app first to create them
mvn spring-boot:run

# Then load sample data
psql -h localhost -U samjhadoo_user -d samjhadoo_dev -f backend/src/main/resources/sample_data.sql
```

---

## 📊 Test Coverage

### ✅ Implemented & Tested
- [x] Authentication (Login/Register)
- [x] Live Sessions (Create, Join, Start, End)
- [x] Favorites (Add, Remove, Mutual detection)
- [x] AI Gateway (Master, Agentic requests)
- [x] Wallet (Balance, Add funds, Transfer)
- [x] WebSocket (Real-time updates)

### ⏳ Needs Testing
- [ ] Pricing calculations
- [ ] Gamification (Badges, Points)
- [ ] Community features
- [ ] Visual Query
- [ ] Admin operations

---

## 🎯 Next Steps

1. **Run all tests**: `mvn test`
2. **Check coverage**: `mvn jacoco:report`
3. **Test with Postman**: Import collection from `POSTMAN_COLLECTION_GUIDE.md`
4. **Load test**: Use JMeter or k6 for performance testing
5. **Deploy**: Follow `BACKEND_INTEGRATION_DEPLOYMENT.md`

---

## 📚 Documentation Links

- **Database Schema**: `DATABASE_SCHEMA_DOCUMENTATION.md`
- **Test Data**: `TEST_DATA_SAMPLES.md`
- **API Testing**: `POSTMAN_COLLECTION_GUIDE.md`
- **Deployment**: `BACKEND_INTEGRATION_DEPLOYMENT.md`
- **Test Coverage**: `TEST_COVERAGE_REPORT.md`

---

## ✅ Success Checklist

- [ ] All services running (PostgreSQL, Redis, RabbitMQ)
- [ ] Sample data loaded successfully
- [ ] Backend application started
- [ ] Health check returns UP
- [ ] Can login and get JWT token
- [ ] Can access live sessions
- [ ] Can add/remove favorites
- [ ] Can make AI requests
- [ ] Wallet operations working
- [ ] WebSocket connections established

---

**Status**: Ready to test! 🚀

All sample data loaded, APIs documented, and ready for comprehensive testing.
