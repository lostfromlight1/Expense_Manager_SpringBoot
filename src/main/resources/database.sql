DROP DATABASE IF EXISTS expense_manager_db;
CREATE DATABASE expense_manager_db;
USE expense_manager_db;

DROP TABLE IF EXISTS wallets;
DROP TABLE IF EXISTS accounts;

-- 1. Create Accounts (Parent Table)
CREATE TABLE accounts (
                          account_id VARCHAR(255) NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          email VARCHAR(255) NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          date_of_birth VARCHAR(255) NOT NULL,
    -- Audit fields from AbstractEntity
                          is_active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP NULL,
                          PRIMARY KEY (account_id),
                          CONSTRAINT uk_accounts_email UNIQUE (email)
);

-- 2. Create Wallets (Child Table)
CREATE TABLE wallets (
                         wallet_id VARCHAR(255) NOT NULL,
                         account_id VARCHAR(255) NOT NULL,
                         balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                         budget DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    -- Audit fields from AbstractEntity
                         is_active BOOLEAN NOT NULL DEFAULT TRUE,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP NULL,
                         PRIMARY KEY (wallet_id),
                         CONSTRAINT uk_wallets_account UNIQUE (account_id), -- Ensures One-to-One
                         CONSTRAINT fk_wallet_account FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);