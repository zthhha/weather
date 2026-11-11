package com.tlias.weather.airquality;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "collection_job_logs")
public class CollectionJobLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Instant startedAt;
    private Instant finishedAt;
    @Column(nullable = false) private String status;
    @Column(nullable = false) private int totalCities;
    @Column(nullable = false) private int successfulCities;
    @Column(nullable = false) private int failedCities;
    private String errorSummary;

    protected CollectionJobLog() { }
    public CollectionJobLog(Instant startedAt, int totalCities) {
        this.startedAt = startedAt;
        this.totalCities = totalCities;
        this.status = "RUNNING";
    }
    public void finish(int successes, int failures, String errorSummary, Instant finishedAt) {
        this.successfulCities = successes;
        this.failedCities = failures;
        this.errorSummary = errorSummary;
        this.finishedAt = finishedAt;
        this.status = failures == 0 ? "SUCCESS" : (successes == 0 ? "FAILED" : "PARTIAL_SUCCESS");
    }
    public Long getId() { return id; }
    public String getStatus() { return status; }
    public int getTotalCities() { return totalCities; }
    public int getSuccessfulCities() { return successfulCities; }
    public int getFailedCities() { return failedCities; }
}
