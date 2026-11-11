CREATE TABLE cities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    location_id VARCHAR(64) NOT NULL UNIQUE,
    timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Shanghai',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cities_name (name)
);

CREATE TABLE air_quality_observations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_id BIGINT NOT NULL,
    observed_at TIMESTAMP NOT NULL,
    collected_at TIMESTAMP NOT NULL,
    source VARCHAR(32) NOT NULL,
    aqi INT NOT NULL,
    pm2_5 DECIMAL(10,2),
    pm10 DECIMAL(10,2),
    so2 DECIMAL(10,2),
    no2 DECIMAL(10,2),
    co DECIMAL(10,2),
    o3 DECIMAL(10,2),
    CONSTRAINT fk_observation_city FOREIGN KEY (city_id) REFERENCES cities(id),
    CONSTRAINT uk_observation_city_time_source UNIQUE (city_id, observed_at, source),
    INDEX idx_observation_city_time (city_id, observed_at DESC)
);

CREATE TABLE collection_job_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    status VARCHAR(32) NOT NULL,
    total_cities INT NOT NULL DEFAULT 0,
    successful_cities INT NOT NULL DEFAULT 0,
    failed_cities INT NOT NULL DEFAULT 0,
    error_summary VARCHAR(1000)
);

CREATE TABLE forecasts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_id BIGINT NOT NULL,
    forecast_at TIMESTAMP NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    aqi INT NOT NULL,
    model_version VARCHAR(64) NOT NULL,
    quality_status VARCHAR(32) NOT NULL,
    CONSTRAINT fk_forecast_city FOREIGN KEY (city_id) REFERENCES cities(id),
    CONSTRAINT uk_forecast_city_time_version UNIQUE (city_id, forecast_at, model_version)
);

CREATE TABLE alert_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    city_id BIGINT,
    metric VARCHAR(32) NOT NULL,
    operator VARCHAR(8) NOT NULL,
    threshold DECIMAL(10,2) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_alert_rule_city FOREIGN KEY (city_id) REFERENCES cities(id)
);

CREATE TABLE alert_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    city_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    triggered_value DECIMAL(10,2) NOT NULL,
    first_triggered_at TIMESTAMP NOT NULL,
    last_triggered_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    CONSTRAINT fk_alert_event_rule FOREIGN KEY (rule_id) REFERENCES alert_rules(id),
    CONSTRAINT fk_alert_event_city FOREIGN KEY (city_id) REFERENCES cities(id),
    INDEX idx_alert_event_status (status, last_triggered_at DESC)
);
