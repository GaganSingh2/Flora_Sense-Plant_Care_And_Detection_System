package org.gagan.florasense.Controller;

import jakarta.validation.Valid;
import org.gagan.florasense.Dto.PlantRequest;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Entity.PlantInfo;
import org.gagan.florasense.Enum.CareLevel;
import org.gagan.florasense.Enum.PlantCategory;
import org.gagan.florasense.Service.AdminPlantInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/plants")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlantInfoController {

    @Autowired
    private AdminPlantInfoService adminPlantInfoService;

    //Add a new Plant
    @PostMapping("/addNewPlant")
    public ResponseEntity<ResponseStructure<?>> addPlant(@Valid @RequestBody PlantRequest request) {
        return adminPlantInfoService.addPlant(request);
    }

    //Get all plants by Searching, Filtering, SOrting and Pagination
    @GetMapping("/allPlants")
    public ResponseEntity<ResponseStructure<?>> getAllPlants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PlantCategory category,
            @RequestParam(required = false) CareLevel careLevel,
            @RequestParam(required = false) Boolean petSafe,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
            )
    {
        return adminPlantInfoService.getAllPlants(search, category, careLevel,petSafe,
                page, size, sortBy, sortOrder);
    }

    //Get plant details by using PlantId
    @GetMapping("/plantId/{plantId}")
    public ResponseEntity<ResponseStructure<?>> getPlantByPlantId(@PathVariable("plantId") Integer plantId) {
        return adminPlantInfoService.getPlantByPlantId(plantId);
    }

    //Update Plant details
    @PutMapping("/updatePlant/{plantId}")
    public ResponseEntity<ResponseStructure<?>> updatePlantInfo(
            @PathVariable("plantId") Integer plantId,
            @Valid @RequestBody PlantRequest request)
    {
        return adminPlantInfoService.updatePlantInfo(plantId, request);
    }

    //Delete a Plant by using PlantId by Making InActive
    @DeleteMapping("/deletePlant/{plantId}")
    public  ResponseEntity<Void> deletePlantByPlantId(@PathVariable("plantId") Integer plantId) {
        adminPlantInfoService.deletePlantByPlantId(plantId);
        return ResponseEntity.noContent().build();
    }

    //Restore a InActive Plant
    @PatchMapping("/{plantId}/restorePlant")
    public ResponseEntity<ResponseStructure<?>> restorePlant(@PathVariable Integer plantId) {
        return adminPlantInfoService.restorePlant(plantId);
    }

    //Export all Plants in the form of CSV file i.e Excel or Google Sheet(CSV stands for Comma-Separated Values)
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportPlants(){
        byte[] csv = adminPlantInfoService.exportPlants();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plants.csv")
                .body(csv);
    }
}
