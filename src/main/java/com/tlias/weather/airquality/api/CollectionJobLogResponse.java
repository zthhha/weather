package com.tlias.weather.airquality.api;

import com.tlias.weather.airquality.CollectionJobLog;

import java.time.Instant;

public record CollectionJobLogResponse(
        Long id,
        Instant startedAt,
        Instant finishedAt,
        String status,
        int totalCities,
        int successfulCities,
        int failedCities,
        String errorSummary
) {
    public static CollectionJobLogResponse from(CollectionJobLog log) {
        return new CollectionJobLogResponse(
                log.getId(), log.getStartedAt(), log.getFinishedAt(), log.getStatus(), log.getTotalCities(),
                log.getSuccessfulCities(), log.getFailedCities(), log.getErrorSummary());
    }
}
