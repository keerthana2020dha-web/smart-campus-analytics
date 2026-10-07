CREATE TABLE IF NOT EXISTS faculty_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    faculty_id VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    successful_login_count BIGINT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_faculty_accounts_faculty_id (faculty_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS faculty_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    faculty_id VARCHAR(50) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_faculty_sessions_token_hash (token_hash),
    KEY idx_faculty_sessions_faculty_id (faculty_id),
    CONSTRAINT fk_faculty_sessions_account
        FOREIGN KEY (faculty_id) REFERENCES faculty_accounts(faculty_id) ON DELETE CASCADE
) ENGINE=InnoDB;
