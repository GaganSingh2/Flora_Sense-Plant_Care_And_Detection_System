package org.gagan.florasense.Dto;

import org.gagan.florasense.Enum.PlantCategory;

public record TrendingPlantResponse(
        Integer plantId,
        String plantName,
        String scientificName,
        String imageUrl,
        String description,
        PlantCategory plantCategory,
        Long identificationCount,
        Integer rank
) {
}
