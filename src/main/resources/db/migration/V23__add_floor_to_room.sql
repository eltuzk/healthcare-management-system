-- ============================================================
-- Add floor column to ROOM table for visual mapping
-- ============================================================

ALTER TABLE ROOM ADD (
    floor NUMBER DEFAULT 1 NOT NULL
);

CREATE INDEX idx_room_floor ON ROOM(floor);
