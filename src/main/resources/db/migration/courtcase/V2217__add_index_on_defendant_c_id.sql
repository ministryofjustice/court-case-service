CREATE INDEX IF NOT EXISTS defendant_c_id_active_idx
    ON defendant (c_id)
    WHERE deleted = false AND c_id IS NOT NULL;
