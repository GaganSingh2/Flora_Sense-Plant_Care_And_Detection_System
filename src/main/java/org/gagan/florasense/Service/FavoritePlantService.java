package org.gagan.florasense.Service;

import org.gagan.florasense.Dto.FavoritePlantResponse;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Entity.FavoritePlant;
import org.gagan.florasense.Entity.PlantInfo;
import org.gagan.florasense.Entity.User;
import org.gagan.florasense.Enum.PlantCategory;
import org.gagan.florasense.Exception.IllegalArgumentException;
import org.gagan.florasense.Exception.ResourceNotFoundException;
import org.gagan.florasense.Repositary.FavoritePlantRepositary;
import org.gagan.florasense.Repositary.PlantInfoRepositary;
import org.gagan.florasense.Repositary.UserRepositary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class FavoritePlantService {

    @Autowired
    private FavoritePlantRepositary favoritePlantRepositary;

    @Autowired
    private UserRepositary userRepositary;

    @Autowired
    private PlantInfoRepositary plantInfoRepositary;

    //Add a Favorite Plant
    public ResponseEntity<ResponseStructure<?>> addFavoritePlant(
            Integer plantId, Authentication authentication)
    {
        //Validate PlantId
        if(plantId == null || plantId < 1){
            throw new org.gagan.florasense.Exception.IllegalArgumentException(
                    "plantId must be greater than 0 or not null");
        }

        //Get username from JWT Authentication
        String username = authentication.getName();

        //Find the User
        User user = userRepositary.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with username: " + username)
                );

        //Find Active Plant
        PlantInfo plantInfo = plantInfoRepositary.findByPlantIdAndIsActiveTrue(plantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("PlantInfo not found with plantId: " + plantId)
                );

        //Check whether plant is already favorite
        Optional<FavoritePlant> existingFavoritePlant =
                favoritePlantRepositary.findByUserAndPlantInfo(user, plantInfo);
        if (existingFavoritePlant.isPresent()) {
            throw new org.gagan.florasense.Exception.IllegalArgumentException(
                    "Plant is already added to favorite plant"
            );
        }

        //Create FavoritePlant
        FavoritePlant favoritePlant = new FavoritePlant();
        favoritePlant.setUser(user);
        favoritePlant.setPlantInfo(plantInfo);

        //Save the FavoritePlant
        FavoritePlant savedFavoritePlant = favoritePlantRepositary.save(favoritePlant);

        //Convert Entity -> Response DTO
        FavoritePlantResponse favoritePlantResponse = mapToFavoritePlantResponse(savedFavoritePlant);
        //Set the Response
        ResponseStructure<FavoritePlantResponse> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.CREATED.value());
        response.setMessage("Plant added to favorite successfully");
        response.setData(favoritePlantResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    //Helper method of Add a Favorite Plant to set Response for UI
    private FavoritePlantResponse mapToFavoritePlantResponse(FavoritePlant favoritePlant) {
        PlantInfo plantInfo = favoritePlant.getPlantInfo();
        FavoritePlantResponse favoritePlantResponse = new FavoritePlantResponse();

        favoritePlantResponse.setFavoriteId(favoritePlant.getFavoriteId());
        favoritePlantResponse.setPlantId(plantInfo.getPlantId());
        favoritePlantResponse.setPlantName(plantInfo.getPlantName());
        favoritePlantResponse.setScientificName(plantInfo.getScientificName());
        favoritePlantResponse.setPlantCategory(plantInfo.getPlantCategory());
        favoritePlantResponse.setImageUrl(plantInfo.getImageUrl());
        favoritePlantResponse.setIsIndoor(plantInfo.getIsIndoor());
        favoritePlantResponse.setIsOutdoor(plantInfo.getIsOutdoor());
        favoritePlantResponse.setIsPetSafe(plantInfo.getIsPetSafe());
        favoritePlantResponse.setCareLevel(plantInfo.getCareLevel());
        favoritePlantResponse.setSunlightRequirement(plantInfo.getSunlightRequirement());
        favoritePlantResponse.setWateringFrequency(plantInfo.getWateringFrequency());
        favoritePlantResponse.setCreatedAt(plantInfo.getCreatedAt());
        return favoritePlantResponse;
    }

    //Get all my favorites plants
    //Support search, filters, sorting and pagination
    public ResponseEntity<ResponseStructure<?>> getAllFavoritePlants(
            Authentication authentication, String search,
            Boolean indoor, String category,
            int page, int size, String sortBy, String sortOrder)
    {
        //Validate authentication
        if(authentication == null || !authentication.isAuthenticated()
            || authentication.getName() == null)
        {
            throw new org.gagan.florasense.Exception.IllegalArgumentException(
                    "User must be authenticated or logged in"
            );
        }

        //Validate Pagination
        if(page < 0){
            throw new  org.gagan.florasense.Exception.IllegalArgumentException(
                    "Page must be greater than or equal to 0"
            );
        }
        if (size < 1 || size > 100){
            throw new  org.gagan.florasense.Exception.IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        //Validate sorting order
        if (sortOrder == null
                || !(sortOrder.equalsIgnoreCase("asc")
                || sortOrder.equalsIgnoreCase("desc")))
        {
            throw new  org.gagan.florasense.Exception.IllegalArgumentException(
                    "Invalid sorting order. Allowed values: asc, desc"
            );
        }

        //Validate sort field and map it to the entity property
        Map<String, String> allowedSortFields = Map.of(
                "favoriteId", "favoriteId",
                "createdAt", "createdAt",
                "plantName", "plantInfo.plantName",
                "scientificName", "plantInfo.scientificName",
                "category", "plantInfo.plantCategory",
                "careLevel", "plantInfo.careLevel"
        );

        if (sortBy == null || !allowedSortFields.containsKey(sortBy)){
            throw new  org.gagan.florasense.Exception.IllegalArgumentException(
                    "Invalid sort field. Allowed values: "
                            +allowedSortFields.keySet()
            );
        }

        //Validate and convert category
        PlantCategory plantCategory = null;
        if (category != null && !category.isBlank()){
            try {
                plantCategory = PlantCategory.valueOf(
                        category.trim().toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException e) {
                throw new  org.gagan.florasense.Exception.IllegalArgumentException(
                        "Invalid category. Allowed values: "
                            + Arrays.toString(PlantCategory.values())
                );
            }
        }

        //Normalize search
        String searchText = search == null ? "" : search.trim();

        //Find authenticated user
        String username = authentication.getName();
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with username: " + username)
                );

        //Create sorting and pagination
        Sort.Direction sortDirection =
                sortOrder.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
        Sort sort = Sort.by(sortDirection, allowedSortFields.get(sortBy));

        Pageable pageable =  PageRequest.of(page, size, sort);

        //Fetch this user's matching active favorite plants
        Page<FavoritePlant> favoritePlants =
                favoritePlantRepositary.searchMyFavoritesPlants(
                    user,
                    searchText,
                    indoor,
                    plantCategory,
                    pageable
                );
        if (favoritePlants.getTotalElements() == 0){
            throw new ResourceNotFoundException("No favorite plants found");
        }
        //Convert entities to response DTOs
        Page<FavoritePlantResponse> favoritePlantResponses =
                favoritePlants.map(this::mapToFavoritePlantResponse);

        //Build Response
        ResponseStructure<Page<FavoritePlantResponse>> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Favorite plants fetched successfully");
        response.setData(favoritePlantResponses);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Check whether a plant is already in favorites
    public ResponseEntity<ResponseStructure<?>> checkFavoritePlant(
            Authentication authentication, Integer plantId)
    {
        //Validate PlantId
        if (plantId == null || plantId < 1){
            throw new  org.gagan.florasense.Exception.IllegalArgumentException(
                    "Plant Id must be greater than 0 or not a null"
            );
        }

        //Get username from JWT Authentication
        String username =  authentication.getName();

        //Find the logged-in user
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with username: " + username)
                );

        //Find the active plant
        PlantInfo plantInfo = plantInfoRepositary.findByPlantIdAndIsActiveTrue(plantId)
                .orElseThrow(()->
                        new ResourceNotFoundException(
                                "PlantInfo not found with plantId: " + plantId
                        )
                );

        //Check whether this plant is already a favorite
        boolean isFavorite =
                favoritePlantRepositary
                        .findByUserAndPlantInfo(user, plantInfo)
                        .isPresent();

        ResponseStructure<Boolean> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage(
                isFavorite
                        ? "Plant is already in favorites"
                        : "Plant is not in favorites"
        );
        response.setData(isFavorite);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Remove Plant from favorites
    public ResponseEntity<ResponseStructure<?>> removeFavoritePlant(
            Authentication authentication, Integer plantId)
    {
        //Validate PlantId
        if (plantId == null || plantId < 1){
            throw new  org.gagan.florasense.Exception.IllegalArgumentException(
                    "PlantId must be greater than 0"
            );
        }

        //Fetch username  from JWT Authentication
        String username = authentication.getName();

        //Find the logged-in user
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with username: " + username)
                );

        //Find the Plant
        PlantInfo plantInfo =
                plantInfoRepositary.findByPlantId(plantId)
                        .orElseThrow(()->
                                new  ResourceNotFoundException("PlantInfo not found with plantId: " + plantId)
                        );

        //Find this user's favorite record
        FavoritePlant favoritePlant =
                favoritePlantRepositary.findByUserAndPlantInfo(user, plantInfo)
                        .orElseThrow(()->
                                new ResourceNotFoundException("Plant is not present in your favorite plant")
                        );

        //Delete only the favorite record
        favoritePlantRepositary.delete(favoritePlant);

        //Set Response
        ResponseStructure<String> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Plant removed from favorite succesfully");
        response.setData(null);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Get total number of my favorite plants
    public ResponseEntity<ResponseStructure<?>> getFavoritePlantCount(Authentication authentication) {
        //Validate authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new  org.gagan.florasense.Exception.IllegalArgumentException(
                    "User must be authenticated"
            );
        }

        //Get username from JWT Authentication
        String username = authentication.getName();

        //Find the logged-in user
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with username: " + username)
                );

        long favoriteCount = favoritePlantRepositary.countByUser(user);

        //Prepare Response
        ResponseStructure<Long> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Favorite plants counts fetched successfully");
        response.setData(favoriteCount);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}














