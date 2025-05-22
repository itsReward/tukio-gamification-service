-- src/main/resources/db/migration/V2__fix_activity_type_column.sql

-- First, ensure the column is of VARCHAR type
ALTER TABLE point_transactions
ALTER COLUMN activity_type TYPE VARCHAR(255)
  USING activity_type::VARCHAR;

-- Then update any mismatched values
UPDATE point_transactions
SET activity_type = 'EVENT_REGISTRATION'
WHERE activity_type = 'REGISTRATION';

UPDATE point_transactions
SET activity_type = 'EVENT_ATTENDANCE'
WHERE activity_type = 'ATTENDANCE';

UPDATE point_transactions
SET activity_type = 'EVENT_RATING'
WHERE activity_type = 'RATING';

UPDATE point_transactions
SET activity_type = 'EVENT_SHARING'
WHERE activity_type = 'SHARING';