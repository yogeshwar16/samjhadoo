# Database Schema Documentation

**Project**: Samjhadoo Backend  
**Database**: PostgreSQL 14+  
**Last Updated**: October 16, 2025

---

## 📋 Table of Contents

1. [Database Overview](#database-overview)
2. [Core Tables](#core-tables)
3. [Module-Specific Tables](#module-specific-tables)
4. [Relationships](#relationships)
5. [Indexes](#indexes)

---

## 🗄️ Database Overview

### Databases Used

| Database | Purpose | Technology |
|----------|---------|------------|
| **PostgreSQL** | Primary relational database | PostgreSQL 14+ |
| **Redis** | Session storage, caching | Redis 6.2+ |
| **RabbitMQ** | Message queue for WebSocket | RabbitMQ 3.9+ |

### Schema Statistics

- **Total Tables**: 50+
- **Total Relationships**: 80+
- **Estimated Size**: 10GB (with data)

---

## 📊 Core Tables

### 1. users
**Purpose**: Store user account information

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY | User ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Username |
| email | VARCHAR(100) | UNIQUE, NOT NULL | Email address |
| password | VARCHAR(255) | NOT NULL | Encrypted password |
| name | VARCHAR(100) | | Full name |
| role | VARCHAR(20) | NOT NULL | USER/MENTOR/ADMIN |
| is_active | BOOLEAN | DEFAULT true | Account status |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | | Last update |

---

### 2. mentor_profiles
**Purpose**: Store mentor-specific information

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY | Profile ID |
| user_id | BIGINT | FK(users), UNIQUE | User reference |
| bio | TEXT | | Mentor biography |
| expertise | VARCHAR(255) | | Expertise areas |
| hourly_rate | DECIMAL(10,2) | | Base hourly rate |
| is_verified | BOOLEAN | DEFAULT false | Verification status |
| rating | DECIMAL(3,2) | | Average rating |
| total_sessions | INTEGER | DEFAULT 0 | Session count |

---

### 3. wallets
**Purpose**: User wallet for transactions

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY | Wallet ID |
| user_id | BIGINT | FK(users), UNIQUE | User reference |
| balance | DECIMAL(10,2) | DEFAULT 0.00 | Current balance |
| currency | VARCHAR(3) | DEFAULT 'INR' | Currency code |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | | Last update |

---

## 🎯 Module-Specific Tables

### Friendly Talk Module

#### live_sessions
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Session ID |
| title | VARCHAR(255) | Session title |
| mentor_id | BIGINT | FK(users) |
| status | VARCHAR(20) | SCHEDULED/LIVE/ENDED/CANCELLED |
| type | VARCHAR(50) | Session type |
| start_time | TIMESTAMP | Actual start |
| scheduled_start_time | TIMESTAMP | Planned start |
| max_participants | INTEGER | Capacity limit |
| current_participants | INTEGER | Current count |
| meeting_url | VARCHAR(500) | Meeting link |
| is_featured | BOOLEAN | Featured flag |

#### favorites
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Favorite ID |
| user_id | BIGINT | FK(users) |
| favorite_user_id | BIGINT | FK(users) |
| tag | VARCHAR(50) | Custom tag |
| notes | VARCHAR(500) | Personal notes |
| notify_when_online | BOOLEAN | Notification preference |
| is_mutual | BOOLEAN | Mutual favorite flag |

---

### AI Module

#### ai_interactions
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Interaction ID |
| user_id | BIGINT | FK(users) |
| tier | VARCHAR(20) | MASTER/AGENTIC |
| request_type | VARCHAR(50) | Request type |
| prompt | TEXT | User prompt |
| response | TEXT | AI response |
| tokens_used | INTEGER | Token count |
| cost | DECIMAL(10,4) | Request cost |

---

### Gamification Module

#### badges
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Badge ID |
| name | VARCHAR(100) | Badge name |
| description | TEXT | Description |
| icon_url | VARCHAR(500) | Icon URL |
| criteria | TEXT | Earning criteria |

#### user_badges
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Record ID |
| user_id | BIGINT | FK(users) |
| badge_id | BIGINT | FK(badges) |
| earned_at | TIMESTAMP | Earned timestamp |

---

## 🔗 Key Relationships

```
users (1) -----> (1) mentor_profiles
users (1) -----> (1) wallets
users (1) -----> (N) live_sessions (as mentor)
users (1) -----> (N) favorites (as user)
users (N) <----> (N) live_sessions (participants)
users (1) -----> (N) ai_interactions
users (N) <----> (N) badges (through user_badges)
```

---

## 📑 Indexes

### Performance Indexes

```sql
-- Users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);

-- Live Sessions
CREATE INDEX idx_live_sessions_mentor ON live_sessions(mentor_id);
CREATE INDEX idx_live_sessions_status ON live_sessions(status);
CREATE INDEX idx_live_sessions_type ON live_sessions(type);
CREATE INDEX idx_live_sessions_scheduled ON live_sessions(scheduled_start_time);

-- Favorites
CREATE INDEX idx_favorites_user ON favorites(user_id);
CREATE INDEX idx_favorites_favorite_user ON favorites(favorite_user_id);
CREATE INDEX idx_favorites_mutual ON favorites(is_mutual);

-- AI Interactions
CREATE INDEX idx_ai_interactions_user ON ai_interactions(user_id);
CREATE INDEX idx_ai_interactions_tier ON ai_interactions(tier);
```

---

See `TEST_DATA_SAMPLES.md` for sample data and `sample_data.sql` for insertion scripts.
