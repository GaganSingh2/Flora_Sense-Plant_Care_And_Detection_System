package org.gagan.florasense.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gagan.florasense.Enum.CareLevel;
import org.gagan.florasense.Enum.PlantCategory;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlantResponse {

    private Integer plantId;
    private String plantName;
    private String scientificName;
    private PlantCategory plantCategory;
    private String description;
    private String medicinalUses;
    private String benefits;
    private String disadvantages;
    private String oxygenInformation;
    private String toxicityInformation;
    private Boolean isIndoor;
    private Boolean isOutdoor;
    private Boolean isPetSafe;
    private Boolean isActive;
    private CareLevel careLevel;
    private String sunlightRequirement;
    private String wateringFrequency;
    private String temperatureRequirement;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String sourceReference;

}
