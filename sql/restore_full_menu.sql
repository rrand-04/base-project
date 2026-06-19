-- Restores the full branch menu, categories, and product images.
USE vanilla_db;

-- Safe add image_file column
SET @has_image := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'vanilla_db' AND TABLE_NAME = 'Product' AND COLUMN_NAME = 'image_file'
);
SET @sql := IF(@has_image = 0,
    'ALTER TABLE Product ADD COLUMN image_file VARCHAR(100) NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SOURCE migrate_al_tireh_menu.sql;
SOURCE migrate_nablus_menu.sql;
SOURCE migrate_manara_and_desserts.sql;
SOURCE migrate_drinks_menu.sql;
SOURCE migrate_small_branches_no_food_no_hookah.sql;
SOURCE migrate_new_menu_items.sql;
SOURCE migrate_product_images.sql;
SOURCE migrate_product_images_2.sql;
SOURCE migrate_product_images_3.sql;
SOURCE migrate_product_images_standardize.sql;
SOURCE migrate_drink_images.sql;
SOURCE migrate_drink_images_2.sql;
SOURCE migrate_dessert_images.sql;
