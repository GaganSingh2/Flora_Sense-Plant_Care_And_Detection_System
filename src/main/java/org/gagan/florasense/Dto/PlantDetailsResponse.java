package org.gagan.florasense.Dto;

import org.gagan.florasense.Enum.CareLevel;
import org.gagan.florasense.Enum.PlantCategory;
import org.gagan.florasense.Enum.UploadStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlantDetailsResponse(
        //Upload Information
        Integer uploadId,
        String imageUrl,
        UploadStatus uploadStatus,
        BigDecimal confidenceScore,
        LocalDateTime uploadedAt,

        //Plant Information
        Integer plantId,
        String plantName,
        String scientificName,
        PlantCategory plantCategory,
        String plantImageUrl,
        String description,

        //Care guide
        CareLevel careLevel,
        String sunlightRequirement,
        String wateringFrequency,
        String temperatureRequirement,

        //Additional details
        String medicinalUses,
        String oxygenInformation,
        String benefits,
        String disadvantages,
        String toxicityInformation,

        //Plant suitability
        Boolean isIndoor,
        Boolean isOutdoor,
        Boolean isPetSafe,
        String sourceReference,

        Boolean catalogueAvailable,
        String message
) {

}
















