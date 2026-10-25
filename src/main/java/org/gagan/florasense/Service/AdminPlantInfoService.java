package org.gagan.florasense.Service;

import jakarta.validation.Valid;
import org.gagan.florasense.Dto.PlantRequest;
import org.gagan.florasense.Dto.PlantResponse;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Entity.PlantInfo;
import org.gagan.florasense.Enum.CareLevel;
import org.gagan.florasense.Enum.PlantCategory;
import org.gagan.florasense.Exception.IllegalArgumentException;
import org.gagan.florasense.Exception.ResourceNotFoundException;
import org.gagan.florasense.Repositary.PlantInfoRepositary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AdminPlantInfoService {

    @Autowired
    private PlantInfoRepositary plantInfoRepositary;

    //Add new Plant
    public ResponseEntity<ResponseStructure<?>> addPlant(@Valid PlantRequest request) {
        //Check duplicate Plant name
        Optional<PlantInfo> existingPlant = plantInfoRepositary.findByPlantNameIgnoreCase(request.getPlantName());
        if (existingPlant.isPresent()) {
            PlantInfo plantInfo = existingPlant.get();
            if (!plantInfo.getIsActive()){
                throw  new org.gagan.florasense.Exception.IllegalArgumentException(
                        "Plant already exists but it is inactive. "+
                         "Please restore the existing plant."
                );
            }
            throw new org.gagan.florasense.Exception.IllegalArgumentException(
                    "Plant with name '" +
                     request.getPlantName()+"' already exists."
            );
        }

        PlantInfo plantInfo = new PlantInfo();
        plantInfo.setPlantName(request.getPlantName());
        plantInfo.setScientificName(request.getScientificName());
        plantInfo.setPlantCategory(request.getPlantCategory());

        plantInfo.setDescription(request.getDescription());
        plantInfo.setMedicinalUses(request.getMedicinalUses());
        plantInfo.setBenefits(request.getBenefits());
        plantInfo.setDisadvantages(request.getDisadvantages());

        plantInfo.setOxygenInformation(request.getOxygenInformation());
        plantInfo.setToxicityInformation(request.getToxicityInformation());

        //if ADMIN does not provide these value.
        //entity defaults will remain.
        if (request.getIsIndoor() != null){
            plantInfo.setIsIndoor(request.getIsIndoor());
        }
        if (request.getIsOutdoor() != null){
            plantInfo.setIsOutdoor(request.getIsOutdoor());
        }
        if (request.getIsPetSafe() != null){
            plantInfo.setIsPetSafe(request.getIsPetSafe());
        }
        if (request.getIsActive() != null){
            plantInfo.setIsActive(request.getIsActive());
        }
        if (request.getCareLevel() != null){
            plantInfo.setCareLevel(request.getCareLevel());
        }


        plantInfo.setSunlightRequirement(request.getSunlightRequirement());
        plantInfo.setWateringFrequency(request.getWateringFrequency());
        plantInfo.setTemperatureRequirement(request.getTemperatureRequirement());

        plantInfo.setImageUrl(request.getImageUrl());
        plantInfo.setSourceReference(request.getSourceReference());

        PlantInfo savedPlantInfo = plantInfoRepositary.save(plantInfo);
        ResponseStructure<PlantInfo> response = new ResponseStructure<>();
        response.setMessage("Plant added successfully");
        response.setStatusCode(HttpStatus.CREATED.value());
        response.setData(savedPlantInfo);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //Get all plants by Searching, Filtering, Sorting and Pagination
    public ResponseEntity<ResponseStructure<?>> getAllPlants(
            String search, PlantCategory category,
            CareLevel careLevel, Boolean petSafe,
            int page, int size, String sortBy, String sortOrder)
    {
        //Validate Pagination
        if(page < 0 || size < 1 || size > 100){
            throw new org.gagan.florasense.Exception.IllegalArgumentException("Page must be non-negative or size must be between 1 and 100");
        }

        //Validate sorting field
        Set<String> allowedSortFields = Set.of(
                "plantId",
                "plantName",
                "scientificName",
                "plantCategory",
                "careLevel",
                "createdAt",
                "modifiedAt"
        );

        //Validating Sorting Fields
        if(!allowedSortFields.contains(sortBy)){
            throw new org.gagan.florasense.Exception.IllegalArgumentException(
                    "Invalid sort field. Allowed fields: "+allowedSortFields
            );
        }

        //Validate Sorting Order
        if(!sortOrder.equalsIgnoreCase("asc")
                && !sortOrder.equalsIgnoreCase("desc")){
            throw new org.gagan.florasense.Exception.IllegalArgumentException("Sort order must be either asc or desc");
        }

        //Normalize Search
        String normalizedSearch = search == null || search.isBlank() ? "" : search.trim();

        //Create Sorting
        Sort.Direction sortingOrder =
                sortOrder.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
        Sort sort = Sort.by(sortingOrder, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        //Fetch Plants now
        Page<PlantInfo> plantPage =
                plantInfoRepositary.searchPlants(
                        normalizedSearch,
                        category,
                        careLevel,
                        petSafe,
                        pageable
                );

        //Convert Entity -> PlantResponse DTO
        Page<PlantResponse> plants =
                plantPage.map(plant ->
                        new PlantResponse(
                                plant.getPlantId(),
                                plant.getPlantName(),
                                plant.getScientificName(),
                                plant.getPlantCategory(),
                                plant.getDescription(),
                                plant.getMedicinalUses(),
                                plant.getBenefits(),
                                plant.getDisadvantages(),
                                plant.getOxygenInformation(),
                                plant.getToxicityInformation(),
                                plant.getIsIndoor(),
                                plant.getIsOutdoor(),
                                plant.getIsPetSafe(),
                                plant.getIsActive(),
                                plant.getCareLevel(),
                                plant.getSunlightRequirement(),
                                plant.getWateringFrequency(),
                                plant.getTemperatureRequirement(),
                                plant.getImageUrl(),
                                plant.getCreatedAt(),
                                plant.getModifiedAt(),
                                plant.getSourceReference()
                        )
                );
        ResponseStructure<Page<PlantResponse>> response = new ResponseStructure<>();
        response.setMessage(
                plants.isEmpty()
                    ? "No plants found"
                    : "Plants fetched successfully"
        );
        response.setStatusCode(HttpStatus.OK.value());
        response.setData(plants);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Get plant details by using PlantId
    public ResponseEntity<ResponseStructure<?>> getPlantByPlantId(Integer plantId) {
        if(plantId < 1){
            throw new org.gagan.florasense.Exception.IllegalArgumentException("PlantId must be greater than 0");
        }
        PlantInfo plantInfo = plantInfoRepositary.findByPlantId(plantId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Plant Not Found with PlantId: " + plantId)
                );
        PlantResponse plantResponse = new PlantResponse(
                plantInfo.getPlantId(),
                plantInfo.getPlantName(),
                plantInfo.getScientificName(),
                plantInfo.getPlantCategory(),
                plantInfo.getDescription(),
                plantInfo.getMedicinalUses(),
                plantInfo.getBenefits(),
                plantInfo.getDisadvantages(),
                plantInfo.getOxygenInformation(),
                plantInfo.getToxicityInformation(),
                plantInfo.getIsIndoor(),
                plantInfo.getIsOutdoor(),
                plantInfo.getIsPetSafe(),
                plantInfo.getIsActive(),
                plantInfo.getCareLevel(),
                plantInfo.getSunlightRequirement(),
                plantInfo.getWateringFrequency(),
                plantInfo.getTemperatureRequirement(),
                plantInfo.getImageUrl(),
                plantInfo.getCreatedAt(),
                plantInfo.getModifiedAt(),
                plantInfo.getSourceReference()
        );
        ResponseStructure<PlantResponse> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.FOUND.value());
        response.setMessage("Plant details fetched successfully");
        response.setData(plantResponse);
        return  ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    //Update Plant details
    public ResponseEntity<ResponseStructure<?>> updatePlantInfo(Integer plantId, @Valid PlantRequest request) {
        if (plantId < 1){
            throw new org.gagan.florasense.Exception.IllegalArgumentException("PlantId must be greater than 0");
        }
        PlantInfo plantInfo = plantInfoRepositary.findByPlantId(plantId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Plant Not Found with PlantId: " + plantId)
                );

        //Check duplicate plant name updating
        if(!plantInfo.getPlantName().equalsIgnoreCase(request.getPlantName())
                && plantInfoRepositary.existsByPlantNameIgnoreCase(request.getPlantName())) {
            throw new IllegalArgumentException(
                    "Plant with name '" + request.getPlantName() + "' already exists"
            );
        }

        //Update Basic details
        plantInfo.setPlantName(request.getPlantName());
        plantInfo.setScientificName(request.getScientificName());
        plantInfo.setPlantCategory(request.getPlantCategory());
        plantInfo.setDescription(request.getDescription());
        plantInfo.setMedicinalUses(request.getMedicinalUses());
        plantInfo.setBenefits(request.getBenefits());
        plantInfo.setDisadvantages(request.getDisadvantages());
        plantInfo.setOxygenInformation(request.getOxygenInformation());
        plantInfo.setToxicityInformation(request.getToxicityInformation());

        //Update optional fields
        if(request.getIsIndoor() != null) {
            plantInfo.setIsIndoor(request.getIsIndoor());
        }
        if (request.getIsOutdoor() != null) {
            plantInfo.setIsOutdoor(request.getIsOutdoor());
        }
        if (request.getIsPetSafe() != null) {
            plantInfo.setIsPetSafe(request.getIsPetSafe());
        }
        if(request.getIsActive() != null) {
            plantInfo.setIsActive(request.getIsActive());
        }
        if (request.getCareLevel() != null) {
            plantInfo.setCareLevel(request.getCareLevel());
        }

        //Care requirements
        plantInfo.setSunlightRequirement(request.getSunlightRequirement());
        plantInfo.setWateringFrequency(request.getWateringFrequency());
        plantInfo.setTemperatureRequirement(request.getTemperatureRequirement());

        //Image and source
        plantInfo.setImageUrl(request.getImageUrl());
        plantInfo.setSourceReference(request.getSourceReference());

        PlantInfo updatedPlant = plantInfoRepositary.save(plantInfo);
        PlantResponse plantResponse = new PlantResponse(
                updatedPlant.getPlantId(),
                updatedPlant.getPlantName(),
                updatedPlant.getScientificName(),
                updatedPlant.getPlantCategory(),
                updatedPlant.getDescription(),
                updatedPlant.getMedicinalUses(),
                updatedPlant.getBenefits(),
                updatedPlant.getDisadvantages(),
                updatedPlant.getOxygenInformation(),
                updatedPlant.getToxicityInformation(),
                updatedPlant.getIsIndoor(),
                updatedPlant.getIsOutdoor(),
                updatedPlant.getIsPetSafe(),
                updatedPlant.getIsActive(),
                updatedPlant.getCareLevel(),
                updatedPlant.getSunlightRequirement(),
                updatedPlant.getWateringFrequency(),
                updatedPlant.getTemperatureRequirement(),
                updatedPlant.getImageUrl(),
                updatedPlant.getCreatedAt(),
                updatedPlant.getModifiedAt(),
                updatedPlant.getSourceReference()
        );
        ResponseStructure<PlantResponse> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Plant details updated successfully");
        response.setData(plantResponse);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Delete a Plant by using PlantId by Making InActive
    public void deletePlantByPlantId(Integer plantId) {
        if (plantId < 1){
            throw new org.gagan.florasense.Exception.IllegalArgumentException("PlantId must be greater than 0");
        }
        PlantInfo plantInfo = plantInfoRepositary.findByPlantId(plantId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Plant Not Found with PlantId: " + plantId)
                );
        if (!plantInfo.getIsActive()) {
            throw new org.gagan.florasense.Exception.IllegalArgumentException("Plant is already Inactive");
        }
        plantInfo.setIsActive(false);
        plantInfoRepositary.save(plantInfo);
    }

    //Restore a InActive plant again
    public ResponseEntity<ResponseStructure<?>> restorePlant(Integer plantId) {
        if (plantId < 1){
            throw new org.gagan.florasense.Exception.IllegalArgumentException("PlantId must be greater than 0");
        }

        PlantInfo plantInfo = plantInfoRepositary.findByPlantId(plantId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Plant Not Found with PlantId: " + plantId)
                );

        if (plantInfo.getIsActive()) {
            throw new org.gagan.florasense.Exception.IllegalArgumentException("Plant is already active");
        }

        plantInfo.setIsActive(true);
        PlantInfo restoredPlant = plantInfoRepositary.save(plantInfo);
        PlantResponse plantResponse = new PlantResponse(
                restoredPlant.getPlantId(),
                restoredPlant.getPlantName(),
                restoredPlant.getScientificName(),
                restoredPlant.getPlantCategory(),
                restoredPlant.getDescription(),
                restoredPlant.getMedicinalUses(),
                restoredPlant.getBenefits(),
                restoredPlant.getDisadvantages(),
                restoredPlant.getOxygenInformation(),
                restoredPlant.getToxicityInformation(),
                restoredPlant.getIsIndoor(),
                restoredPlant.getIsOutdoor(),
                restoredPlant.getIsPetSafe(),
                restoredPlant.getIsActive(),
                restoredPlant.getCareLevel(),
                restoredPlant.getSunlightRequirement(),
                restoredPlant.getWateringFrequency(),
                restoredPlant.getTemperatureRequirement(),
                restoredPlant.getImageUrl(),
                restoredPlant.getCreatedAt(),
                restoredPlant.getModifiedAt(),
                restoredPlant.getSourceReference()
        );
        ResponseStructure<PlantResponse> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Plant details restored successfully");
        response.setData(plantResponse);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Export all Users in the form of CSV file i.e Excel or Google Sheet(CSV stands for Comma-Separated values
    public byte[] exportPlants() {
        List<PlantInfo> plantInfos = plantInfoRepositary.findByIsActiveTrue();
        if(plantInfos.isEmpty()){
            throw new ResourceNotFoundException("Plant Not Found");
        }
        StringBuilder csv = new StringBuilder();

        //CSV Header
        csv.append("Plant ID, Plant Name, Scientific Name, Category,")
                .append("Description, Medicinal Uses, Benefits, Disadvantages, ")
                .append("Oxygen Information, Toxicity Information, Indoor, ")
                .append("Outdoor, Pet Safe, Care Level, Sunlight Requirement, ")
                .append("Watering Frequency, Temperature Requirement, Image Url, ")
                .append("Created At, Modified At, Source Reference\n");

        //CSV Data
        for(PlantInfo plant:  plantInfos){
            csv.append(escapeCsv(plant.getPlantId()))
                    .append(", ")
                    .append(escapeCsv(plant.getPlantName()))
                    .append(", ")
                    .append(escapeCsv(plant.getScientificName()))
                    .append(", ")
                    .append(escapeCsv(plant.getPlantCategory()))
                    .append(", ")
                    .append(escapeCsv(plant.getDescription()))
                    .append(", ")
                    .append(escapeCsv(plant.getMedicinalUses()))
                    .append(", ")
                    .append(escapeCsv(plant.getBenefits()))
                    .append(", ")
                    .append(escapeCsv(plant.getDisadvantages()))
                    .append(", ")
                    .append(escapeCsv(plant.getOxygenInformation()))
                    .append(", ")
                    .append(escapeCsv(plant.getToxicityInformation()))
                    .append(", ")
                    .append(escapeCsv(plant.getIsIndoor()))
                    .append(", ")
                    .append(escapeCsv(plant.getIsOutdoor()))
                    .append(", ")
                    .append(escapeCsv(plant.getIsPetSafe()))
                    .append(", ")
                    .append(escapeCsv(plant.getCareLevel()))
                    .append(", ")
                    .append(escapeCsv(plant.getSunlightRequirement()))
                    .append(", ")
                    .append(escapeCsv(plant.getWateringFrequency()))
                    .append(", ")
                    .append(escapeCsv(plant.getTemperatureRequirement()))
                    .append(", ")
                    .append(escapeCsv(plant.getImageUrl()))
                    .append(", ")
                    .append(escapeCsv(plant.getCreatedAt()))
                    .append(", ")
                    .append(escapeCsv(plant.getModifiedAt()))
                    .append(", ")
                    .append(escapeCsv(plant.getSourceReference()))
                    .append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    //Helper method for generating CSV file
    private String escapeCsv(Object value){
        if (value == null){
            return "";
        }
        String text = value.toString();
        //Escape double quotes
        text = text.replace("\"", "\"\"");

        //If value contains comma, quote or newline, surround the entire value with double quotes
        if(text.contains(",")
            || text.contains("\"")
            || text.contains("\n")
            || text.contains("\r")){
            return "\"" + text + "\"";
        }
        return text;
    }
}














