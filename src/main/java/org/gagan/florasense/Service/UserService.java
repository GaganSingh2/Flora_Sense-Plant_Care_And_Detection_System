package org.gagan.florasense.Service;

import lombok.AllArgsConstructor;
import org.gagan.florasense.Dto.*;
import org.gagan.florasense.Entity.User;
import org.gagan.florasense.Exception.FileOperationException;
import org.gagan.florasense.Exception.ImageNotFoundException;
import org.gagan.florasense.Exception.InvalidFileException;
import org.gagan.florasense.Repositary.UserRepositary;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepositary userRepositary;

    //1)Get my Profile
    public ResponseEntity<ResponseStructure<?>> getMyProfile(Authentication authentication) {
        String username = authentication.getName();

        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new UsernameNotFoundException("User not found with username: "+username));

        CurrentUserProfileResponse response = new CurrentUserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getIsPushNotificationEnabled(),
                user.getIsEmailNotificationEnabled()
        );

        ResponseStructure<CurrentUserProfileResponse> structure = new ResponseStructure<>();
        structure.setStatusCode(HttpStatus.OK.value());
        structure.setMessage("Profile fetched successfully");
        structure.setData(response);

        return ResponseEntity.ok(structure);
    }

    //2) Get my Notification preferences
    public ResponseEntity<ResponseStructure<?>> getMyPreferences(Authentication authentication) {
        String username = authentication.getName();

        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new UsernameNotFoundException("User not found with username: "+username));

        NotificationPreferenceResponse response = new NotificationPreferenceResponse();
        response.setEmailNotificationEnabled(user.getIsEmailNotificationEnabled());
        response.setPushNotificationEnabled(user.getIsPushNotificationEnabled());

        ResponseStructure<NotificationPreferenceResponse> structure = new ResponseStructure<>();
        structure.setStatusCode(HttpStatus.OK.value());
        structure.setMessage("Notification preferences fetched successfully");
        structure.setData(response);

        return ResponseEntity.ok(structure);
    }

    //3) Update my Notification preferences
    public ResponseEntity<ResponseStructure<?>> updateMyPreferences(Authentication authentication, NotificationPreferenceRequest request) {
        String username = authentication.getName();
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new UsernameNotFoundException("User not found with username: "+username));

        user.setIsPushNotificationEnabled(request.getPushNotificationEnabled());
        user.setIsEmailNotificationEnabled(request.getEmailNotificationEnabled());
        userRepositary.save(user);

        NotificationPreferenceResponse response = new NotificationPreferenceResponse();
        response.setEmailNotificationEnabled(user.getIsEmailNotificationEnabled());
        response.setPushNotificationEnabled(user.getIsPushNotificationEnabled());
        ResponseStructure<NotificationPreferenceResponse> structure = new ResponseStructure<>();
        structure.setStatusCode(HttpStatus.OK.value());
        structure.setMessage("Notification preferences update successfully");
        structure.setData(response);
        return ResponseEntity.ok(structure);
    }

    //4)Deactivate my account
    public ResponseEntity<ResponseStructure<?>> deleteMyProfile(Authentication authentication) {
        String username = authentication.getName();

        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new UsernameNotFoundException("User not found with username: "+username));

        user.setIsActive(false);
        userRepositary.save(user);

        ResponseStructure<String> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Account deactivated successfully");
        response.setData("Your account has been deactivated");

        return ResponseEntity.ok(response);
    }

    //5)Upload the Profile Picture
    public ResponseEntity<ResponseStructure<?>> uploadProfileImage(Authentication authentication, MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Profile image is required");
        }
        if (file.getContentType() == null ||
            !file.getContentType().startsWith("image/")){
            throw new InvalidFileException("Only image files are allowed");
        }
        String username = authentication.getName();
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new UsernameNotFoundException("User not found with username: "+username));
        try {
            String uploadDirectory = "uploads/profile/"; //this define where u want to store the uploaded image in ur local storage.
            Path directory = Paths.get(uploadDirectory);
            if (!Files.exists(directory)){ //check whether path already exists
                Files.createDirectories(directory);// if not then create new directory
            }
            String extension = "";
            String originalFilename = file.getOriginalFilename();
            if(originalFilename != null && originalFilename.contains(".")){
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")); //find the exact extension of the image like .jpg, .png, .jpeg etc
            }
            String fileName = UUID.randomUUID() + extension; //generate the random UUID for the image that is going to store in DB
            Path filePath = directory.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );
            String imageUrl = "/uploads/profile/" + fileName;
            user.setProfileImageUrl(imageUrl);
            userRepositary.save(user);

            ResponseStructure<String> response = new ResponseStructure<>();
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Profile image upload successfully");
            response.setData(imageUrl);
            return ResponseEntity.ok(response);
        }
        catch (IOException e){
            throw new FileOperationException("Failed to upload profile image");
        }

    }

    //6)Delete the Profile Picture
    public ResponseEntity<ResponseStructure<?>> deleteProfileImage(Authentication authentication) {
        String username = authentication.getName();

        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new UsernameNotFoundException("User not found with username: "+username));

        String imageUrl = user.getProfileImageUrl();
        if(imageUrl == null || imageUrl.isBlank()){
            throw new ImageNotFoundException("Profile image not found");
        }
        try{
            //Convert URL path to local file path
            String filePath = imageUrl.startsWith("/")
                    ? imageUrl.substring(1)
                    : imageUrl;
            Path path = Paths.get(filePath);
            if (Files.exists(path)){
                Files.delete(path);
            }
            //Remove image reference from databse
            user.setProfileImageUrl(null);
            userRepositary.save(user);

            ResponseStructure<String> response = new ResponseStructure<>();
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Profile image deleted successfully");
            response.setData("Profile image removed");

            return ResponseEntity.ok(response);
        }
        catch (IOException e){
            throw new FileOperationException("Failed to delete profile image");
        }
    }
}
