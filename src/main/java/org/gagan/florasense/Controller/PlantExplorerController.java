package org.gagan.florasense.Controller;

import org.gagan.florasense.Dto.PlantCatalogueResponse;
import org.gagan.florasense.Dto.PlantExplorerDetailsRespones;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Enum.CareLevel;
import org.gagan.florasense.Enum.PlantCategory;
import org.gagan.florasense.Service.PlantExplorerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plants/explorer")
public class PlantExplorerController {

    @Autowired
    private PlantExplorerService plantExplorerService;

    //Get all active plants, with optional search and filters
    @GetMapping("plantCard")
    public ResponseEntity<ResponseStructure<Page<PlantCatalogueResponse>>> getPlantsExplorer(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)String category,
            @RequestParam(required = false)String careLevel,
            @RequestParam(required = false) Boolean isPetSafe,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "plantName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder
    )
    {
        return plantExplorerService.getPlantsExplorer(
                keyword,
                category,
                careLevel,
                isPetSafe,
                page,
                size,
                sortBy,
                sortOrder
        );
    }

    //Get details of a single plant when user clicks on Plant Card on Plant Explorer page
    @GetMapping("/plantDetails/{plantId}")
    public ResponseEntity<ResponseStructure<PlantExplorerDetailsRespones>> getPlantExplorerDetail(
            @PathVariable Integer plantId
    )
    {
        return plantExplorerService.getPlantExplorerDetail(plantId);
    }
}
