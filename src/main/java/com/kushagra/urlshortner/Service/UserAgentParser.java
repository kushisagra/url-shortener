package com.kushagra.urlshortner.Service;

import org.springframework.stereotype.Component;

@Component
public class UserAgentParser {

    public String extractBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        // Order matters — check Edge before Chrome, Chrome before Safari
        if (userAgent.contains("Edg/"))             return "Edge";
        if (userAgent.contains("OPR/")
                || userAgent.contains("Opera"))            return "Opera";
        if (userAgent.contains("Chrome"))           return "Chrome";
        if (userAgent.contains("Firefox"))          return "Firefox";
        if (userAgent.contains("Safari")
                && !userAgent.contains("Chrome"))          return "Safari";
        if (userAgent.contains("MSIE")
                || userAgent.contains("Trident"))          return "Internet Explorer";
        return "Other";
    }

    public String extractOs(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Windows NT"))       return "Windows";
        if (userAgent.contains("Mac OS X"))         return "macOS";
        if (userAgent.contains("Android"))          return "Android";
        if (userAgent.contains("iPhone")
                || userAgent.contains("iPad"))             return "iOS";
        if (userAgent.contains("Linux"))            return "Linux";
        return "Other";
    }

    public String extractDevice(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("iPad"))             return "TABLET";
        if (userAgent.contains("Mobile")
                || userAgent.contains("Android")
                || userAgent.contains("iPhone"))           return "MOBILE";
        return "DESKTOP";
    }
}