package org.gagan.florasense.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.gagan.florasense.Enum.CareLevel;
import org.gagan.florasense.Enum.PlantCategory;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class PlantRequest {

    @NotBlank(message = "Plant name is required")
    @Size(max = 100, message = "Plant name must not exceed 100 characters")
    private String plantName;

    @NotBlank(message = "Scientific name is required")
    @Size(max = 150, message = "Scientific name must not exceed 150 characters")
    private String scientificName;

    @NotNull(message = "Plant category is required")
    private PlantCategory  plantCategory;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Medicinal uses are required")
    private String medicinalUses;

    @NotBlank(message = "Benefits are required")
    private String benefits;

    @NotBlank(message = "Disadvantages are required")
    private String disadvantages;

    @NotBlank(message = "Oxygen information is required")
    private String oxygenInformation;

    @NotBlank(message = "Toxicity information is required")
    private String toxicityInformation;

    // Optional - entity defaults will be used if not provided
    private Boolean isIndoor;

    // Optional - entity defaults will be used if not provided
    private Boolean isOutdoor;

    // Optional - entity defaults will be used if not provided
    private Boolean isPetSafe;

    // Optional - entity default is EASY
    private CareLevel careLevel;

    //Optional - entity defaul is true
    private Boolean isActive;

    @NotBlank(message = "Sunlight requirement is required")
    private String sunlightRequirement;

    @NotBlank(message = "Watering frequency is required")
    private String wateringFrequency;

    @NotBlank(message = "Temperature requirement is reuired")
    private String temperatureRequirement;

    @NotBlank(message = "Image URL is required")
    @URL(message = "Invalid image URL")
    private String imageUrl;

    @NotBlank(message = "Source reference is required")
    private String sourceReference;
}

















