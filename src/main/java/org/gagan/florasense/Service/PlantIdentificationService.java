package org.gagan.florasense.Service;

import org.gagan.florasense.Configuration.PlantNetClient;
import org.gagan.florasense.Dto.PlantDetailsResponse;
import org.gagan.florasense.Dto.PlantIdentificationResponse;
import org.gagan.florasense.Dto.PlantNetResponse;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Entity.PlantInfo;
import org.gagan.florasense.Entity.PlantUpload;
import org.gagan.florasense.Entity.User;
import org.gagan.florasense.Enum.PlantCategory;
import org.gagan.florasense.Enum.UploadStatus;
import org.gagan.florasense.Exception.ResourceNotFoundException;
import org.gagan.florasense.Repositary.PlantInfoRepositary;
import org.gagan.florasense.Repositary.PlantUploadRepositary;
import org.gagan.florasense.Repositary.UserRepositary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class PlantIdentificationService {

    @Autowired
    private PlantNetClient plantNetClient;

    @Autowired
    private UserRepositary userRepositary;

    @Autowired
    private PlantInfoRepositary plantInfoRepositary;

    @Autowired
    private PlantUploadRepositary plantUploadRepositary;

    @Value("${app.upload.plants-dir:uploads/plants}")
    private String uploadDirectory;

    //Upload plant image and identified by the Pl@ntNet API and store in PlantUpload
    public ResponseEntity<PlantIdentificationResponse> identifyPlant(
            Authentication authentication,
            MultipartFile file
    ){
        //Validate the uploaded file
        if(file == null || file.isEmpty()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a plant image"
            );
        }

        String contentType = file.getContentType();

        //Validate image extension bcz only png and jpeg supported
        if(contentType == null ||
                !(contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/png"))
        )
        {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only JPEG or PNG images are supported"
            );
        }

        //Validate Image size, Max Size is 10 MB
        if (file.getSize() > 10L * 1024 * 1024){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Image size must not exceed 10 MB"
            );
        }

        //Find the logged-in user using JWT authentication
        String userName = authentication.getName();
        User user = userRepositary.findByUsername(userName)
                .orElseThrow(()->
                        new ResourceNotFoundException(
                                "User not found with UserId: " + userName)
                );

        //Generate a safe filename and save the image
        String extension =
                contentType.equalsIgnoreCase("image/png")
                        ? ".png" : ".jpg";

        String fileName = UUID.randomUUID() + extension;

        Path directory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        Path imagePath = directory.resolve(fileName).normalize();

        if (!imagePath.startsWith(directory)){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid image path"
            );
        }

        byte[] imageBytes;

        try{
            imageBytes = file.getBytes();
            Files.createDirectories(directory);
            Files.write(imagePath, imageBytes); //Save the actual image file on disk
        }
        catch (IOException exception){
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to save uploaded image"
            );
        }

        //Save an initial upload record
        PlantUpload upload = new PlantUpload();
        upload.setUser(user);
        upload.setImageName(fileName);
        upload.setImageUrl("/uploads/plants/" + fileName);
        upload.setConfidenceScore(null);
        upload.setUploadStatus(UploadStatus.FAILED);

        upload = plantUploadRepositary.save(upload);

        //Ask Pl@ntNet to identify the image
        try{
            //Sends the image to the external AI service
            PlantNetResponse result = plantNetClient.identify(
                    imageBytes,
                    fileName,
                    contentType
            );
            if (result == null ||
                    result.results() == null ||
                    result.results().isEmpty())
            {
                upload.setUploadStatus(UploadStatus.NOT_IDENTIFIED);

                plantUploadRepositary.save(upload);

                return toResponse(
                        upload, null, null, false
                );
            }

            //The first candidate has the highest score
            PlantNetResponse.Candidate best = result.results().get(0);

            PlantNetResponse.Species species = best.species();

            if (species == null){
                upload.setUploadStatus(UploadStatus.NOT_IDENTIFIED);
                plantUploadRepositary.save(upload);
                return toResponse(upload, null,  null, false);
            }

            String scientificName =
                    species.scientificNameWithoutAuthor();
            String plantName =
                    species.commonNames() != null
                    && !species.commonNames().isEmpty()
                    ? species.commonNames().get(0)
                            : species.scientificName();

            //Find the plant in your own catalogue or databse
            PlantInfo matchedPlant = null;

            //First, search by scientific name
            if(scientificName != null && !scientificName.isBlank()){
                matchedPlant = plantInfoRepositary
                        .findFirstByScientificNameIgnoreCase(scientificName) //Search ur plant catalogue
                        .orElse(null);
            }

            //If no scientific name match, search by common plant name
            if(matchedPlant == null && plantName != null && !plantName.isBlank()){
                matchedPlant = plantInfoRepositary
                        .findFirstByPlantNameIgnoreCase(plantName)
                        .orElse(null);
            }

            //Update PlantUpload with the prediction
            upload.setPlantInfo(matchedPlant); //connects the upload to the matched catalogue plant.

            upload.setConfidenceScore( //Stroes the prediction score
                    best.score() == null
                    ? null
                    : BigDecimal.valueOf(best.score())
            );

            upload.setUploadStatus(UploadStatus.SUCCESS);
            upload = plantUploadRepositary.save(upload); //Updates the record with the identificatio result

            //Return the prediction, even if catalogue match is absent
            return toResponse(
                    upload, plantName, species.scientificName(),
                    matchedPlant != null);
        }
