package org.gagan.florasense.Dto;

public record UserDashboardResponse(
        String username,
        long countTotalUploads,
        long successfulIdentifications,
        long unIdentifiedAttempts,
        long failedAttempts,
        long totalFavorites,
        long weeklyDiscoveries,
        Double successRate
) {
}
