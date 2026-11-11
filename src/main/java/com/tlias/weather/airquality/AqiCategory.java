package com.tlias.weather.airquality;

/**
 * China MEP air quality index categories, mapped from the numeric AQI value.
 */
public enum AqiCategory {
    EXCELLENT(0, 50, "优", "#00e400", "空气质量令人满意，基本无空气污染，适合各类户外活动。"),
    GOOD(51, 100, "良", "#ffff00", "空气质量可接受，极少数敏感人群应减少长时间户外活动。"),
    LIGHTLY_POLLUTED(101, 150, "轻度污染", "#ff7e00", "易感人群症状有轻度加剧，健康人群出现刺激症状。"),
    MODERATELY_POLLUTED(151, 200, "中度污染", "#ff0000", "进一步加剧易感人群症状，可能对健康人群心脏、呼吸系统有影响。"),
    HEAVILY_POLLUTED(201, 300, "重度污染", "#99004c", "心脏病和肺病患者症状显著加剧，运动耐受力降低。"),
    SEVERELY_POLLUTED(301, 500, "严重污染", "#7e0023", "健康人群运动耐受力降低，有明显强烈症状，应避免户外活动。");

    private final int min;
    private final int max;
    private final String level;
    private final String color;
    private final String healthAdvice;

    AqiCategory(int min, int max, String level, String color, String healthAdvice) {
        this.min = min;
        this.max = max;
        this.level = level;
        this.color = color;
        this.healthAdvice = healthAdvice;
    }

    public static AqiCategory from(int aqi) {
        int clamped = Math.max(0, Math.min(500, aqi));
        for (AqiCategory category : values()) {
            if (clamped >= category.min && clamped <= category.max) {
                return category;
            }
        }
        return SEVERELY_POLLUTED;
    }

    public String level() { return level; }
    public String color() { return color; }
    public String healthAdvice() { return healthAdvice; }
}
