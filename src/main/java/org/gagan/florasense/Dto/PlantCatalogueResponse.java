package org.gagan.florasense.Dto;

import org.gagan.florasense.Enum.CareLevel;
import org.gagan.florasense.Enum.PlantCategory;

public record PlantCatalogueResponse(
        Integer plantId,
        String plantName,
        String scientificName,
        PlantCategory plantCategory,
        String imageUrl,
        Boolean isIndoor,
        Boolean isOutdoor,
        Boolean isPetSafe,
        CareLevel careLevel,
        String sunlightRequirement,
        String wateringFrequency
) {
}
