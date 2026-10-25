package org.gagan.florasense.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gagan.florasense.Enum.CareLevel;
import org.gagan.florasense.Enum.PlantCategory;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "plant_info")
public class PlantInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plant_id")
    private Integer plantId;

    @Column(name = "plant_name", nullable = false, unique = true)
    private String plantName;

    @Column(name = "scientific_name", nullable = false)
    private  String scientificName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private PlantCategory plantCategory;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "medicinal_uses", nullable = false, columnDefinition = "TEXT")
    private String medicinalUses;

    @Column(name = "benefits", nullable = false, columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "disadvantages", nullable = false, columnDefinition = "TEXT")
    private String disadvantages;

    @Column(name = "oxygen_information", nullable = false, columnDefinition = "TEXT")
    private String oxygenInformation;

    @Column(name = "toxicity_information", nullable = false, columnDefinition = "TEXT")
    private String toxicityInformation;

    @Column(name = "is_indoor", nullable = false)
    private Boolean isIndoor = false; //if ADMIN don't set by default set this value

    @Column(name = "is_outdoor", nullable = false)
    private Boolean isOutdoor = true; //if ADMIN don't set by default set this value

    @Column(name="is_pet_safe", nullable = false)
    private Boolean isPetSafe = true; //if ADMIN don't set by default set this value

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "care_level", nullable = false)
    private CareLevel careLevel = CareLevel.EASY; //if ADMIN failed to set CareLevel then JVM set this CareLevel as defaul

    @Column(name = "sunlight_requirement", nullable = false)
    private String sunlightRequirement;

    @Column(name = "watering_frequency", nullable = false)
    private String wateringFrequency;

    @Column(name = "temperature_requirement", nullable = false)
    private String temperatureRequirement;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "source_reference", nullable = false, columnDefinition = "TEXT")
    private String sourceReference;

    @PrePersist //Execute this method just before JPA inserts a new record into the database.
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        modifiedAt = now;
    }

    @PreUpdate //Execute this method just before JPA updates an existing record in the database.
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}
