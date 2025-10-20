-- ============================================
-- Samjhadoo Backend - Sample Test Data
-- ============================================
-- Purpose: Populate database with test data for API testing
-- Database: PostgreSQL 14+
-- Last Updated: October 16, 2025
-- ============================================

-- Clear existing data (use with caution!)
-- TRUNCATE TABLE users, mentor_profiles, wallets, live_sessions, favorites CASCADE;

-- ============================================
-- 1. USERS
-- ============================================
-- Password: All passwords are 'Test@123' (BCrypt encoded)
-- BCrypt hash: $2a$10$xQPXj5qZ5qZ5qZ5qZ5qZ5uZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ

INSERT INTO users (id, username, email, password, name, role, is_active, created_at, updated_at) VALUES
(1, 'admin_user', 'admin@samjhadoo.com', '$2a$10$xQPXj5qZ5qZ5qZ5qZ5qZ5uZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ', 'Admin User', 'ADMIN', true, NOW(), NOW()),
(2, 'mentor_john', 'john@mentor.com', '$2a$10$xQPXj5qZ5qZ5qZ5qZ5qZ5uZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ', 'John Smith', 'MENTOR', true, NOW(), NOW()),
(3, 'mentor_sarah', 'sarah@mentor.com', '$2a$10$xQPXj5qZ5qZ5qZ5qZ5qZ5uZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ', 'Sarah Johnson', 'MENTOR', true, NOW(), NOW()),
(4, 'user_alice', 'alice@user.com', '$2a$10$xQPXj5qZ5qZ5qZ5qZ5qZ5uZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ', 'Alice Williams', 'USER', true, NOW(), NOW()),
(5, 'user_bob', 'bob@user.com', '$2a$10$xQPXj5qZ5qZ5qZ5qZ5qZ5uZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ', 'Bob Brown', 'USER', true, NOW(), NOW()),
(6, 'mentor_raj', 'raj@mentor.com', '$2a$10$xQPXj5qZ5qZ5qZ5qZ5qZ5uZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ', 'Raj Kumar', 'MENTOR', true, NOW(), NOW()),
(7, 'user_priya', 'priya@user.com', '$2a$10$xQPXj5qZ5qZ5qZ5qZ5qZ5uZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ', 'Priya Sharma', 'USER', true, NOW(), NOW()),
(8, 'mentor_emma', 'emma@mentor.com', '$2a$10$xQPXj5qZ5qZ5qZ5qZ5qZ5uZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ5qZ', 'Emma Davis', 'MENTOR', true, NOW(), NOW());

-- Reset sequence
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- ============================================
-- 2. MENTOR PROFILES
-- ============================================

INSERT INTO mentor_profiles (id, user_id, bio, expertise, hourly_rate, is_verified, rating, total_sessions, created_at, updated_at) VALUES
(1, 2, 'Senior Software Engineer with 10+ years experience in full-stack development. Passionate about mentoring aspiring developers.', 'Software Engineering, System Design, Career Guidance', 500.00, true, 4.8, 150, NOW(), NOW()),
(2, 3, 'Life Coach and Wellness Expert. Helping people find balance and peace in their lives through mindfulness and practical strategies.', 'Life Coaching, Wellness, Stress Management', 600.00, true, 4.9, 200, NOW(), NOW()),
(3, 6, 'Tech Lead at a Fortune 500 company. Specializing in AI/ML and cloud architecture. Love sharing knowledge with the community.', 'AI/ML, Cloud Computing, Leadership', 800.00, true, 4.7, 120, NOW(), NOW()),
(4, 8, 'Career Transition Specialist. Helped 100+ professionals successfully switch careers. Expert in resume building and interview prep.', 'Career Coaching, Interview Prep, Resume Building', 450.00, true, 4.9, 180, NOW(), NOW());

SELECT setval('mentor_profiles_id_seq', (SELECT MAX(id) FROM mentor_profiles));

-- ============================================
-- 3. WALLETS
-- ============================================

