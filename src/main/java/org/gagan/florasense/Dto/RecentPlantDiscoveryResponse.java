package org.gagan.florasense.Dto;

import org.gagan.florasense.Enum.PlantCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentPlantDiscoveryResponse(
        Integer uploadId,
        String imageUrl,
        String imageName,
        Integer plantId,
        String plantName,
        String scientificName,
        PlantCategory plantCategory,
        BigDecimal confidenceScore,
        LocalDateTime discoverdAt
) {
}
