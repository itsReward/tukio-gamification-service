-- Path: src/main/resources/db/migration/V1__initial_schema.sql

-- User Points table
CREATE TABLE IF NOT EXISTS user_points (
                                           id BIGSERIAL PRIMARY KEY,
                                           user_id BIGINT NOT NULL UNIQUE,
                                           total_points INT NOT NULL DEFAULT 0,
                                           monthly_points INT NOT NULL DEFAULT 0,
                                           weekly_points INT NOT NULL DEFAULT 0,
                                           level INT NOT NULL DEFAULT 1,
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Badges table
CREATE TABLE IF NOT EXISTS badges (
                                      id BIGSERIAL PRIMARY KEY,
                                      name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    required_points INT NOT NULL,
    category VARCHAR(50) NOT NULL,
    tier VARCHAR(50) NOT NULL
    );

-- User Badges table
CREATE TABLE IF NOT EXISTS user_badges (
                                           id BIGSERIAL PRIMARY KEY,
                                           user_id BIGINT NOT NULL,
                                           badge_id BIGINT NOT NULL REFERENCES badges(id),
    awarded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_id BIGINT NULL,
    UNIQUE (user_id, badge_id)
    );

-- Point Transactions table
CREATE TABLE IF NOT EXISTS point_transactions (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  user_id BIGINT NOT NULL,
                                                  points INT NOT NULL,
                                                  activity_type VARCHAR(50) NOT NULL,
    event_id BIGINT NULL,
    description VARCHAR(255) NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Leaderboards table
CREATE TABLE IF NOT EXISTS leaderboards (
                                            id BIGSERIAL PRIMARY KEY,
                                            name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    time_frame VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    event_category_id BIGINT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Leaderboard Entries table
CREATE TABLE IF NOT EXISTS leaderboard_entries (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   leaderboard_id BIGINT NOT NULL REFERENCES leaderboards(id),
    user_id BIGINT NOT NULL,
    rank INT NOT NULL,
    score INT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (leaderboard_id, user_id)
    );

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_user_points_user_id ON user_points(user_id);
CREATE INDEX IF NOT EXISTS idx_user_points_level ON user_points(level);
CREATE INDEX IF NOT EXISTS idx_user_points_total_points ON user_points(total_points);

CREATE INDEX IF NOT EXISTS idx_badges_category ON badges(category);
CREATE INDEX IF NOT EXISTS idx_badges_tier ON badges(tier);
CREATE INDEX IF NOT EXISTS idx_badges_required_points ON badges(required_points);

CREATE INDEX IF NOT EXISTS idx_user_badges_user_id ON user_badges(user_id);
CREATE INDEX IF NOT EXISTS idx_user_badges_badge_id ON user_badges(badge_id);
CREATE INDEX IF NOT EXISTS idx_user_badges_awarded_at ON user_badges(awarded_at);

CREATE INDEX IF NOT EXISTS idx_point_transactions_user_id ON point_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_point_transactions_activity_type ON point_transactions(activity_type);
CREATE INDEX IF NOT EXISTS idx_point_transactions_event_id ON point_transactions(event_id);
CREATE INDEX IF NOT EXISTS idx_point_transactions_timestamp ON point_transactions(timestamp);

CREATE INDEX IF NOT EXISTS idx_leaderboards_time_frame ON leaderboards(time_frame);
CREATE INDEX IF NOT EXISTS idx_leaderboards_category ON leaderboards(category);
CREATE INDEX IF NOT EXISTS idx_leaderboards_is_active ON leaderboards(is_active);

CREATE INDEX IF NOT EXISTS idx_leaderboard_entries_leaderboard_id ON leaderboard_entries(leaderboard_id);
CREATE INDEX IF NOT EXISTS idx_leaderboard_entries_user_id ON leaderboard_entries(user_id);
CREATE INDEX IF NOT EXISTS idx_leaderboard_entries_rank ON leaderboard_entries(rank);
CREATE INDEX IF NOT EXISTS idx_leaderboard_entries_score ON leaderboard_entries(score);

-- Initial badges data
INSERT INTO badges (name, description, image_url, required_points, category, tier) VALUES
                                                                                       ('First Attendance', 'Attended your first event', '/images/badges/first-attendance.png', 10, 'ATTENDANCE', 'BRONZE'),
                                                                                       ('Event Explorer', 'Attended 5 different events', '/images/badges/event-explorer.png', 50, 'ATTENDANCE', 'SILVER'),
                                                                                       ('Event Master', 'Attended 25 different events', '/images/badges/event-master.png', 250, 'ATTENDANCE', 'GOLD'),
                                                                                       ('Event Legend', 'Attended 50 different events', '/images/badges/event-legend.png', 500, 'ATTENDANCE', 'PLATINUM'),

                                                                                       ('First Registration', 'Registered for your first event', '/images/badges/first-registration.png', 5, 'REGISTRATION', 'BRONZE'),
                                                                                       ('Regular Participant', 'Registered for 10 events', '/images/badges/regular-participant.png', 50, 'REGISTRATION', 'SILVER'),
                                                                                       ('Dedicated Participant', 'Registered for 30 events', '/images/badges/dedicated-participant.png', 150, 'REGISTRATION', 'GOLD'),

                                                                                       ('Critic', 'Rated your first event', '/images/badges/critic.png', 2, 'RATING', 'BRONZE'),
                                                                                       ('Reviewer', 'Rated 10 events', '/images/badges/reviewer.png', 20, 'RATING', 'SILVER'),
                                                                                       ('Feedback Master', 'Rated 25 events', '/images/badges/feedback-master.png', 50, 'RATING', 'GOLD'),

                                                                                       ('Diverse Attendee', 'Attended events in 3 different categories', '/images/badges/diverse-attendee.png', 30, 'DIVERSITY', 'BRONZE'),
                                                                                       ('Versatile Attendee', 'Attended events in 5 different categories', '/images/badges/versatile-attendee.png', 50, 'DIVERSITY', 'SILVER'),
                                                                                       ('Campus Explorer', 'Attended events in all categories', '/images/badges/campus-explorer.png', 100, 'DIVERSITY', 'GOLD'),

                                                                                       ('Social Butterfly', 'Shared your first event', '/images/badges/social-butterfly.png', 3, 'SOCIAL', 'BRONZE'),
                                                                                       ('Event Promoter', 'Shared 10 events', '/images/badges/event-promoter.png', 30, 'SOCIAL', 'SILVER'),
                                                                                       ('Campus Influencer', 'Shared 25 events', '/images/badges/campus-influencer.png', 75, 'SOCIAL', 'GOLD'),

                                                                                       ('Milestone: 100', 'Earned 100 points', '/images/badges/milestone-100.png', 100, 'MILESTONE', 'BRONZE'),
                                                                                       ('Milestone: 500', 'Earned 500 points', '/images/badges/milestone-500.png', 500, 'MILESTONE', 'SILVER'),
                                                                                       ('Milestone: 1000', 'Earned 1000 points', '/images/badges/milestone-1000.png', 1000, 'MILESTONE', 'GOLD'),
                                                                                       ('Milestone: 5000', 'Earned 5000 points', '/images/badges/milestone-5000.png', 5000, 'MILESTONE', 'PLATINUM')
    ON CONFLICT (name) DO NOTHING;

-- Create default leaderboards
INSERT INTO leaderboards (name, description, time_frame, category, is_active) VALUES
                                                                                  ('Weekly Overall', 'Top users for weekly overall points leaderboard', 'WEEKLY', 'OVERALL', true),
                                                                                  ('Monthly Overall', 'Top users for monthly overall points leaderboard', 'MONTHLY', 'OVERALL', true),
                                                                                  ('All-Time Overall', 'Top users for all-time overall points leaderboard', 'ALL_TIME', 'OVERALL', true),
                                                                                  ('Weekly Attendance', 'Top users for weekly event attendance leaderboard', 'WEEKLY', 'EVENT_ATTENDANCE', true),
                                                                                  ('Monthly Attendance', 'Top users for monthly event attendance leaderboard', 'MONTHLY', 'EVENT_ATTENDANCE', true)
    ON CONFLICT (name) DO NOTHING;