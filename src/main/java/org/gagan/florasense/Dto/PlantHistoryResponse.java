package org.gagan.florasense.Dto;

import org.gagan.florasense.Enum.UploadStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlantHistoryResponse(
        Integer uploadId,
        String imageUrl,
        String imageName,
        BigDecimal confidenceScore,
        UploadStatus uploadStatus,
        LocalDateTime uploadedAt,
        Integer plantId,
        String plantName,
        String scientificName
) {
}
