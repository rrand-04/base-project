-- Demo data for partner admin module (inventory / warehouse / analytics)
USE vanilla_db;

INSERT INTO Suppliers (supplier_name, first_name, last_name, supplier_contact, email, country, city, street, postal_num)
VALUES
('Nassar Trading', 'Omar', 'Nassar', '0598000001', 'omar@nassar.ps', 'Palestine', 'Nablus', 'Main St 12', 'P123'),
('Barakat Foods', 'Lina', 'Barakat', '0598000002', 'lina@barakat.ps', 'Palestine', 'Ramallah', 'Market Rd 5', 'P124'),
('Mansour Supplies', 'Yousef', 'Mansour', '0598000003', 'yousef@mansour.ps', 'Palestine', 'Ramallah', 'Industrial 3', 'P125')
ON DUPLICATE KEY UPDATE supplier_name = VALUES(supplier_name);

INSERT INTO Inventory (item_name, item_category, unit)
VALUES
('Arabica Beans', 'raw_material', 'kg'),
('Milk', 'raw_material', 'liter'),
('Paper Cups', 'packaging', 'pieces'),
('Napkins', 'packaging', 'pieces')
ON DUPLICATE KEY UPDATE item_name = VALUES(item_name);

INSERT INTO Warehouse (warehouse_name, warehouse_capacity, branch_id)
SELECT 'AL Tireh Storage', 500, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Warehouse WHERE warehouse_name = 'AL Tireh Storage');

INSERT INTO Warehouse (warehouse_name, warehouse_capacity, branch_id)
SELECT 'Nablus Storage', 400, 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Warehouse WHERE warehouse_name = 'Nablus Storage');

INSERT INTO Warehouse_Inventory (warehouse_id, item_id, quantity, minimum_threshold)
SELECT w.warehouse_id, i.item_id, 25, 30
FROM Warehouse w, Inventory i
WHERE w.warehouse_name = 'AL Tireh Storage' AND i.item_name = 'Arabica Beans'
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), minimum_threshold = VALUES(minimum_threshold);

INSERT INTO Warehouse_Inventory (warehouse_id, item_id, quantity, minimum_threshold)
SELECT w.warehouse_id, i.item_id, 80, 40
FROM Warehouse w, Inventory i
WHERE w.warehouse_name = 'AL Tireh Storage' AND i.item_name = 'Milk'
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), minimum_threshold = VALUES(minimum_threshold);

INSERT INTO Warehouse_Inventory (warehouse_id, item_id, quantity, minimum_threshold)
SELECT w.warehouse_id, i.item_id, 15, 25
FROM Warehouse w, Inventory i
WHERE w.warehouse_name = 'Nablus Storage' AND i.item_name = 'Paper Cups'
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), minimum_threshold = VALUES(minimum_threshold);

INSERT INTO Purchase (supplier_id, branch_id, purchase_date, total_cost)
SELECT s.supplier_id, 1, '2026-03-15', 1200.00
FROM Suppliers s WHERE s.supplier_name = 'Nassar Trading'
AND NOT EXISTS (SELECT 1 FROM Purchase WHERE purchase_date = '2026-03-15' AND total_cost = 1200.00);

INSERT INTO Purchase (supplier_id, branch_id, purchase_date, total_cost)
SELECT s.supplier_id, 1, '2026-04-10', 850.50
FROM Suppliers s WHERE s.supplier_name = 'Barakat Foods'
AND NOT EXISTS (SELECT 1 FROM Purchase WHERE purchase_date = '2026-04-10' AND total_cost = 850.50);

INSERT INTO Purchase (supplier_id, branch_id, purchase_date, total_cost)
SELECT s.supplier_id, 4, '2026-05-05', 640.00
FROM Suppliers s WHERE s.supplier_name = 'Mansour Supplies'
AND NOT EXISTS (SELECT 1 FROM Purchase WHERE purchase_date = '2026-05-05' AND total_cost = 640.00);

INSERT INTO Stock_Movement (item_id, warehouse_id, movement_type, quantity, reference_id)
SELECT i.item_id, w.warehouse_id, 'IN', 50, 1
FROM Inventory i, Warehouse w
WHERE i.item_name = 'Arabica Beans' AND w.warehouse_name = 'AL Tireh Storage'
AND NOT EXISTS (SELECT 1 FROM Stock_Movement WHERE reference_id = 1);
