package com.tlias.weather.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    @Query("select rule from AlertRule rule where rule.enabled = true and (rule.city.id = :cityId or rule.city is null)")
    List<AlertRule> findEnabledApplicableToCity(@Param("cityId") Long cityId);
}
