package org.gagan.florasense.Dto;

import org.gagan.florasense.Enum.CareLevel;
import org.gagan.florasense.Enum.PlantCategory;

import java.time.LocalDateTime;

public record PlantExplorerDetailsRespones(
        Integer plantId,
        String plantName,
        String scientificName,
        PlantCategory plantCategory,
        String imageUrl,

        //About the species
        String description,

        //Care guide
        CareLevel careLevel,
        String sunlightRequirement,
        String wateringFrequency,
        String temperatureRequirement,

        //Plant Information
        String medicinalUses,
        String benefits,
        String oxygenInformation,
        String toxicityInformation,
        String disadvantage,

        //Suitability
        Boolean isIndoor,
        Boolean isOutdoor,
        Boolean isPetSafe,

        //Additional information
        String sourceReference,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
