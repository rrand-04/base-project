-- Sample branches and starter menu data.
USE vanilla_db;

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM Delivery;
DELETE FROM Payment;
DELETE FROM Employee_Order;
DELETE FROM Order_Items;
DELETE FROM Orders;
DELETE FROM Reservation;
DELETE FROM Branch_Product;
DELETE FROM Product;
SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE Product AUTO_INCREMENT = 1;

UPDATE Branches SET branch_name = 'AL Tireh',         branch_location = 'Ramallah', branch_contact = '022961179' WHERE branch_id = 1;
UPDATE Branches SET branch_name = 'AL Masyoon Branch', branch_location = 'Ramallah', branch_contact = '022961180' WHERE branch_id = 2;

INSERT INTO Branches (branch_name, branch_location, branch_contact)
SELECT 'AL Irsal Branch', 'Ramallah', '022961181' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Branches WHERE branch_name = 'AL Irsal Branch');

INSERT INTO Branches (branch_name, branch_location, branch_contact)
SELECT 'Nablus Branch', 'Nablus', '022961182' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Branches WHERE branch_name = 'Nablus Branch');

INSERT INTO Branches (branch_name, branch_location, branch_contact)
SELECT 'Icon Mall Branch', 'Nablus', '022961183' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Branches WHERE branch_name = 'Icon Mall Branch');

INSERT INTO Branches (branch_name, branch_location, branch_contact)
SELECT 'Al Manara Branch', 'Ramallah', '022961184' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Branches WHERE branch_name = 'Al Manara Branch');

INSERT INTO Product (product_name, product_category, product_price, product_description) VALUES
('Butter Croissant',   'Food',     12.00, 'Fresh baked buttery croissant'),
('Avocado Toast',      'Food',     22.00, 'Sourdough with smashed avocado'),
('Chicken Panini',     'Food',     25.00, 'Grilled chicken with pesto'),
('Caesar Salad',       'Food',     20.00, 'Crisp romaine with parmesan'),
('Latte',              'Drinks',   15.00, 'Espresso with steamed milk'),
('Cappuccino',         'Drinks',   14.00, 'Rich espresso with foam'),
('Iced Tea',           'Drinks',   10.00, 'Refreshing cold tea'),
('Fresh Orange Juice', 'Drinks',   12.00, 'Freshly squeezed oranges'),
('Iced Americano',     'Drinks',   13.00, 'Chilled espresso over ice'),
('Cheesecake',         'Desserts', 18.00, 'Creamy vanilla cheesecake'),
('Chocolate Brownie',  'Desserts', 16.00, 'Warm fudge brownie'),
('Tiramisu',           'Desserts', 20.00, 'Classic Italian dessert'),
('Kunafa',             'Desserts', 22.00, 'Sweet cheese pastry');

-- product_id 1-13 after AUTO_INCREMENT reset
INSERT INTO Branch_Product (branch_id, product_id, is_available, branch_price) VALUES
(1,1,TRUE,12.00),(1,2,TRUE,22.00),(1,3,TRUE,25.00),(1,4,TRUE,20.00),(1,5,TRUE,15.00),(1,6,TRUE,14.00),(1,7,TRUE,10.00),(1,8,TRUE,12.00),(1,10,TRUE,18.00),(1,11,TRUE,16.00),(1,13,TRUE,22.00),
(2,1,TRUE,13.00),(2,2,TRUE,23.00),(2,5,TRUE,16.00),(2,6,TRUE,15.00),(2,7,TRUE,11.00),(2,9,TRUE,14.00),(2,10,TRUE,19.00),(2,12,TRUE,21.00),
(3,3,TRUE,26.00),(3,4,TRUE,21.00),(3,5,TRUE,15.00),(3,6,TRUE,14.00),(3,8,TRUE,13.00),(3,9,TRUE,13.00),(3,11,TRUE,17.00),(3,13,TRUE,23.00),
(4,1,TRUE,12.00),(4,2,TRUE,22.00),(4,5,TRUE,15.00),(4,6,TRUE,14.00),(4,7,TRUE,10.00),(4,10,TRUE,18.00),(4,11,TRUE,16.00),(4,12,TRUE,20.00),
(5,1,TRUE,14.00),(5,3,TRUE,27.00),(5,5,TRUE,17.00),(5,6,TRUE,15.00),(5,8,TRUE,14.00),(5,9,TRUE,14.00),(5,10,TRUE,20.00),(5,13,TRUE,24.00),
(6,2,TRUE,23.00),(6,4,TRUE,22.00),(6,5,TRUE,16.00),(6,6,TRUE,15.00),(6,7,TRUE,11.00),(6,9,TRUE,14.00),(6,11,TRUE,17.00),(6,12,TRUE,21.00);

SELECT branch_id, branch_name FROM Branches ORDER BY branch_id;
