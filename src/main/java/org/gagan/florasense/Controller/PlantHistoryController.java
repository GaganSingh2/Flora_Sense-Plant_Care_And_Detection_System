package org.gagan.florasense.Controller;

import org.gagan.florasense.Dto.PlantDetailsResponse;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Service.PlantHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plant-history")
public class PlantHistoryController {

    @Autowired
    private PlantHistoryService plantHistoryService;

    //Get identification history and Search Box with search, sorting and pagination
    @GetMapping("/allPlantCards")
    public ResponseEntity<ResponseStructure<?>> getAllPlantCardsHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "newest") String sortBy
    ) {
        return plantHistoryService.getAllPlantCardsHistory(
                authentication,
                page, size, keyword, sortBy);
    }

    //Get total identification count
    @GetMapping("/count")
    public ResponseEntity<ResponseStructure<Long>> getAllPlantIdentificationCount(
            Authentication authentication
    )
    {
        return plantHistoryService.getAllPlantIdentificationCount(authentication);
    }

    //Get Plant Details when user click on View Details button on Plant Card
    @GetMapping("/plantDetails/{uploadId}")
    public ResponseEntity<ResponseStructure<PlantDetailsResponse>> getPlantDetailsByUploadId(
            Authentication authentication,
            @PathVariable("uploadId") Integer uploadId
    ){
        PlantDetailsResponse detailsResponse = plantHistoryService.getPlantDetailsByUploadId(authentication, uploadId);
        ResponseStructure<PlantDetailsResponse> response = new ResponseStructure<>();
        response.setMessage(detailsResponse.message());
        response.setStatusCode(HttpStatus.OK.value());
        response.setData(detailsResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Delete one plant history record and image file from local disk if it does not belong to others upload references.
    @DeleteMapping("/deletePlantHistory/{uploadId}")
    public ResponseEntity<ResponseStructure<Void>> deletePlantHistoryByUploadId(
            Authentication authentication,
            @PathVariable("uploadId") Integer uploadId
    )
    {
        plantHistoryService.deletePlantHistoryByUploadId(authentication, uploadId);

        ResponseStructure<Void> response = new ResponseStructure<>();
        response.setMessage("Plant Upload History Deleted Successfully");
        response.setStatusCode(HttpStatus.OK.value());
        response.setData(null);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Export all Plants History in the form of CSV file i.e Excel or Google Sheet(CSV stands for Comma-Separated Values)
    @GetMapping(value = "/export")
    public ResponseEntity<byte[]> exportAllPlantHistory(
            Authentication authentication
    )
    {
        byte[] csvData = plantHistoryService.exportAllPlantHistory(authentication);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"florasense-history.csv\""
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
