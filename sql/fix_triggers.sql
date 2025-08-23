-- =====================================================
-- DROP PROBLEMATIC TRIGGERS AND RECREATE FIXED ONES
-- =====================================================

-- Drop the problematic duplicate prevention triggers
DROP TRIGGER trg_prevent_duplicate_rating;
DROP TRIGGER trg_prevent_duplicate_review;

-- Show that triggers are dropped
SELECT 'Problematic triggers dropped successfully!' as STATUS FROM DUAL;
