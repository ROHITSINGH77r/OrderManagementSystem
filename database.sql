-- ====================================================================
-- Order Management System - Database Setup Script
-- B.Tech 3rd Semester OOPJ Practical Examination Project
-- Database: ecommerce_db
-- Table: Orders
-- ====================================================================

-- 1. Create the database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS ecommerce_db;

-- 2. Switch to ecommerce_db
USE ecommerce_db;

-- 3. Drop existing table if needed (for fresh setup) and create Orders table
DROP TABLE IF EXISTS Orders;

CREATE TABLE Orders (
    order_id INT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    status VARCHAR(30) NOT NULL
);

-- 4. Insert sample order records
-- These records demonstrate different statuses (Pending, Shipped, Delivered, Cancelled)
-- and quantities/prices to satisfy testing of all 5 operations.
INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) VALUES
(101, 'Aarav Sharma', 'Gaming Laptop', 1, 65000.00, 'Delivered'),
(102, 'Priya Patel', 'Wireless Mouse', 3, 750.50, 'Pending'),
(103, 'Rohan Verma', 'Mechanical Keyboard', 4, 2800.00, 'Shipped'),
(104, 'Sneha Gupta', 'Noise-Cancelling Headphones', 1, 4500.00, 'Cancelled'),
(105, 'Vikram Singh', 'USB-C Fast Charger', 5, 1200.00, 'Pending'),
(106, 'Ananya Das', 'External SSD 1TB', 3, 5600.00, 'Cancelled');

-- Verify inserted records
SELECT * FROM Orders;
