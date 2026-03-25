package com.kushagra.urlshortner.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(description = "Analytics summary for a shortened URL")
public class AnalyticsSummary {

    @Schema(description = "The short code", example = "aB3xKz")
    private String shortCode;

    @Schema(description = "Total number of clicks", example = "150")
    private long totalClicks;

    @Schema(description = "Number of clicks in the last 7 days", example = "42")
    private int clicksLast7Days;

    @Schema(description = "Click breakdown by country")
    private List<Map<String, Object>> byCountry;

    @Schema(description = "Click breakdown by device type (Mobile, Desktop, Tablet)")
    private List<Map<String, Object>> byDevice;

    @Schema(description = "Click breakdown by browser (Chrome, Firefox, Safari, etc.)")
    private List<Map<String, Object>> byBrowser;
}