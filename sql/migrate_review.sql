-- Product review table used by My Orders and the ratings chart.
USE vanilla_db;

CREATE TABLE IF NOT EXISTS Review (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    product_id INT NOT NULL,
    order_id INT NULL,
    rating INT NOT NULL,
    comment VARCHAR(255) NULL,
    review_date DATE NOT NULL,
    UNIQUE KEY uk_customer_product (customer_id, product_id),
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE SET NULL
);

SELECT 'Review table ready' AS status;
