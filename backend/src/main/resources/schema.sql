-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    gst_number VARCHAR(15),
    created_at TIMESTAMP NOT NULL
);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    unit VARCHAR(20) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    gst_rate DECIMAL(5, 2) NOT NULL,
    hsn_code VARCHAR(10),
    stock_quantity INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);

-- Create bills table
CREATE TABLE IF NOT EXISTS bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_number VARCHAR(255) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    bill_date DATE NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    cgst_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    sgst_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    igst_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    is_inter_state BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Create bill_items table
CREATE TABLE IF NOT EXISTS bill_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    gst_rate DECIMAL(5, 2) NOT NULL,
    cgst_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    sgst_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    igst_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_price DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (bill_id) REFERENCES bills(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
