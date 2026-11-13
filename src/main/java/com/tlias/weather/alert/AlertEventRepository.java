package com.tlias.weather.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {
    Optional<AlertEvent> findByActiveKey(String activeKey);
    List<AlertEvent> findByCityIdAndStatus(Long cityId, AlertEventStatus status);
}
