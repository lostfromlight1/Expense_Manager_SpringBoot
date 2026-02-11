DROP DATABASE IF EXISTS expense_manager_db;
CREATE DATABASE expense_manager_db;
USE expense_manager_db;

CREATE TABLE accounts (
                          account_id VARCHAR(50) PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          date_of_birth VARCHAR(50),
                          email VARCHAR(100) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP NULL
);
SELECT * FROM accounts;
DROP TABLE accounts;
CREATE TABLE wallets (
                         wallet_id VARCHAR(50) PRIMARY KEY,
                         account_id VARCHAR(50) NOT NULL UNIQUE,
                         balance DECIMAL(12,2) NOT NULL DEFAULT 0,
                         budget DECIMAL(12,2) NOT NULL,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP NULL,
                         CONSTRAINT fk_wallet_account FOREIGN KEY (account_id)
                             REFERENCES accounts(account_id) ON DELETE CASCADE
);
select * from wallets;
-- 1. Remove the constraint that is blocking the update
ALTER TABLE wallets DROP FOREIGN KEY fk_wallet_account;

-- 2. (Optional) If you want a fresh start, drop the table so it recreates perfectly
DROP TABLE IF EXISTS wallets;
-- 1. Disable checks so you can drop everything regardless of order
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Drop the tables that are causing conflicts
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS wallets;
DROP TABLE IF EXISTS accounts;

-- 3. Re-enable checks
SET FOREIGN_KEY_CHECKS = 1;
