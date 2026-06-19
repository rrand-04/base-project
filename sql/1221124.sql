DROP DATABASE IF EXISTS vanilla_db;
CREATE DATABASE vanilla_db;
USE vanilla_db;

CREATE TABLE Employee (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    employee_position VARCHAR(50),
    employee_salary DECIMAL(10,2),
    employee_contact VARCHAR(30),
    branch_id INT,
    manager_id INT
);

CREATE TABLE Branches (
    branch_id INT PRIMARY KEY AUTO_INCREMENT,
    branch_name VARCHAR(100) NOT NULL,
    branch_location VARCHAR(150),
    branch_contact VARCHAR(30),
    manager_id INT
);

ALTER TABLE Branches
    ADD CONSTRAINT fk_branch_manager
    FOREIGN KEY (manager_id) REFERENCES Employee(employee_id);

ALTER TABLE Employee
    ADD CONSTRAINT fk_employee_branch
    FOREIGN KEY (branch_id) REFERENCES Branches(branch_id);

ALTER TABLE Employee
    ADD CONSTRAINT fk_employee_manager
    FOREIGN KEY (manager_id) REFERENCES Employee(employee_id);

CREATE TABLE Customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    customer_contact VARCHAR(30),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE Product (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    product_category VARCHAR(50),
    product_price DECIMAL(10,2),
    product_description VARCHAR(255)
);

CREATE TABLE Branch_Product (
    branch_id INT,
    product_id INT,
    is_available BOOLEAN,
    branch_price DECIMAL(10,2),
    PRIMARY KEY (branch_id, product_id),
    FOREIGN KEY (branch_id) REFERENCES Branches(branch_id),
    FOREIGN KEY (product_id) REFERENCES Product(product_id)
);

CREATE TABLE Tables (
    table_id INT AUTO_INCREMENT PRIMARY KEY,
    branch_id INT NOT NULL,
    capacity INT NOT NULL DEFAULT 4,
    FOREIGN KEY (branch_id) REFERENCES Branches(branch_id) ON DELETE CASCADE
);

CREATE TABLE Orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    order_date DATE,
    order_status VARCHAR(30),
    total_price DECIMAL(10,2),
    customer_id INT,
    branch_id INT,
    table_id INT,
    FOREIGN KEY (table_id) REFERENCES Tables(table_id),
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id),
    FOREIGN KEY (branch_id) REFERENCES Branches(branch_id)
);

CREATE TABLE Order_Items (
    order_id INT,
    product_id INT,
    quantity INT,
    price DECIMAL(10,2),
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (product_id) REFERENCES Product(product_id)
);

CREATE TABLE Employee_Order (
    employee_id INT,
    order_id INT,
    role VARCHAR(50),
    PRIMARY KEY (employee_id, order_id),
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id)
);

CREATE TABLE Payment (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    payment_method VARCHAR(50),
    payment_amount DECIMAL(10,2),
    payment_date DATE,
    payment_status VARCHAR(30),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id)
);

CREATE TABLE Reservation (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    branch_id INT NOT NULL,
    reservation_date DATE NOT NULL,
    reservation_time TIME NOT NULL,
    number_of_people INT NOT NULL DEFAULT 1,
    status ENUM('pending', 'confirmed', 'cancelled', 'completed') NOT NULL DEFAULT 'pending',
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES Branches(branch_id) ON DELETE CASCADE
);

CREATE TABLE Delivery (
    delivery_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,
    delivery_address VARCHAR(255) NOT NULL,
    delivery_status ENUM('pending', 'on_the_way', 'delivered', 'failed') NOT NULL DEFAULT 'pending',
    delivery_time TIMESTAMP NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
);

CREATE TABLE Suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    supplier_contact VARCHAR(30),
    email VARCHAR(100),
    country VARCHAR(50),
    city VARCHAR(50),
    street VARCHAR(100),
    postal_num VARCHAR(20)
);