INSERT INTO wallets (id, user_id, balance, currency, created_at, updated_at) VALUES
(1, 1, 10000.00, 'INR', NOW(), NOW()),
(2, 2, 5000.00, 'INR', NOW(), NOW()),
(3, 3, 7500.00, 'INR', NOW(), NOW()),
(4, 4, 2000.00, 'INR', NOW(), NOW()),
(5, 5, 1500.00, 'INR', NOW(), NOW()),
(6, 6, 6000.00, 'INR', NOW(), NOW()),
(7, 7, 3000.00, 'INR', NOW(), NOW()),
(8, 8, 4500.00, 'INR', NOW(), NOW());

SELECT setval('wallets_id_seq', (SELECT MAX(id) FROM wallets));

-- ============================================
-- 4. LIVE SESSIONS
-- ============================================

INSERT INTO live_sessions (id, title, description, mentor_id, status, type, start_time, end_time, scheduled_start_time, scheduled_duration_minutes, max_participants, current_participants, meeting_url, thumbnail_url, is_recorded, recording_url, is_featured, created_at, updated_at) VALUES
-- Currently LIVE sessions
(1, 'Night Radio - Chill Vibes', 'Late night relaxing session with music and soothing stories to help you unwind', 2, 'LIVE', 'NIGHT_RADIO', NOW() - INTERVAL '30 minutes', NULL, NOW() - INTERVAL '30 minutes', 120, 100, 25, 'https://meet.google.com/abc-defg-hij', 'https://example.com/night-radio.jpg', true, NULL, true, NOW() - INTERVAL '1 hour', NOW()),
(2, 'Morning Motivation Boost', 'Start your day with positive energy and actionable insights', 3, 'LIVE', 'LIVE_MOTIVATION_TALK', NOW() - INTERVAL '15 minutes', NULL, NOW() - INTERVAL '15 minutes', 30, 200, 87, 'https://meet.google.com/xyz-abcd-efg', 'https://example.com/motivation.jpg', true, NULL, true, NOW() - INTERVAL '1 hour', NOW()),

-- SCHEDULED sessions
(3, 'Peaceful Dreams - Lori Session', 'Soothing lullabies and bedtime stories for a peaceful night', 3, 'SCHEDULED', 'NIGHT_LORI', NULL, NULL, NOW() + INTERVAL '8 hours', 45, 50, 0, NULL, 'https://example.com/lori.jpg', true, NULL, true, NOW(), NOW()),
(4, 'Breaking into Tech - Career Guidance', 'How to transition into software engineering from non-tech background', 2, 'SCHEDULED', 'LIVE_TOPIC_SESSION', NULL, NULL, NOW() + INTERVAL '5 hours', 60, 30, 0, NULL, 'https://example.com/career.jpg', true, NULL, false, NOW(), NOW()),
(5, 'Relationship Wisdom - Love & Life', 'Navigate relationships with confidence and emotional intelligence', 8, 'SCHEDULED', 'LOVE_LIFE_TIPS', NULL, NULL, NOW() + INTERVAL '6 hours', 45, 50, 0, NULL, 'https://example.com/love-life.jpg', false, NULL, false, NOW(), NOW()),
(6, 'AI/ML Career Roadmap', 'Complete guide to building a career in artificial intelligence and machine learning', 6, 'SCHEDULED', 'LIVE_TOPIC_SESSION', NULL, NULL, NOW() + INTERVAL '2 days', 90, 40, 0, NULL, 'https://example.com/ai-career.jpg', true, NULL, true, NOW(), NOW()),