//        catch (RestClientException exception){
//            //the initial record remains FAILED if the provider call fails.
//            upload.setUploadStatus(UploadStatus.FAILED);
//            plantUploadRepositary.save(upload);
//
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_GATEWAY,
//                    "Pl@ntNet could not process the image. Please try again"
//            );
//        }
        catch (HttpStatusCodeException exception){
            String responseBody = exception.getResponseBodyAsString();

            System.err.println("Pl@ntNet status: " + exception.getStatusCode());

            System.err.println("Pl@ntNet response: " + responseBody);

            if(exception.getStatusCode().value() == 404 && responseBody != null
                    && responseBody.contains("Species not found"))
            {
                upload.setUploadStatus(UploadStatus.NOT_IDENTIFIED);
                upload.setConfidenceScore(null);
                upload.setPlantInfo(null);

                upload = plantUploadRepositary.save(upload);
                return toResponse(upload, null, null, false);
            }

            upload.setUploadStatus(UploadStatus.FAILED);
            plantUploadRepositary.save(upload);

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "The plant identification provider returned an error"
            );
        }
        catch (ResourceAccessException exception){
            exception.printStackTrace();
            upload.setUploadStatus(UploadStatus.FAILED);
            plantUploadRepositary.save(upload);

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to connect to the plant identification provider"
            );
        }
    }
    //Helper class to support PlantIdentification in identifyPlant()
    private ResponseEntity<PlantIdentificationResponse> toResponse(
            PlantUpload upload, String plantName,
            String  scientificName, boolean catalogueMatch)
    {
        //Decide the response message based on the upload status
        String message;
        if(upload.getUploadStatus() !=  UploadStatus.SUCCESS){
            message = "Plant identification failed.";
        }
        else if(catalogueMatch){
            message = "Plant identified successfully";
        }
        else{
            message = "Plant identified successfully! "
                    + "Detailed care information is not yet available "
                    + "in our catalogoue";
        }
        PlantIdentificationResponse response = new PlantIdentificationResponse(
                upload.getUploadId(),
                upload.getImageUrl(),
                upload.getUploadStatus(),
                plantName,
                scientificName,
                upload.getConfidenceScore(),
                upload.getUploadedAt(),
                catalogueMatch,
                message
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }


    @Transactional(readOnly = true)
    public PlantDetailsResponse getPlantDetails(
            Authentication authentication,
            Integer uploadId)
    {
        //Validate Authenticated user
        if(authentication == null
            || !authentication.isAuthenticated()
            || "anonymousUser".equalsIgnoreCase(authentication.getName())
        ){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );
        }

        //Validate upload Id
        if(uploadId == null || uploadId < 1){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid upload iD"
            );
        }

        //Find the logged-in user
        String userName = authentication.getName();

        User user = userRepositary.findByUsername(userName)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found")
                );

        //Find only this user's upload
        PlantUpload upload =
                plantUploadRepositary
                        .findByUploadIdAndUserUserId(
                                uploadId,
                                user.getUserId()
                        )
                        .orElseThrow(()->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Identification record not found"
                                )
                        );

        //Get the matched catalogue plant
        PlantInfo plant = upload.getPlantInfo();

        //if the plant is not in the catalogue
        if (plant == null){
            return new PlantDetailsResponse(
                    upload.getUploadId(),
                    upload.getImageUrl(),
                    upload.getUploadStatus(),
                    upload.getConfidenceScore(),
                    upload.getUploadedAt(),

                    null,
                    "Unknown Plant",
                    null,
                    null,
                    null,
                    null,

                    null,
                    null,
                    null,
                    null,

                    null,
                    null,
                    null,
                    null,
                    null,

                    null,
                    null,
                    null,
                    null,

                    false,
                    "Plant details are not available in the FloraSense Catalogue"
            );
        }

        //If the plant exists, return it details
        return new PlantDetailsResponse(
                upload.getUploadId(),
                upload.getImageUrl(),
                upload.getUploadStatus(),
                upload.getConfidenceScore(),
                upload.getUploadedAt(),

                plant.getPlantId(),
                plant.getPlantName(),
                plant.getScientificName(),
                plant.getPlantCategory() != null
                            ? plant.getPlantCategory()
                            : null,
                plant.getImageUrl(),
                plant.getDescription(),

                plant.getCareLevel() != null
                            ? plant.getCareLevel()
                            : null,
                plant.getSunlightRequirement(),
                plant.getWateringFrequency(),
                plant.getTemperatureRequirement(),

                plant.getMedicinalUses(),
                plant.getOxygenInformation(),
                plant.getBenefits(),
                plant.getDisadvantages(),
                plant.getToxicityInformation(),

                plant.getIsIndoor(),
                plant.getIsOutdoor(),
                plant.getIsPetSafe(),
                plant.getSourceReference(),

                true,
                "Plant details retrieved successfully"
        );
    }
}











