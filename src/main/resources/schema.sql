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

-- Content Moderation Reports Table
CREATE TABLE IF NOT EXISTS content_reports (
                                               id INT AUTO_INCREMENT PRIMARY KEY,
                                               report_id VARCHAR(50) UNIQUE NOT NULL, -- e.g., 'REP-10482'
                                               content_id VARCHAR(50) NOT NULL,
                                               content_snippet TEXT NOT NULL,
                                               reported_user VARCHAR(255) NOT NULL,
                                               reporter VARCHAR(255) NOT NULL,
                                               severity ENUM('LOW', 'MEDIUM', 'HIGH', 'SEVERE') DEFAULT 'MEDIUM',
                                               status ENUM('PENDING', 'REVIEW', 'RESOLVED', 'DISMISSED') DEFAULT 'PENDING',
                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Social Media Ingestion Pipeline Table
CREATE TABLE IF NOT EXISTS ingested_media_links (
                                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                                    partner_id INT NOT NULL,
                                                    platform ENUM('INSTAGRAM', 'YOUTUBE', 'TIKTOK') NOT NULL,
                                                    original_url TEXT NOT NULL,
                                                    media_id VARCHAR(100), -- Extracted ID (e.g., Reel ID or Video ID)
                                                    status ENUM('PENDING_REVIEW', 'APPROVED', 'REJECTED') DEFAULT 'PENDING_REVIEW',
                                                    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                    FOREIGN KEY (partner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 1. Partner Subscriptions Table
CREATE TABLE IF NOT EXISTS partner_subscriptions (
                                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                                     partner_id INT NOT NULL,
                                                     tier_name VARCHAR(50) NOT NULL, -- e.g., 'BASIC', 'PRO', 'ENTERPRISE'
                                                     monthly_fee DECIMAL(10,2) NOT NULL,
                                                     status ENUM('ACTIVE', 'EXPIRED', 'CANCELLED') DEFAULT 'ACTIVE',
                                                     started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                     expires_at TIMESTAMP NULL,
                                                     FOREIGN KEY (partner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 2. Partner Commissions & Transactions Table
CREATE TABLE IF NOT EXISTS partner_transactions (
                                                    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
                                                    partner_id INT NOT NULL,
                                                    booking_id VARCHAR(50) NOT NULL,
                                                    total_amount DECIMAL(10,2) NOT NULL,
                                                    platform_commission DECIMAL(10,2) NOT NULL, -- Platform rake (e.g., 10%)
                                                    partner_payout DECIMAL(10,2) NOT NULL,     -- Net amount for partner
                                                    payout_status ENUM('PENDING', 'PAID', 'FAILED') DEFAULT 'PENDING',
                                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                    FOREIGN KEY (partner_id) REFERENCES users(id) ON DELETE CASCADE
);