CREATE TABLE Inventory (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    item_category ENUM('raw_material', 'packaging', 'cleaning', 'other'),
    unit ENUM('kg', 'liter', 'pieces') NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE Supplier_Inventory (
    supplier_id INT NOT NULL,
    item_id INT NOT NULL,
    supply_price DECIMAL(10,2) NOT NULL,
    quantity_supplied DECIMAL(10,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (supplier_id, item_id),
    FOREIGN KEY (supplier_id) REFERENCES Suppliers(supplier_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES Inventory(item_id) ON DELETE CASCADE
);

CREATE TABLE Supplier_Branch (
    supplier_id INT NOT NULL,
    branch_id INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (supplier_id, branch_id),
    FOREIGN KEY (supplier_id) REFERENCES Suppliers(supplier_id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES Branches(branch_id) ON DELETE CASCADE
);

CREATE TABLE Supplier_Product (
    supplier_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity_required DECIMAL(10,4) NOT NULL,
    PRIMARY KEY (supplier_id, product_id),
    FOREIGN KEY (supplier_id) REFERENCES Suppliers(supplier_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE CASCADE
);

CREATE TABLE Warehouse (
    warehouse_id INT AUTO_INCREMENT PRIMARY KEY,
    warehouse_name VARCHAR(100) NOT NULL,
    warehouse_capacity DECIMAL(10,2) NOT NULL,
    branch_id INT NOT NULL,
    FOREIGN KEY (branch_id) REFERENCES Branches(branch_id) ON DELETE CASCADE
);

CREATE TABLE Warehouse_Inventory (
    warehouse_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL DEFAULT 0,
    minimum_threshold DECIMAL(10,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (warehouse_id, item_id),
    FOREIGN KEY (warehouse_id) REFERENCES Warehouse(warehouse_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES Inventory(item_id) ON DELETE CASCADE
);

CREATE TABLE Purchase (
    purchase_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    branch_id INT NOT NULL,
    purchase_date DATE NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (supplier_id) REFERENCES Suppliers(supplier_id) ON DELETE RESTRICT,
    FOREIGN KEY (branch_id) REFERENCES Branches(branch_id) ON DELETE RESTRICT
);

CREATE TABLE Stock_Movement (
    movement_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    warehouse_id INT NOT NULL,
    movement_type ENUM('IN', 'OUT') NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reference_id INT,
    FOREIGN KEY (item_id) REFERENCES Inventory(item_id) ON DELETE RESTRICT,
    FOREIGN KEY (warehouse_id) REFERENCES Warehouse(warehouse_id) ON DELETE RESTRICT
);

CREATE TABLE Product_Inventory (
    product_id INT NOT NULL,
    item_id INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    quantity DECIMAL(10,4) NOT NULL,
    PRIMARY KEY (product_id, item_id),
    FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES Inventory(item_id) ON DELETE RESTRICT
);

INSERT INTO Branches (branch_name, branch_location, branch_contact)
VALUES
('AL Tireh', 'Ramallah', '022961179'),
('AL Masyoon Branch', 'Ramallah', '022961180'),
('AL Irsal Branch', 'Ramallah', '022961181'),
('Nablus Branch', 'Nablus', '022961182'),
('Icon Mall Branch', 'Nablus', '022961183'),
('Al Manara Branch', 'Ramallah', '022961184');

INSERT INTO Employee
(first_name, last_name, employee_position, employee_salary, employee_contact, branch_id, manager_id)
VALUES
('Woroud', 'Hamayel', 'Manager', 1200, '0599000001', 1, NULL),
('Ahmad', 'Saleh', 'Cashier', 700, '0599000002', 1, 1),
('Sara', 'Ali', 'Barista', 650, '0599000003', 1, 1),
('Rand', 'Saleh', 'Manager', 1250, '0599000004', 2, NULL),
('Omar', 'Khaled', 'Cashier', 720, '0599000005', 2, 4);

UPDATE Branches SET manager_id = 1 WHERE branch_id = 1;
UPDATE Branches SET manager_id = 4 WHERE branch_id = 2;

INSERT INTO Customers (customer_name, customer_contact, username, password)
VALUES
('Lina Ahmad', '0591111111', 'lina', 'Customer@1'),
('Maya Saleh', '0592222222', 'maya', 'Customer@2'),
('Khaled Omar', '0593333333', 'khaled', 'Customer@3');

INSERT INTO Product
(product_name, product_category, product_price, product_description)
VALUES
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

INSERT INTO Branch_Product (branch_id, product_id, is_available, branch_price) VALUES
(1,1,TRUE,12.00),(1,2,TRUE,22.00),(1,3,TRUE,25.00),(1,4,TRUE,20.00),(1,5,TRUE,15.00),(1,6,TRUE,14.00),(1,7,TRUE,10.00),(1,8,TRUE,12.00),(1,10,TRUE,18.00),(1,11,TRUE,16.00),(1,13,TRUE,22.00),
(2,1,TRUE,13.00),(2,2,TRUE,23.00),(2,5,TRUE,16.00),(2,6,TRUE,15.00),(2,7,TRUE,11.00),(2,9,TRUE,14.00),(2,10,TRUE,19.00),(2,12,TRUE,21.00),
(3,3,TRUE,26.00),(3,4,TRUE,21.00),(3,5,TRUE,15.00),(3,6,TRUE,14.00),(3,8,TRUE,13.00),(3,9,TRUE,13.00),(3,11,TRUE,17.00),(3,13,TRUE,23.00),
(4,1,TRUE,12.00),(4,2,TRUE,22.00),(4,5,TRUE,15.00),(4,6,TRUE,14.00),(4,7,TRUE,10.00),(4,10,TRUE,18.00),(4,11,TRUE,16.00),(4,12,TRUE,20.00),
(5,1,TRUE,14.00),(5,3,TRUE,27.00),(5,5,TRUE,17.00),(5,6,TRUE,15.00),(5,8,TRUE,14.00),(5,9,TRUE,14.00),(5,10,TRUE,20.00),(5,13,TRUE,24.00),
(6,2,TRUE,23.00),(6,4,TRUE,22.00),(6,5,TRUE,16.00),(6,6,TRUE,15.00),(6,7,TRUE,11.00),(6,9,TRUE,14.00),(6,11,TRUE,17.00),(6,12,TRUE,21.00);

INSERT INTO Tables (branch_id, capacity)
VALUES
(1, 4),
(1, 6),
(1, 2),
(2, 4),
(2, 8),
(3, 4),
(3, 4),
(3, 6),
(4, 2),
(4, 4),
(4, 4),
(4, 6),
(5, 2),
(5, 4),
(5, 6),
(5, 8),
(6, 2),
(6, 4),
(6, 6),
(6, 6);

INSERT INTO Orders
(order_date, order_status, total_price, customer_id, branch_id)
VALUES
('2026-05-01', 'Completed', 29.00, 1, 1),
('2026-05-01', 'Completed', 18.00, 2, 1),
('2026-05-02', 'Pending', 15.00, 1, 2),
('2026-05-03', 'Completed', 25.00, 3, 2);

INSERT INTO Order_Items
(order_id, product_id, quantity, price)
VALUES
(1, 1, 1, 15.00),
(1, 2, 1, 14.00),
(2, 3, 1, 18.00),
(3, 1, 1, 15.00),
(4, 4, 2, 10.00);

INSERT INTO Employee_Order (employee_id, order_id, role)
VALUES
(2, 1, 'Cashier'),
(3, 1, 'Prepared order'),
(2, 2, 'Cashier'),
(5, 3, 'Cashier');

INSERT INTO Payment
(order_id, payment_method, payment_amount, payment_date, payment_status)
VALUES
(1, 'Cash', 29.00, '2026-05-01', 'Paid'),
(2, 'Card', 18.00, '2026-05-01', 'Paid'),
(3, 'Cash', 15.00, '2026-05-02', 'Pending');

INSERT INTO Reservation
(customer_id, branch_id, reservation_date, reservation_time, number_of_people, status)
VALUES
(1, 1, '2026-06-10', '18:00:00', 4, 'confirmed'),
(1, 2, '2026-06-15', '12:30:00', 2, 'pending');

INSERT INTO Delivery (order_id, delivery_address, delivery_status)
VALUES
(3, 'Nablus Street 5', 'pending');
USE vanilla_db;
 -- SELECT username, password FROM Customers;


