-- Adds username and password columns for customer login.
USE vanilla_db;

ALTER TABLE Customers ADD COLUMN username VARCHAR(50) UNIQUE AFTER customer_contact;
ALTER TABLE Customers ADD COLUMN password VARCHAR(100) NOT NULL DEFAULT '' AFTER username;

UPDATE Customers SET username = 'lina',   password = 'Customer@1' WHERE customer_id = 1;
UPDATE Customers SET username = 'maya',   password = 'Customer@2' WHERE customer_id = 2;
UPDATE Customers SET username = 'khaled', password = 'Customer@3' WHERE customer_id = 3;
