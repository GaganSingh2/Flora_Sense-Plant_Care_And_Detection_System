package org.gagan.florasense.Dto;

import java.time.DayOfWeek;
import java.util.List;

public record TrendingPlantsDashboardResponse(
        DayOfWeek featuredDay,
        TrendingPlantResponse featuredPlant,
        List<TrendingPlantResponse> trendingPlants,
        String message
) {
}
