package com.tlias.weather.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<AppNotification> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndAlertEventId(Long userId, Long alertEventId);
    long countByUserIdAndReadAtIsNull(Long userId);
}
