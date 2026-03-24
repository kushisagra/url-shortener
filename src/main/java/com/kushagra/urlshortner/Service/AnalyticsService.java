package com.kushagra.urlshortner.Service;

import com.kushagra.urlshortner.DTO.AnalyticsSummary;
import com.kushagra.urlshortner.Entity.ClickEvent;
import com.kushagra.urlshortner.Repository.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;
    private final UserAgentParser userAgentParser;

    // @Async — this method returns immediately to the caller
    // actual execution happens on "analyticsExecutor" thread pool
    @Async("analyticsExecutor")
    public void recordClick(String shortCode,
                            String ipAddress,
                            String userAgent,
                            String referer) {
        try {
            ClickEvent event = ClickEvent.builder()
                    .shortCode(shortCode)
                    .ipAddress(anonymizeIp(ipAddress)) // privacy best practice
                    .country(resolveCountry(ipAddress))
                    .browser(userAgentParser.extractBrowser(userAgent))
                    .os(userAgentParser.extractOs(userAgent))
                    .device(userAgentParser.extractDevice(userAgent))
                    .referer(referer)
                    .build();

            clickEventRepository.save(event);
            log.debug("Recorded click for '{}' — device: {}, browser: {}, country: {}",
                    shortCode, event.getDevice(), event.getBrowser(), event.getCountry());

        } catch (Exception e) {
            // Never let analytics failure affect the redirect
            log.error("Failed to record click for '{}': {}", shortCode, e.getMessage());
        }
    }

    // --- Analytics read methods ---

    public AnalyticsSummary getSummary(String shortCode) {
        long totalClicks = clickEventRepository.countByShortCode(shortCode);

        // Last 7 days breakdown — click count per day
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<ClickEvent> recentClicks = clickEventRepository
                .findByShortCodeAndClickedAtBetween(shortCode, sevenDaysAgo, LocalDateTime.now());

        List<Map<String, Object>> byCountry = clickEventRepository
                .countGroupedByCountry(shortCode);
        List<Map<String, Object>> byDevice  = clickEventRepository
                .countGroupedByDevice(shortCode);
        List<Map<String, Object>> byBrowser = clickEventRepository
                .countGroupedByBrowser(shortCode);

        return AnalyticsSummary.builder()
                .shortCode(shortCode)
                .totalClicks(totalClicks)
                .clicksLast7Days(recentClicks.size())
                .byCountry(byCountry)
                .byDevice(byDevice)
                .byBrowser(byBrowser)
                .build();
    }

    // Anonymize last octet of IP for GDPR-friendliness
    // e.g. 192.168.1.42 → 192.168.1.0
    private String anonymizeIp(String ip) {
        if (ip == null) return null;
        int lastDot = ip.lastIndexOf('.');
        if (lastDot == -1) return ip; // IPv6 or malformed — skip
        return ip.substring(0, lastDot) + ".0";
    }

    // Basic country resolution — returns "IN" for Indian IPs etc.
    // In a real system you'd use MaxMind GeoIP here
    private String resolveCountry(String ip) {
        if (ip == null) return "Unknown";
        // Localhost / loopback — happens during local development
        if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) return "Local";
        return "Unknown"; // placeholder — we'll improve this below
    }
}