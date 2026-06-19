-- Replace weak test passwords with policy-compliant values.
-- Run: mysql -u root -p vanilla_db < sql/migrate_strong_passwords.sql
USE vanilla_db;

UPDATE Customers SET password = 'Customer@1' WHERE username = 'lina';
UPDATE Customers SET password = 'Customer@2' WHERE username = 'maya';
UPDATE Customers SET password = 'Customer@3' WHERE username = 'khaled';

SELECT username, customer_name FROM Customers;
