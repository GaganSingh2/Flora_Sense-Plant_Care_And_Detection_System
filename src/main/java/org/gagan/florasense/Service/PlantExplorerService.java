package org.gagan.florasense.Service;

import org.gagan.florasense.Dto.PlantCatalogueResponse;
import org.gagan.florasense.Dto.PlantExplorerDetailsRespones;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.persistence.criteria.Predicate;
import org.springframework.web.bind.annotation.GetMapping;

@Service
public class PlantExplorerService {

    @Autowired
    private PlantInfoRepositary plantInfoRepositary;

    //Only allow sorting by these PlantInfo entity properties.
    private static final Map<String, String> ALLOWED_SORT_FIELDS =
            Map.of(
                    "plantName", "plantName",
                    "scientificName", "scientificName",
                    "plantCategory", "plantCategory",
                    "careLevel", "careLevel",
                    "createdAt", "createdAt"
            );

    //Get all active plants, with optional search and filters
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseStructure<Page<PlantCatalogueResponse>>> getPlantsExplorer(
            String keyword, String category,
            String careLevel, Boolean isPetSafe,
            int page, int size,
            String sortBy, String sortOrder)
    {
        //Validate pagination
        if(page < 0){
            throw new IllegalArgumentException("Page number must be greater than or equal to 0.");
        }

        if(size < 1 || size > 100){
            throw new IllegalArgumentException("Size must be between 1 and 100.");
        }

        //Validate and resolve the sorting field
        String sortProperty = ALLOWED_SORT_FIELDS.get(sortBy);

        if (sortProperty == null){
            throw new IllegalArgumentException(
                    "Unsupported sort field: ."+sortBy
            );
        }

        //Validate sorting direction
        if (sortOrder == null ||
                !(sortOrder.equalsIgnoreCase("asc")
                || sortOrder.equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("Direction must be either asc or desc.");
        }

        Sort.Direction sortDirection =
                sortOrder.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        //Add a stable secondary sort for consistent pagination
        Sort sort = Sort.by(sortDirection, sortProperty)
                .and(Sort.by(Sort.Direction.ASC, "plantId"));

        Pageable pageable = PageRequest.of(page, size, sort);

        //Build search and filter conditions dynamically
        Specification<PlantInfo> specification =
                (root, criteriaQuery, criteriaBuilder) ->{
                    List<Predicate> predicates = new ArrayList<>();

                    //Show active plants only
                    predicates.add(
                            criteriaBuilder.isTrue(root.get("isActive"))
                    );

                    //Search common name, scientific name, description,
                    //medicinal uses, and toxicity information
                    if(keyword != null && !keyword.isBlank()){
                        String search = "%"
                                + keyword.trim().toLowerCase(Locale.ROOT)
                                + "%";
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("plantName")),
                                        search
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("scientificName")),
                                        search
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("description")),
                                        search
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("medicinalUses")),
                                        search
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("toxicityInformation")),
                                        search
                                )
                        ));
                    }

                    //Plant Category filter: HERB, FERN, FLOWERING etc.
                    if (category != null && !category.isBlank()) {
                        PlantCategory plantCategory;
                        try{
                            plantCategory = PlantCategory.valueOf(
                                    category.trim().trim().toUpperCase(Locale.ROOT)
                            );
                        }
                        catch(IllegalArgumentException e){
                            throw new IllegalArgumentException("Invalid category: "+category);
                        }

                        predicates.add(
                                criteriaBuilder.equal(
                                        root.get("plantCategory"),
                                        plantCategory
                                )
                        );
                    }

                    //Care level enum filter
                    if(careLevel != null && !careLevel.isBlank()){
                        CareLevel plantCareLevel;
                        try{
                            plantCareLevel = CareLevel.valueOf(
                                    careLevel.trim().toUpperCase(Locale.ROOT)
                            );
                        }
                        catch(IllegalArgumentException e){
                            throw new IllegalArgumentException("Invalid careLevel: "+careLevel);
                        }

                        predicates.add(
                                criteriaBuilder.equal(
                                        root.get("careLevel"),
                                        plantCareLevel
                                )
                        );
                    }

                    //Pet-safety filter
                    if (isPetSafe != null){
                        predicates.add(
                                criteriaBuilder.equal(
                                        root.get("isPetSafe"),
                                        isPetSafe
                                )
                        );
                    }
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                };

        //Query databse, paginate and convert entities to DTOs
        Page<PlantCatalogueResponse> plantCatalogueResponses =
                plantInfoRepositary.findAll(specification, pageable)
                        .map(this::toCatalogueResponse);

        if (plantCatalogueResponses.getTotalElements() == 0){
            throw new ResourceNotFoundException("No plants found"+
                    "Try a different plant name or remove some filters to discover more plants");
        }

        ResponseStructure<Page<PlantCatalogueResponse>> responseStructure = new ResponseStructure<>();
        responseStructure.setStatusCode(HttpStatus.OK.value());
        responseStructure.setMessage("Plant catalogue found");
        responseStructure.setData(plantCatalogueResponses);

        return ResponseEntity.status(HttpStatus.OK).body(responseStructure);
    }
    //helper method to manipulate the Catalogue Response
    private PlantCatalogueResponse toCatalogueResponse(PlantInfo plantInfo) {
        return new PlantCatalogueResponse(
                plantInfo.getPlantId(),
                plantInfo.getPlantName(),
                plantInfo.getScientificName(),
                plantInfo.getPlantCategory(),
                plantInfo.getImageUrl(),
                plantInfo.getIsIndoor(),
                plantInfo.getIsOutdoor(),
                plantInfo.getIsPetSafe(),
                plantInfo.getCareLevel(),
                plantInfo.getSunlightRequirement(),
                plantInfo.getWateringFrequency()
        );
    }


    //Get details of a single plant when user clicks on Plant Card on Plant Explorer page
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseStructure<PlantExplorerDetailsRespones>> getPlantExplorerDetail(Integer plantId) {
        //Validate plantId
        if(plantId == null || plantId < 1){
            throw new IllegalArgumentException("Plant ID must be a positive number");
        }

        //Find the plant, but only if it is active
        PlantInfo plantInfo = plantInfoRepositary
                .findByPlantIdAndIsActiveTrue(plantId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Active plant not found with ID: "+plantId)
                );

        PlantExplorerDetailsRespones plantExplorerDetailsRespones = new PlantExplorerDetailsRespones(
                plantInfo.getPlantId(),
                plantInfo.getPlantName(),
                plantInfo.getScientificName(),

                plantInfo.getPlantCategory(),

                plantInfo.getImageUrl(),
                plantInfo.getDescription(),

                plantInfo.getCareLevel(),

                plantInfo.getSunlightRequirement(),
                plantInfo.getWateringFrequency(),
                plantInfo.getTemperatureRequirement(),

                plantInfo.getMedicinalUses(),
                plantInfo.getBenefits(),
                plantInfo.getOxygenInformation(),
                plantInfo.getToxicityInformation(),
                plantInfo.getDisadvantages(),

                plantInfo.getIsIndoor(),
                plantInfo.getIsOutdoor(),
                plantInfo.getIsPetSafe(),

                plantInfo.getSourceReference(),
                plantInfo.getCreatedAt(),
                plantInfo.getModifiedAt()
        );

        //Create Response
        ResponseStructure<PlantExplorerDetailsRespones> responseStructure = new ResponseStructure<>();
        responseStructure.setStatusCode(HttpStatus.OK.value());
        responseStructure.setMessage("Plant details display successfully");
        responseStructure.setData(plantExplorerDetailsRespones);

        return ResponseEntity.status(HttpStatus.OK).body(responseStructure);
    }
}
















