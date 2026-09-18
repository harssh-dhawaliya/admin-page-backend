CREATE TABLE IF NOT EXISTS users (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('SUPER_ADMIN', 'CONTENT_MODERATOR', 'PARTNER', 'USER') DEFAULT 'USER',
    kyc_status ENUM('NOT_SUBMITTED', 'PENDING', 'VERIFIED', 'REJECTED') DEFAULT 'NOT_SUBMITTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS audit_logs (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          admin_id INT NOT NULL,
                                          action VARCHAR(255) NOT NULL,
    payload JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id)
    );

-- Cities Table for Spot CMS
CREATE TABLE IF NOT EXISTS cities (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      name VARCHAR(255) NOT NULL,
                                      country VARCHAR(255) NOT NULL,
                                      description TEXT,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Points of Interest (POIs) Table linked to Cities
CREATE TABLE IF NOT EXISTS pois (
                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                    city_id INT NOT NULL,
                                    title VARCHAR(255) NOT NULL,
                                    category VARCHAR(100) NOT NULL, -- e.g., 'ADVENTURE', 'CULTURE', 'FOOD'
                                    description TEXT,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    FOREIGN KEY (city_id) REFERENCES cities(id) ON DELETE CASCADE
);

-- Partner KYC Submissions Table
CREATE TABLE IF NOT EXISTS kyc_submissions (
                                               id INT AUTO_INCREMENT PRIMARY KEY,
                                               user_id INT NOT NULL,
                                               business_name VARCHAR(255) NOT NULL,
                                               document_type VARCHAR(100) NOT NULL, -- e.g., 'PASSPORT', 'TAX_ID', 'BUSINESS_LICENSE'
                                               document_reference VARCHAR(255) NOT NULL, -- Path or ID reference to the uploaded document
                                               status ENUM('PENDING', 'VERIFIED', 'REJECTED') DEFAULT 'PENDING',
                                               submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);