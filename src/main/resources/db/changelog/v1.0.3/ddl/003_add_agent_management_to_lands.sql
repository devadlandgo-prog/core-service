-- Add agent_management column to lands table
ALTER TABLE lands ADD COLUMN IF NOT EXISTS agent_management BOOLEAN DEFAULT FALSE NOT NULL;
