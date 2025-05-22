ALTER TABLE user_points ALTER COLUMN user_id TYPE bigint USING user_id::bigint;
ALTER TABLE point_transactions ALTER COLUMN user_id TYPE bigint USING user_id::bigint;
ALTER TABLE user_badges ALTER COLUMN user_id TYPE bigint USING user_id::bigint;