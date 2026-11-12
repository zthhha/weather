package com.tlias.weather.notification.api;

import com.tlias.weather.common.ApiResponse;
import com.tlias.weather.notification.AppNotification;
import com.tlias.weather.notification.AppNotificationRepository;
import com.tlias.weather.security.User;
import com.tlias.weather.security.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/me/notifications")
public class NotificationController {
    private final UserRepository users;
    private final AppNotificationRepository notifications;

    public NotificationController(UserRepository users, AppNotificationRepository notifications) {
        this.users = users;
        this.notifications = notifications;
    }

    @GetMapping
    public ApiResponse<List<NotificationResponse>> list(Authentication authentication, @RequestParam(defaultValue = "false") boolean unreadOnly) {
        List<NotificationResponse> result = notifications.findByUserIdOrderByCreatedAtDesc(currentUser(authentication).getId()).stream()
                .filter(notification -> !unreadOnly || notification.getReadAt() == null)
                .map(NotificationResponse::from).toList();
        return ApiResponse.of(result);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(Authentication authentication) {
        return ApiResponse.of(notifications.countByUserIdAndReadAtIsNull(currentUser(authentication).getId()));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(@PathVariable Long id, Authentication authentication) {
        AppNotification notification = notifications.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.markRead(Instant.now());
        return ApiResponse.of(NotificationResponse.from(notifications.save(notification)));
    }

    private User currentUser(Authentication authentication) {
        return users.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
