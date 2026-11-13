package com.tlias.weather.notification.api;

import com.tlias.weather.common.ApiResponse;
import com.tlias.weather.notification.AppNotification;
import com.tlias.weather.notification.AppNotificationRepository;
import com.tlias.weather.notification.NotificationService;
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
    private final NotificationService notificationService;

    public NotificationController(UserRepository users, AppNotificationRepository notifications, NotificationService notificationService) {
        this.users = users;
        this.notifications = notifications;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<NotificationResponse>> list(Authentication authentication, @RequestParam(defaultValue = "false") boolean unreadOnly) {
        User user = currentUser(authentication);
        notificationService.reconcileActiveNotifications(user);
        List<NotificationResponse> result = notifications.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(notification -> !unreadOnly || notification.getReadAt() == null)
                .map(NotificationResponse::from).toList();
        return ApiResponse.of(result);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(Authentication authentication) {
        User user = currentUser(authentication);
        notificationService.reconcileActiveNotifications(user);
        return ApiResponse.of(notifications.countByUserIdAndReadAtIsNull(user.getId()));
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
