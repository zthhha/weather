CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE user_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    city_id BIGINT NOT NULL,
    minimum_severity VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_subscription_city FOREIGN KEY (city_id) REFERENCES cities(id),
    CONSTRAINT uk_subscription_user_city UNIQUE (user_id, city_id),
    INDEX idx_subscription_user_enabled (user_id, enabled),
    INDEX idx_subscription_city_enabled (city_id, enabled)
);

CREATE TABLE app_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    alert_event_id BIGINT NOT NULL,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_notification_event FOREIGN KEY (alert_event_id) REFERENCES alert_events(id),
    CONSTRAINT uk_notification_user_event UNIQUE (user_id, alert_event_id),
    INDEX idx_notification_user_read_time (user_id, read_at, created_at DESC)
);

INSERT INTO roles (code, name) VALUES
    ('ROLE_ADMIN', 'Administrator'),
    ('ROLE_USER', 'User');

INSERT INTO users (username, password_hash, enabled)
SELECT 'admin', '$2a$10$WQ2QxZdwFYOJZCZiW41L5eKjWJXg4f2gZyqPiMG2FG.zJlWwepN2W', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO user_roles (user_id, role_id)
SELECT users.id, roles.id
FROM users JOIN roles ON roles.code = 'ROLE_ADMIN'
WHERE users.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles
      WHERE user_roles.user_id = users.id AND user_roles.role_id = roles.id
  );
