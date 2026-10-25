package org.gagan.florasense.Controller;

import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Enum.PlantCategory;
import org.gagan.florasense.Service.FavoritePlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
public class FavoritePlantController {

    @Autowired
    private FavoritePlantService favoritePlantService;

    //Add a plant to favorites
    @PostMapping("/addFavoritePlant/{plantId}")
    public ResponseEntity<ResponseStructure<?>> addFavoritePlant(
            @PathVariable Integer plantId, Authentication authentication) {
        return favoritePlantService.addFavoritePlant(plantId, authentication);
    }

    //Get all my favorites plants
    //Support search, filters, sorting and pagination
    @GetMapping("allFavoritePlants")
    public ResponseEntity<ResponseStructure<?>> getAllFavoritePlants(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean indoor,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
            )
    {
            return favoritePlantService.getAllFavoritePlants(
                    authentication,
                    search,
                    indoor,
                    category,
                    page,
                    size,
                    sortBy,
                    sortOrder
            );
    }

    //Check whether a plant is already in favorites(helpful when user open plant details page,
    // if it is favorite then heart show with red color otherwise outline i.e we can add as a favorite)
    @GetMapping("/{plantId}/check")
    public ResponseEntity<ResponseStructure<?>> checkFavoritePlant(
            Authentication authentication,
            @PathVariable Integer plantId
    ){
        return favoritePlantService.checkFavoritePlant(authentication, plantId);
    }

    //Remove Plant from favorites
    @DeleteMapping("/removeFavoritePlant/{plantId}")
    public ResponseEntity<ResponseStructure<?>> removeFavoritePlant(
            Authentication authentication,
            @PathVariable Integer plantId
    ){
        return favoritePlantService.removeFavoritePlant(authentication, plantId);
    }

    //Get total number of my favorite plants
    @GetMapping("/count")
    public ResponseEntity<ResponseStructure<?>> getFavoritePlantCount(
            Authentication authentication
    ){
        return favoritePlantService.getFavoritePlantCount(authentication);
    }
}













