-- SQL Script to manually add logged_in column and create driver_status_event table
-- Execute this in pgAdmin or any PostgreSQL client connected to tiltaxidb database

-- Step 1: Add logged_in column to driver table if it doesn't exist
ALTER TABLE driver ADD COLUMN IF NOT EXISTS logged_in BOOLEAN DEFAULT false;

-- Step 2: Update any NULL values to false
UPDATE driver SET logged_in = false WHERE logged_in IS NULL;

-- Step 3: Create driver_status_event table if it doesn't exist
CREATE TABLE IF NOT EXISTS driver_status_event (
    id SERIAL PRIMARY KEY,
    driver_id INTEGER NOT NULL REFERENCES driver(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL CHECK (event_type IN ('LOGIN', 'LOGOUT', 'ACTIVE', 'INACTIVE')),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Step 4: Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_driver_status_event_driver_id ON driver_status_event(driver_id);
CREATE INDEX IF NOT EXISTS idx_driver_status_event_timestamp ON driver_status_event(timestamp);
CREATE INDEX IF NOT EXISTS idx_driver_status_event_driver_timestamp ON driver_status_event(driver_id, timestamp);

-- Step 5: Verify the changes
SELECT 'driver table columns:' as info;
SELECT column_name FROM information_schema.columns WHERE table_name='driver' ORDER BY ordinal_position;

SELECT 'driver_status_event table exists:' as info;
SELECT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name='driver_status_event') as table_exists;

-- Step 6: Check current driver data
SELECT 'Sample drivers with logged_in status:' as info;
SELECT id, email, logged_in, is_active FROM driver LIMIT 5;

