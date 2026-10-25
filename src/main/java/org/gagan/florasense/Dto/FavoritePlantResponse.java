package org.gagan.florasense.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gagan.florasense.Entity.FavoritePlant;
import org.gagan.florasense.Enum.CareLevel;
import org.gagan.florasense.Enum.PlantCategory;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FavoritePlantResponse {
    private Integer favoriteId;
    private Integer plantId;
    private String plantName;
    private String scientificName;
    private PlantCategory plantCategory;
    private String imageUrl;
    private Boolean isIndoor;
    private Boolean isOutdoor;
    private Boolean isPetSafe;
    private CareLevel careLevel;
    private String sunlightRequirement;
    private String wateringFrequency;
    private LocalDateTime createdAt;
}
