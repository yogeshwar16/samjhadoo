-- Create match_preferences table
CREATE TABLE IF NOT EXISTS match_preferences (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    preferred_session_type VARCHAR(20) NOT NULL,
    min_mentor_experience INT,
    max_hourly_rate DECIMAL(10, 2),
    timezone VARCHAR(50),
    only_verified_mentors BOOLEAN DEFAULT true,
    min_mentor_rating DECIMAL(3, 2),
    preferred_gender VARCHAR(20),
    preferred_age_range VARCHAR(20),
    preferred_experience_level VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create junction tables for collections
CREATE TABLE IF NOT EXISTS preferred_skills (
    preference_id VARCHAR(36) NOT NULL,
    skill VARCHAR(100) NOT NULL,
    PRIMARY KEY (preference_id, skill),
    FOREIGN KEY (preference_id) REFERENCES match_preferences(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS preferred_languages (
    preference_id VARCHAR(36) NOT NULL,
    language VARCHAR(50) NOT NULL,
    PRIMARY KEY (preference_id, language),
    FOREIGN KEY (preference_id) REFERENCES match_preferences(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS interests (
    preference_id VARCHAR(36) NOT NULL,
    interest VARCHAR(100) NOT NULL,
    PRIMARY KEY (preference_id, interest),
    FOREIGN KEY (preference_id) REFERENCES match_preferences(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS preferred_industries (
    preference_id VARCHAR(36) NOT NULL,
    industry VARCHAR(100) NOT NULL,
    PRIMARY KEY (preference_id, industry),
    FOREIGN KEY (preference_id) REFERENCES match_preferences(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create index for performance
CREATE INDEX idx_match_prefs_user ON match_preferences(user_id);
CREATE INDEX idx_match_prefs_session_type ON match_preferences(preferred_session_type);
