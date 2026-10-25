package org.gagan.florasense.Controller;

import org.gagan.florasense.Dto.PlantDetailsResponse;
import org.gagan.florasense.Dto.PlantIdentificationResponse;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Service.PlantIdentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/plant-identification")
public class PlantIdentificationController {

    @Autowired
    private PlantIdentificationService plantIdentificationService;

    //Upload plant image and identified by the Pl@ntNet API and store in PlantUpload
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<PlantIdentificationResponse> identifyPlant(
            Authentication authentication,
            @RequestParam("file")MultipartFile file
    )
    {
        return plantIdentificationService.identifyPlant(authentication, file);
    }

    @GetMapping("/plantDetails/{uploadId}")
    public ResponseEntity<ResponseStructure<PlantDetailsResponse>> getPlantDetails(
            Authentication authentication,
            @PathVariable Integer uploadId
    )
    {
        PlantDetailsResponse response = plantIdentificationService.getPlantDetails(authentication, uploadId);

        ResponseStructure<PlantDetailsResponse> responseStructure = new ResponseStructure<>();
        responseStructure.setStatusCode(HttpStatus.OK.value());
        responseStructure.setMessage(response.message());
        responseStructure.setData(response);
        return ResponseEntity.status(HttpStatus.OK).body(responseStructure);
    }
}
