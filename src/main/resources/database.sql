DROP DATABASE IF EXISTS expense_manager_db;
CREATE DATABASE expense_manager_db;
USE expense_manager_db;

-- 1. Accounts Table
-- Maps to Account.java and AbstractEntity.java
CREATE TABLE accounts (
                          account_id VARCHAR(255) NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          email VARCHAR(255) NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          date_of_birth VARCHAR(255) NOT NULL,
                          is_active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          deleted_at DATETIME NULL,
                          PRIMARY KEY (account_id),
                          CONSTRAINT uk_accounts_email UNIQUE (email)
);

-- 2. Wallets Table
-- Maps to MyWallet.java and AbstractEntity.java
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

-- 3. Transactions Table
-- Maps to Transaction.java and AbstractEntity.java
CREATE TABLE transactions (
                              transaction_id VARCHAR(50) NOT NULL,
                              wallet_id VARCHAR(255) NOT NULL,
                              transaction_type VARCHAR(20) NOT NULL, -- Enum: INCOME, EXPENSE
                              category_type VARCHAR(50) NOT NULL,    -- Enum: SALARY, FOOD, etc.
                              amount DOUBLE PRECISION NOT NULL,
                              is_active BOOLEAN NOT NULL DEFAULT TRUE,
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              deleted_at DATETIME NULL,
                              PRIMARY KEY (transaction_id),
                              CONSTRAINT fk_transaction_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE
);

-- 4. Audit Logs Table
-- Maps to AuditLog.java
CREATE TABLE audit_logs (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            action VARCHAR(255) NOT NULL,
                            entity_name VARCHAR(255) NOT NULL,
                            entity_id VARCHAR(255) NOT NULL,
                            details TEXT,
                            performed_by VARCHAR(255),
                            timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Verification Selects
SELECT 'Accounts' as table_name, COUNT(*) as count FROM accounts;
SELECT 'Wallets' as table_name, COUNT(*) as count FROM wallets;
SELECT 'Transactions' as table_name, COUNT(*) as count FROM transactions;
SELECT 'Audit Logs' as table_name, COUNT(*) as count FROM audit_logs;
SELECT * FROM  audit_logs;
SELECT * FROM  accounts;
SELECT * FROM wallets;
SELECT * FROM transactions;