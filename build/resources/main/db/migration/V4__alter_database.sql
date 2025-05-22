-- src/main/resources/db/migration/V2__fix_enum_columns.sql

-- Update the badge category and tier columns
ALTER TABLE badges ALTER COLUMN category TYPE VARCHAR(255) USING category::VARCHAR;
ALTER TABLE badges ALTER COLUMN tier TYPE VARCHAR(255) USING tier::VARCHAR;

-- Also update the point_transactions activity_type column if it hasn't been done yet
ALTER TABLE point_transactions ALTER COLUMN activity_type TYPE VARCHAR(255) USING activity_type::VARCHAR;