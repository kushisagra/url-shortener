package com.kushagra.urlshortner.DTO;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsSummary {
    private String shortCode;
    private long totalClicks;
    private int clicksLast7Days;
    private List<Map<String, Object>> byCountry;
    private List<Map<String, Object>> byDevice;
    private List<Map<String, Object>> byBrowser;
}