package com.tlias.weather.airquality.api;

import com.tlias.weather.airquality.AirQualityCollectionService;
import com.tlias.weather.airquality.CollectionJobLog;
import com.tlias.weather.airquality.CollectionJobLogRepository;
import com.tlias.weather.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collections")
public class CollectionController {
    private final AirQualityCollectionService collectionService;
    private final CollectionJobLogRepository jobLogs;

    public CollectionController(AirQualityCollectionService collectionService, CollectionJobLogRepository jobLogs) {
        this.collectionService = collectionService;
        this.jobLogs = jobLogs;
    }

    @PostMapping
    public ApiResponse<CollectionJobLogResponse> collectNow() {
        CollectionJobLog jobLog = collectionService.collectAllEnabledCities();
        return ApiResponse.of(CollectionJobLogResponse.from(jobLog));
    }

    @GetMapping
    public ApiResponse<List<CollectionJobLogResponse>> list() {
        List<CollectionJobLogResponse> results = jobLogs.findAll().stream()
                .sorted((left, right) -> right.getStartedAt().compareTo(left.getStartedAt()))
                .map(CollectionJobLogResponse::from)
                .toList();
        return ApiResponse.of(results);
    }
}