-- ENDED sessions
(7, 'Stress Management Workshop', 'Practical techniques to manage stress and anxiety in daily life', 3, 'ENDED', 'GENERAL_TALK', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '60 minutes', NOW() - INTERVAL '2 days', 60, 50, 42, 'https://meet.google.com/old-session-1', 'https://example.com/stress.jpg', true, 'https://example.com/recordings/stress-workshop.mp4', false, NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days'),
(8, 'Interview Preparation Masterclass', 'Ace your tech interviews with proven strategies and mock interviews', 2, 'ENDED', 'LIVE_TOPIC_SESSION', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '90 minutes', NOW() - INTERVAL '1 day', 90, 30, 28, 'https://meet.google.com/old-session-2', 'https://example.com/interview.jpg', true, 'https://example.com/recordings/interview-prep.mp4', false, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day');

SELECT setval('live_sessions_id_seq', (SELECT MAX(id) FROM live_sessions));

-- ============================================
-- 5. LIVE SESSION TAGS
-- ============================================

INSERT INTO live_session_tags (session_id, tag) VALUES
(1, 'relaxation'), (1, 'music'), (1, 'stories'), (1, 'night'),
(2, 'motivation'), (2, 'morning'), (2, 'inspiration'), (2, 'productivity'),
(3, 'lullaby'), (3, 'sleep'), (3, 'relaxation'), (3, 'bedtime'),
(4, 'career'), (4, 'tech'), (4, 'guidance'), (4, 'transition'),
(5, 'relationships'), (5, 'love'), (5, 'life'), (5, 'emotional-intelligence'),
(6, 'AI'), (6, 'machine-learning'), (6, 'career'), (6, 'roadmap'),
(7, 'stress'), (7, 'wellness'), (7, 'mental-health'),
(8, 'interview'), (8, 'tech'), (8, 'preparation'), (8, 'career');

-- ============================================
-- 6. LIVE SESSION PARTICIPANTS
-- ============================================

INSERT INTO live_session_participants (session_id, user_id) VALUES
-- Session 1 (Night Radio) - 25 participants
(1, 4), (1, 5), (1, 7),
-- Session 2 (Morning Motivation) - 87 participants (showing subset)
(2, 4), (2, 5), (2, 7),
-- Past sessions
(7, 4), (7, 5), (7, 7),
(8, 4), (8, 5), (8, 7);

-- ============================================
-- 7. FAVORITES
-- ============================================

INSERT INTO favorites (id, user_id, favorite_user_id, tag, notes, notify_when_online, is_mutual, created_at) VALUES
-- Alice's favorites
(1, 4, 2, 'Career Mentor', 'Excellent guidance on tech career paths. Very patient and knowledgeable.', true, false, NOW() - INTERVAL '30 days'),
(2, 4, 3, 'Calm Vibes', 'Always helps me relax during stressful times. Her sessions are therapeutic.', true, true, NOW() - INTERVAL '20 days'),
(3, 4, 6, 'AI Expert', 'Best mentor for AI/ML topics. Clear explanations and practical examples.', true, false, NOW() - INTERVAL '10 days'),

-- Bob's favorites
(4, 5, 2, 'Always Replies', 'Very responsive and helpful. Great for quick career advice.', false, false, NOW() - INTERVAL '25 days'),
(5, 5, 8, 'Interview Coach', 'Helped me land my dream job! Amazing interview preparation sessions.', true, false, NOW() - INTERVAL '15 days'),

-- Priya's favorites
(6, 7, 3, 'Life Coach', 'Changed my perspective on work-life balance. Highly recommended!', true, true, NOW() - INTERVAL '40 days'),
(7, 7, 6, 'Tech Guru', 'Inspiring mentor who makes complex topics easy to understand.', true, false, NOW() - INTERVAL '5 days'),

-- Mutual favorite (Sarah favorites Alice back)
(8, 3, 4, 'Engaged Student', 'Very dedicated and asks great questions. Pleasure to mentor!', true, true, NOW() - INTERVAL '18 days');

SELECT setval('favorites_id_seq', (SELECT MAX(id) FROM favorites));

-- ============================================
-- 8. AI INTERACTIONS
-- ============================================

INSERT INTO ai_interactions (id, user_id, tier, request_type, prompt, response, tokens_used, cost, created_at) VALUES
(1, 4, 'MASTER', 'CHAT', 'How can I improve my communication skills?', 'Here are 5 key strategies to improve communication: 1) Active listening...', 250, 0.005, NOW() - INTERVAL '2 days'),
(2, 5, 'AGENTIC', 'MENTOR_MATCH', 'Find me a mentor for career transition to AI/ML', 'Based on your requirements, I recommend the following mentors: 1) Raj Kumar...', 450, 0.015, NOW() - INTERVAL '1 day'),
(3, 7, 'MASTER', 'SESSION_PREP', 'Prepare talking points for interview preparation session', 'Session Prep: 1) Resume optimization techniques, 2) Common interview questions...', 320, 0.008, NOW() - INTERVAL '3 hours'),
(4, 4, 'AGENTIC', 'CHAT', 'What are the best practices for system design interviews?', 'System design interviews require: 1) Understanding requirements, 2) Scalability...', 580, 0.020, NOW() - INTERVAL '5 hours');

SELECT setval('ai_interactions_id_seq', (SELECT MAX(id) FROM ai_interactions));

-- ============================================
-- 9. AI RATE LIMITS
-- ============================================

INSERT INTO ai_rate_limits (id, user_id, tier, request_count, window_start, window_end, created_at, updated_at) VALUES
(1, 4, 'MASTER', 3, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '23 hours', NOW() - INTERVAL '1 hour', NOW()),
(2, 5, 'AGENTIC', 1, NOW() - INTERVAL '30 minutes', NOW() + INTERVAL '23.5 hours', NOW() - INTERVAL '30 minutes', NOW()),
(3, 7, 'MASTER', 2, NOW() - INTERVAL '2 hours', NOW() + INTERVAL '22 hours', NOW() - INTERVAL '2 hours', NOW());

SELECT setval('ai_rate_limits_id_seq', (SELECT MAX(id) FROM ai_rate_limits));

-- ============================================
-- 10. BADGES
-- ============================================

INSERT INTO badges (id, name, description, icon_url, criteria, created_at) VALUES
(1, 'First Session', 'Completed your first mentoring session', 'https://example.com/badges/first-session.png', 'Complete 1 session', NOW()),
(2, 'Early Bird', 'Joined 5 morning sessions', 'https://example.com/badges/early-bird.png', 'Join 5 sessions before 9 AM', NOW()),
(3, 'Night Owl', 'Attended 10 night sessions', 'https://example.com/badges/night-owl.png', 'Join 10 sessions after 10 PM', NOW()),
(4, 'Knowledge Seeker', 'Completed 25 sessions', 'https://example.com/badges/knowledge-seeker.png', 'Complete 25 sessions', NOW()),
(5, 'Mentor Master', 'Conducted 50 sessions as mentor', 'https://example.com/badges/mentor-master.png', 'Conduct 50 sessions', NOW()),
(6, 'Community Builder', 'Helped 100 people', 'https://example.com/badges/community-builder.png', 'Help 100 unique users', NOW());

SELECT setval('badges_id_seq', (SELECT MAX(id) FROM badges));

-- ============================================
-- 11. USER BADGES
-- ============================================

INSERT INTO user_badges (id, user_id, badge_id, earned_at) VALUES
-- Alice's badges
(1, 4, 1, NOW() - INTERVAL '60 days'),
(2, 4, 2, NOW() - INTERVAL '45 days'),
(3, 4, 4, NOW() - INTERVAL '10 days'),

-- Bob's badges
(4, 5, 1, NOW() - INTERVAL '50 days'),
(5, 5, 3, NOW() - INTERVAL '30 days'),

-- Mentor John's badges
(6, 2, 5, NOW() - INTERVAL '20 days'),
(7, 2, 6, NOW() - INTERVAL '5 days'),

-- Mentor Sarah's badges
(8, 3, 5, NOW() - INTERVAL '15 days'),
(9, 3, 6, NOW() - INTERVAL '3 days');

SELECT setval('user_badges_id_seq', (SELECT MAX(id) FROM user_badges));

-- ============================================
-- 12. TOPICS
-- ============================================

INSERT INTO topics (id, name, description, category, is_active, created_at, updated_at) VALUES
(1, 'Career Guidance', 'Professional career advice and planning', 'CAREER', true, NOW(), NOW()),
(2, 'Technical Skills', 'Programming, system design, and technical topics', 'TECHNICAL', true, NOW(), NOW()),
(3, 'Life Coaching', 'Personal development and life balance', 'PERSONAL', true, NOW(), NOW()),
(4, 'Interview Preparation', 'Job interview tips and mock interviews', 'CAREER', true, NOW(), NOW()),
(5, 'Stress Management', 'Techniques for managing stress and anxiety', 'WELLNESS', true, NOW(), NOW()),
(6, 'Relationship Advice', 'Love, relationships, and emotional intelligence', 'PERSONAL', true, NOW(), NOW());

SELECT setval('topics_id_seq', (SELECT MAX(id) FROM topics));

-- ============================================
-- 13. NOTIFICATIONS
-- ============================================

INSERT INTO notifications (id, user_id, type, title, message, is_read, created_at) VALUES
(1, 4, 'SESSION_REMINDER', 'Session Starting Soon', 'Your session "Breaking into Tech" starts in 30 minutes', false, NOW() - INTERVAL '30 minutes'),
(2, 4, 'FAVORITE_ONLINE', 'Mentor Online', 'Your favorite mentor John Smith is now online', false, NOW() - INTERVAL '1 hour'),
(3, 5, 'SESSION_STARTED', 'Session Live', 'Morning Motivation Boost is now live! Join now.', true, NOW() - INTERVAL '15 minutes'),
(4, 7, 'MUTUAL_FAVORITE', 'New Mutual Favorite', 'Sarah Johnson added you as a favorite too!', false, NOW() - INTERVAL '2 hours'),
(5, 4, 'BADGE_EARNED', 'New Badge Earned', 'Congratulations! You earned the "Knowledge Seeker" badge', false, NOW() - INTERVAL '10 days');

SELECT setval('notifications_id_seq', (SELECT MAX(id) FROM notifications));

-- ============================================
-- 14. TRANSACTIONS (Wallet History)
-- ============================================

INSERT INTO transactions (id, wallet_id, type, amount, description, status, created_at) VALUES
(1, 4, 'CREDIT', 2000.00, 'Initial deposit', 'COMPLETED', NOW() - INTERVAL '60 days'),
(2, 4, 'DEBIT', 500.00, 'Payment for session with John Smith', 'COMPLETED', NOW() - INTERVAL '45 days'),
(3, 5, 'CREDIT', 1500.00, 'Initial deposit', 'COMPLETED', NOW() - INTERVAL '50 days'),
(4, 2, 'CREDIT', 500.00, 'Payment received from Alice Williams', 'COMPLETED', NOW() - INTERVAL '45 days'),
(5, 7, 'CREDIT', 3000.00, 'Initial deposit', 'COMPLETED', NOW() - INTERVAL '40 days'),
(6, 7, 'DEBIT', 600.00, 'Payment for session with Sarah Johnson', 'COMPLETED', NOW() - INTERVAL '30 days'),
(7, 3, 'CREDIT', 600.00, 'Payment received from Priya Sharma', 'COMPLETED', NOW() - INTERVAL '30 days');

SELECT setval('transactions_id_seq', (SELECT MAX(id) FROM transactions));

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Verify data insertion
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Mentor Profiles', COUNT(*) FROM mentor_profiles
UNION ALL
SELECT 'Wallets', COUNT(*) FROM wallets
UNION ALL
SELECT 'Live Sessions', COUNT(*) FROM live_sessions
UNION ALL
SELECT 'Favorites', COUNT(*) FROM favorites
UNION ALL
SELECT 'AI Interactions', COUNT(*) FROM ai_interactions
UNION ALL
SELECT 'Badges', COUNT(*) FROM badges
UNION ALL
SELECT 'User Badges', COUNT(*) FROM user_badges
UNION ALL
SELECT 'Topics', COUNT(*) FROM topics
UNION ALL
SELECT 'Notifications', COUNT(*) FROM notifications
UNION ALL
SELECT 'Transactions', COUNT(*) FROM transactions;

-- ============================================
-- USEFUL QUERIES FOR TESTING
-- ============================================

-- Get all live sessions with mentor details
-- SELECT ls.*, u.name as mentor_name 
-- FROM live_sessions ls 
-- JOIN users u ON ls.mentor_id = u.id 
-- WHERE ls.status = 'LIVE';

-- Get user's favorites with mentor details
-- SELECT f.*, u.name as favorite_mentor_name 
-- FROM favorites f 
-- JOIN users u ON f.favorite_user_id = u.id 
-- WHERE f.user_id = 4;

-- Get mutual favorites
-- SELECT f1.user_id, f1.favorite_user_id, u1.name as user_name, u2.name as favorite_name
-- FROM favorites f1
-- JOIN favorites f2 ON f1.user_id = f2.favorite_user_id AND f1.favorite_user_id = f2.user_id
-- JOIN users u1 ON f1.user_id = u1.id
-- JOIN users u2 ON f1.favorite_user_id = u2.id
-- WHERE f1.is_mutual = true;

-- Get user's wallet balance
-- SELECT u.name, w.balance, w.currency 
-- FROM wallets w 
-- JOIN users u ON w.user_id = u.id;

-- ============================================
-- END OF SAMPLE DATA
-- ============================================
