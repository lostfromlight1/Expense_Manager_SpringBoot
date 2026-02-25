DROP DATABASE IF EXISTS expense_manager_db;
CREATE DATABASE expense_manager_db;
USE expense_manager_db;

-- 1. Permissions Table
CREATE TABLE permissions (
                             permission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE
);

-- 2. Roles Table
CREATE TABLE roles (
                       role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

-- 3. Role-Permission Join Table
CREATE TABLE role_permissions (
                                  role_id BIGINT NOT NULL,
                                  permission_id BIGINT NOT NULL,
                                  PRIMARY KEY (role_id, permission_id),
                                  FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
                                  FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
);

-- 4. Accounts Table (Updated with role_id)
CREATE TABLE accounts (
                          account_id VARCHAR(255) NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          email VARCHAR(255) NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          date_of_birth VARCHAR(255) NOT NULL,
                          role_id BIGINT,
                          is_active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          deleted_at DATETIME NULL,
                          PRIMARY KEY (account_id),
                          CONSTRAINT uk_accounts_email UNIQUE (email),
                          CONSTRAINT fk_account_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- 5. Wallets Table
CREATE TABLE wallets (
                         wallet_id VARCHAR(255) NOT NULL,
                         account_id VARCHAR(255) NOT NULL,
                         balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                         budget DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                         is_active BOOLEAN NOT NULL DEFAULT TRUE,
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         deleted_at DATETIME NULL,
                         PRIMARY KEY (wallet_id),
                         CONSTRAINT uk_wallets_account UNIQUE (account_id),
                         CONSTRAINT fk_wallet_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- 6. Transactions Table
CREATE TABLE transactions (
                              transaction_id VARCHAR(50) NOT NULL,
                              wallet_id VARCHAR(255) NOT NULL,
                              transaction_type VARCHAR(20) NOT NULL,
                              category_type VARCHAR(50) NOT NULL,
                              amount DOUBLE PRECISION NOT NULL,
                              is_active BOOLEAN NOT NULL DEFAULT TRUE,
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              deleted_at DATETIME NULL,
                              PRIMARY KEY (transaction_id),
                              CONSTRAINT fk_transaction_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE
);

-- 7. Audit Logs Table
CREATE TABLE audit_logs (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            action VARCHAR(50) NOT NULL,
                            entity_name VARCHAR(50) NOT NULL,
                            entity_id VARCHAR(50) NOT NULL,
                            performed_by VARCHAR(100) DEFAULT 'SYSTEM_USER',
                            details TEXT,
                            timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- INITIAL SEED DATA
INSERT INTO permissions (name) VALUES ('CRUD_OWN_DATA'), ('CRUD_ALL_DATA');
INSERT INTO roles (name) VALUES ('USER'), ('ADMIN');

-- Assign permissions: Admin gets both, User gets only own data
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1), (2, 1), (2, 2);