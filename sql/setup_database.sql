-- =====================================================
-- BOOKTRACK DATABASE SETUP SCRIPT
-- Execute this script to set up all database objects
-- =====================================================

-- First, ensure error_log table exists
@@error_log_table.sql

-- Create or recreate all sequences and tables
@@ddls.sql

-- Create triggers
@@triggers.sql

-- Create user procedures and functions
@@user_procedures.sql

-- Show completion message
SELECT 'BookTrack Database Setup Complete!' as STATUS FROM DUAL;
