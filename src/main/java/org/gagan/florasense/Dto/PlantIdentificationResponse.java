package org.gagan.florasense.Dto;

import org.gagan.florasense.Enum.UploadStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlantIdentificationResponse(
        Integer uploadId,
        String imageUrl,
        UploadStatus uploadStatus,
        String plantName,
        String scientificName,
        BigDecimal confidenceScore,
        LocalDateTime uploadAt,
        Boolean catalogueMatch,
        String message
) {
}
