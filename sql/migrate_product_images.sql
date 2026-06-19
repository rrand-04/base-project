-- Link uploaded menu images to products (column added above if missing)
USE vanilla_db;

UPDATE Product SET image_file = 'full_english_breakfast.jpg' WHERE product_name = 'Full English Breakfast';
UPDATE Product SET image_file = 'shakshuka.jpg'              WHERE product_name = 'Shakshuka';
UPDATE Product SET image_file = 'pancakes_maple.jpg'        WHERE product_name = 'Pancakes & Maple';
UPDATE Product SET image_file = 'chicken_burger.jpg'        WHERE product_name = 'Chicken Burger';

SELECT product_name, image_file FROM Product WHERE image_file IS NOT NULL;
