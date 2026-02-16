-- V3__Add_driver_status_tracking.sql
-- Create table for tracking driver status changes (login/logout, active/inactive)
-- This allows us to calculate active hours in the last 24 hours

CREATE TABLE IF NOT EXISTS driver_status_event (
    id SERIAL PRIMARY KEY,
    driver_id INTEGER NOT NULL REFERENCES driver(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL CHECK (event_type IN ('LOGIN', 'LOGOUT', 'ACTIVE', 'INACTIVE')),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_driver_status_event_driver_id ON driver_status_event(driver_id);
CREATE INDEX IF NOT EXISTS idx_driver_status_event_timestamp ON driver_status_event(timestamp);
CREATE INDEX IF NOT EXISTS idx_driver_status_event_driver_timestamp ON driver_status_event(driver_id, timestamp);

