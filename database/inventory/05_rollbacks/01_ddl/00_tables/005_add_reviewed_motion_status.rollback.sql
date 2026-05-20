UPDATE inventory.motion
SET status = 'MARKED'
WHERE status = 'REVIEWED';

ALTER TABLE inventory.motion
    DROP CONSTRAINT IF EXISTS chk_motion_status;

ALTER TABLE inventory.motion
    ADD CONSTRAINT chk_motion_status
    CHECK (status IN ('NORMAL', 'MARKED'));
