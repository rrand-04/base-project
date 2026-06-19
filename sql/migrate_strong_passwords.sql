-- Strong passwords for the sample customer accounts.
USE vanilla_db;

UPDATE Customers SET password = 'Customer@1' WHERE username = 'lina';
UPDATE Customers SET password = 'Customer@2' WHERE username = 'maya';
UPDATE Customers SET password = 'Customer@3' WHERE username = 'khaled';

SELECT username, customer_name FROM Customers;
