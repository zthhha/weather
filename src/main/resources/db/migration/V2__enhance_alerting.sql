ALTER TABLE alert_rules
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX idx_alert_rule_enabled_city ON alert_rules (enabled, city_id);

ALTER TABLE alert_events
    ADD COLUMN active_key VARCHAR(128) NULL,
    ADD COLUMN observation_id BIGINT NULL,
    ADD COLUMN last_observation_id BIGINT NULL,
    ADD COLUMN last_evaluated_at TIMESTAMP NULL,
    ADD COLUMN last_evaluated_value DECIMAL(10,2) NULL,
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE alert_events
SET active_key = CONCAT(rule_id, ':', city_id),
    last_evaluated_at = last_triggered_at,
    last_evaluated_value = triggered_value
WHERE status = 'ACTIVE';

ALTER TABLE alert_events
    ADD CONSTRAINT fk_alert_event_observation FOREIGN KEY (observation_id) REFERENCES air_quality_observations(id),
    ADD CONSTRAINT fk_alert_event_last_observation FOREIGN KEY (last_observation_id) REFERENCES air_quality_observations(id),
    ADD CONSTRAINT uk_alert_event_active_key UNIQUE (active_key);

CREATE INDEX idx_alert_event_city_status_time ON alert_events (city_id, status, last_triggered_at DESC);
CREATE INDEX idx_alert_event_rule_status_time ON alert_events (rule_id, status, last_triggered_at DESC);
