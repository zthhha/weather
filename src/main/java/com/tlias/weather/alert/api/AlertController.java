package com.tlias.weather.alert.api;

import com.tlias.weather.alert.AlertEventRepository;
import com.tlias.weather.alert.AlertMetric;
import com.tlias.weather.alert.AlertRuleRepository;
import com.tlias.weather.alert.AlertRuleService;
import com.tlias.weather.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AlertController {
    private final AlertRuleRepository rules;
    private final AlertEventRepository events;
    private final AlertRuleService ruleService;

    public AlertController(AlertRuleRepository rules, AlertEventRepository events, AlertRuleService ruleService) {
        this.rules = rules;
        this.events = events;
        this.ruleService = ruleService;
    }

    @org.springframework.web.bind.annotation.PostMapping("/alert-rules")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<AlertRuleResponse> createRule(@Valid @org.springframework.web.bind.annotation.RequestBody AlertRuleRequest request) {
        return ApiResponse.of(AlertRuleResponse.from(ruleService.create(request)));
    }

    @org.springframework.web.bind.annotation.PutMapping("/alert-rules/{id}")
    public ApiResponse<AlertRuleResponse> updateRule(@org.springframework.web.bind.annotation.PathVariable Long id,
                                                      @Valid @org.springframework.web.bind.annotation.RequestBody AlertRuleRequest request) {
        return ApiResponse.of(AlertRuleResponse.from(ruleService.update(id, request)));
    }

    @GetMapping("/alert-rules")
    public ApiResponse<List<AlertRuleResponse>> rules(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) AlertMetric metric,
            @RequestParam(required = false) String severity) {
        List<AlertRuleResponse> result = rules.findAll().stream()
                .filter(rule -> cityId == null || (rule.getCity() != null && cityId.equals(rule.getCity().getId())))
                .filter(rule -> enabled == null || enabled.equals(rule.isEnabled()))
                .filter(rule -> metric == null || metric == rule.getMetric())
                .filter(rule -> severity == null || severity.equalsIgnoreCase(rule.getSeverity()))
                .sorted(Comparator.comparing(rule -> rule.getId()))
                .map(AlertRuleResponse::from)
                .toList();
        return ApiResponse.of(result);
    }

    @GetMapping("/alert-events")
    public ApiResponse<List<AlertEventResponse>> events(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long ruleId,
            @RequestParam(required = false) String status) {
        List<AlertEventResponse> result = events.findAll().stream()
                .filter(event -> cityId == null || cityId.equals(event.getCity().getId()))
                .filter(event -> ruleId == null || ruleId.equals(event.getRule().getId()))
                .filter(event -> status == null || status.equalsIgnoreCase(event.getStatus().name()))
                .sorted(Comparator.comparing(event -> event.getLastTriggeredAt(), Comparator.reverseOrder()))
                .map(AlertEventResponse::from)
                .toList();
        return ApiResponse.of(result);
    }
}
