-- Align column sizes with the API validation rules
ALTER TABLE categories
    ALTER COLUMN name TYPE VARCHAR(50),
    ALTER COLUMN description TYPE VARCHAR(255);

ALTER TABLE tasks
    ALTER COLUMN title TYPE VARCHAR(100),
    ALTER COLUMN description TYPE VARCHAR(500);

-- Store instants with time zone and never allow missing audit timestamps
ALTER TABLE tasks
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at SET NOT NULL;

-- Status is a closed set of values
ALTER TABLE tasks
    ADD CONSTRAINT ck_tasks_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE'));

-- Category names are unique regardless of case
CREATE UNIQUE INDEX uq_categories_name_lower ON categories (LOWER(name));

-- Support filtering tasks by category and status
CREATE INDEX idx_tasks_category_id ON tasks (category_id);
CREATE INDEX idx_tasks_status ON tasks (status);
