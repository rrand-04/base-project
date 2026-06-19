-- Soft cancel support for Orders (no hard DELETE)
USE vanilla_db;

ALTER TABLE Orders
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN cancelled_reason VARCHAR(255) NULL;
