-- Branch-specific menu prices.
USE vanilla_db;

ALTER TABLE Branch_Product ADD COLUMN branch_price DECIMAL(10,2) NULL AFTER is_available;

UPDATE Branch_Product SET branch_price = 15.00 WHERE branch_id = 1 AND product_id = 1;
UPDATE Branch_Product SET branch_price = 14.00 WHERE branch_id = 1 AND product_id = 2;
UPDATE Branch_Product SET branch_price = 18.00 WHERE branch_id = 1 AND product_id = 3;
UPDATE Branch_Product SET branch_price = 16.00 WHERE branch_id = 2 AND product_id = 1;
UPDATE Branch_Product SET branch_price = 11.00 WHERE branch_id = 2 AND product_id = 4;
