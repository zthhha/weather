package com.tlias.weather.subscription;

import com.tlias.weather.security.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    List<UserSubscription> findByUserOrderByIdDesc(User user);
    Optional<UserSubscription> findByUserIdAndCityId(Long userId, Long cityId);
    List<UserSubscription> findByCityIdAndEnabledTrue(Long cityId);
}
