package org.gagan.florasense.Service;

import org.gagan.florasense.Dto.PlantDetailsResponse;
import org.gagan.florasense.Dto.PlantHistoryResponse;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Entity.PlantInfo;
import org.gagan.florasense.Entity.PlantUpload;
import org.gagan.florasense.Entity.User;
import org.gagan.florasense.Exception.InvalidFileException;
import org.gagan.florasense.Exception.ResourceNotFoundException;
import org.gagan.florasense.Repositary.PlantUploadRepositary;
import org.gagan.florasense.Repositary.UserRepositary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class PlantHistoryService {

    @Autowired
    private PlantUploadRepositary plantUploadRepositary;

    @Autowired
    private UserRepositary userRepositary;

    //Get identification history with search, sorting and pagination
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseStructure<?>> getAllPlantCardsHistory(
            Authentication authentication,
            int page, int size,
            String keyword, String sortBy)
    {
        //Validate Authentication
        if(authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null)
        {
            throw new org.gagan.florasense.Exception.IllegalArgumentException(
                    "User must be authenticated or logged in"
            );
        }

        //Validate Pagination
        if (page < 0){
            throw new org.gagan.florasense.Exception.IllegalArgumentException(
                    "Page number must be greater than or equal to zero"
            );
        }
        if (size < 1 || size > 100){
            throw new org.gagan.florasense.Exception.IllegalArgumentException(
                    "Size must be between 1 and 100"
            );
        }

        //Find the logged-in user
        String username = authentication.getName();
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with username: " + username)
                );

        //Determine sorting
        Sort sort;
        if ("oldest".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "uploadedAt");
        }
        else if("newest".equalsIgnoreCase(sortBy)
            || sortBy == null
            || sortBy.isBlank()
        ) {
            sort = Sort.by(Sort.Direction.DESC, "uploadedAt");
        }
        else{
            throw new org.gagan.florasense.Exception.IllegalArgumentException(
                    "Invalid sortBy value. Use newest or oldest"
            );
        }

        //Create Pagination
        Pageable pageable = PageRequest.of(page, size, sort);

        //Normalize search keyword
        String searchKeyword =
                keyword == null ? "": keyword.trim();

        //Fetch only this user's upload history
        Page<PlantUpload> historyPlants =
                    plantUploadRepositary.searchMyAllHistory(
                            user,
                            searchKeyword,
                            pageable
                    );

        if(historyPlants.getTotalElements() == 0){
            throw new ResourceNotFoundException("No plants history found");
        }
        Page<PlantHistoryResponse> uploadPages = historyPlants.map(this::convertToResponse);

        ResponseStructure<Page<PlantHistoryResponse>> respone = new ResponseStructure<>();
        respone.setMessage("All plants history fetched successfully");
        respone.setStatusCode(HttpStatus.OK.value());
        respone.setData(uploadPages);

        return ResponseEntity.status(HttpStatus.OK).body(respone);
    }
    //Helper method to generate the response
    private PlantHistoryResponse convertToResponse(PlantUpload plantUpload) {
        PlantInfo plant = plantUpload.getPlantInfo();

        return new PlantHistoryResponse(
                plantUpload.getUploadId(),
                plantUpload.getImageUrl(),
                plantUpload.getImageName(),
                plantUpload.getConfidenceScore(),
                plantUpload.getUploadStatus() == null
                            ? null
                            : plantUpload.getUploadStatus(),
                plantUpload.getUploadedAt(),
                plant == null ? null : plant.getPlantId(),
                plant == null ? null : plant.getPlantName(),
                plant == null ? null : plant.getScientificName()
        );
    }

    //Get total identification count
    public ResponseEntity<ResponseStructure<Long>> getAllPlantIdentificationCount(
            Authentication authentication)
    {
        //Find the logged-in user
        String username =  authentication.getName();
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with username: " + username)
                );

        Long totalUploads = plantUploadRepositary.countByUser(user);
        if(totalUploads == 0){
            throw new ResourceNotFoundException("No plants identification found");
        }

        ResponseStructure<Long> response = new ResponseStructure<>();
        response.setMessage("All plants identification count fetched successfully");
        response.setStatusCode(HttpStatus.OK.value());
        response.setData(totalUploads);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Get Plant Details when user click on View Details button on Plant Card
    @Transactional(readOnly = true)
    public PlantDetailsResponse getPlantDetailsByUploadId(
            Authentication authentication, Integer uploadId) {

        //Validate Authentication
        if(authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null)
        {
            throw new org.gagan.florasense.Exception.IllegalArgumentException(
                    "User must be authenticated or logged in"
            );
        }
        //Validate UploadId
        if (uploadId == null || uploadId <= 0){
            throw new org.gagan.florasense.Exception.IllegalArgumentException("Upload Id must be greater than 0");
        }

        //Find the logged-in user
        String username = authentication.getName();
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with username: " + username)
                );

        PlantUpload upload = plantUploadRepositary
                .findByUploadIdAndUserUserId(uploadId, user.getUserId())
                .orElseThrow(()->
                        new ResourceNotFoundException(
                                "Plant Upload History not found with Upload ID: "+uploadId
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


    //Delete one plant history record and image file from local disk if it does not belong to others upload references.
    public void deletePlantHistoryByUploadId(
            Authentication authentication, Integer uploadId)
    {
        //Validate Upload Id
        if(uploadId == null || uploadId <= 0){
            throw new org.gagan.florasense.Exception.IllegalArgumentException(
                    "Upload Id must be greater than 0"
            );
        }

        //Find the logged-in user
        String username = authentication.getName();
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with username: " + username)
                );

        //FInd the upload history belonging to this user
        PlantUpload upload = plantUploadRepositary
                .findByUploadIdAndUserUserId(uploadId, user.getUserId())
                .orElseThrow(()->
                        new ResourceNotFoundException("Plant Upload History not found with Upload ID: "+uploadId)
                );

        //Save the image path before deleting the database record
        String imageUrl = upload.getImageUrl();

        //Delete the upload record
        plantUploadRepositary.delete(upload);

        //Check whether another upload reference the same image
        boolean imageStillReferenced =
                plantUploadRepositary.existsByImageUrlAndUploadIdNot(
                        imageUrl,
                        uploadId
                );

        //Delete the physical image if it is no longer referenced i.e not refer to other uploads
        if(!imageStillReferenced){
            deleteImageFile(imageUrl);
        }
    }
    //Helper method to delete a ImageUrl from local disk
    @Value("${app.upload.plants-dir}")
    private String plantsUploadDirectory;
    private void deleteImageFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()){
            return;
        }

        try{
            //Convert the configured directory to an absolute normalized path
            Path uploadDirectory = Paths.get(plantsUploadDirectory)
                    .toAbsolutePath()
                    .normalize();

            //Our database stores paths such as:
            // /uploads/plants/tulsi.jpg

            //Extract only the filename, not an arbitrary user-supplied path
            String fileName = Paths.get(imageUrl)
                    .getFileName()
                    .toString();

            //Resolve the filename inside the configured upload directory
            Path imagePath = uploadDirectory.resolve(fileName).normalize();

            //Ensure the resolved path remains inside the upload directory
            if(!imagePath.startsWith(uploadDirectory)){
                throw new org.gagan.florasense.Exception.IllegalArgumentException(
                        "Invalid image path"
                );
            }

            //Delete the image if it exists
            Files.delete(imagePath);
        }
        catch (IOException e){
            throw new InvalidFileException(
                    "Failed to delete the uploaded image file");
        }
    }


    //Export all Plants History in the form of CSV file i.e Excel or Google Sheet(CSV stands for Comma-Separated Values)
    @Transactional(readOnly = true)
    public byte[] exportAllPlantHistory(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()){
            throw new org.gagan.florasense.Exception.IllegalArgumentException(
                    "User is not authenticated"
            );
        }

        //Find the logged-in user
        String username = authentication.getName();
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found with username: " + username)
                );

        //Fetch all uploads belonging to this user
        List<PlantUpload> uploads =
                plantUploadRepositary.findByUserUserIdOrderByUploadedAtDesc(user.getUserId());

        if (uploads.isEmpty()){
            throw new ResourceNotFoundException(
                    "Plant History Not Found"
            );
        }

        //Create CSV content
        StringBuilder csv = new StringBuilder();

        //CSV header
        csv.append("Upload ID,Plant Name,Scientific Name, Image Name,")
                .append("Confidence Score,Upload Status, Image URL, Uploaded At\n");

        //Add each upload as CSV row
        for(PlantUpload upload : uploads){
            PlantInfo info = upload.getPlantInfo();

            csv.append(upload.getUploadId()).append(",")
                    .append(csvValue(info == null ? "Unknown" : info.getPlantName())).append(",")
                    .append(csvValue(info == null ? "Unknown" : info.getScientificName())).append(",")
                    .append(csvValue(upload.getImageName())).append(",")
                    .append(upload.getConfidenceScore() == null
                                ? "0.0"
                                : upload.getConfidenceScore().toString()).append(",")
                    .append(csvValue(upload.getUploadStatus().name())).append(",")
                    .append(csvValue(upload.getImageUrl())).append(",")
                    .append(upload.getUploadedAt()).append("\n");
        }

        //Convert CSV content into bytes
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    //Helper method for Export the Plants History, Escape a value according to CSV rules
    private String csvValue(String value) {
        if (value == null || value.isBlank()){
            return "";
        }

        //Prevent spreadsheet formula injection in text fields
        String safeValue = value;
        if (!safeValue.isEmpty()
                && "=+-@".indexOf(safeValue.charAt(0)) >= 0){
            safeValue = "'" + safeValue;
        }

        //Escape double quotes and wrap fields containingg specical characters
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
}



















