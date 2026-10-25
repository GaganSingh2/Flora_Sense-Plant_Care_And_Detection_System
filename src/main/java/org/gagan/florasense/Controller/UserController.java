package org.gagan.florasense.Controller;

import lombok.RequiredArgsConstructor;
import org.gagan.florasense.Dto.NotificationPreferenceRequest;
import org.gagan.florasense.Dto.ResponseStructure;
import org.gagan.florasense.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    //1)Get my Profiles data
    @GetMapping("/me")
    public ResponseEntity<ResponseStructure<?>> getMyProfile(Authentication authentication){
        return userService.getMyProfile(authentication);
    }

    //2)Get Notifications preferences
    @GetMapping("/me/preferences")
    public ResponseEntity<ResponseStructure<?>> getMyPreferences(Authentication authentication){
        return  userService.getMyPreferences(authentication);
    }

    //3)Update Notification Preferences
    @PutMapping("/me/update/preferences")
    public ResponseEntity<ResponseStructure<?>> updateMyPreferences(Authentication authentication, @RequestBody NotificationPreferenceRequest request){
        return userService.updateMyPreferences(authentication, request);
    }

    //4)Deactivate my account
    @DeleteMapping("/me/delete")
    public ResponseEntity<ResponseStructure<?>> deleteMyProfile(Authentication authentication){
        return userService.deleteMyProfile(authentication);
    }

    //5)Upload the Profile Picture
    @PostMapping(value = "/me/upload/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseStructure<?>> uploadProfileImage(Authentication authentication, @RequestParam("file") MultipartFile file){
        return userService.uploadProfileImage(authentication, file);
    }

    //6)Delete the profile picture
    @DeleteMapping("/me/delete/profile-image")
    public ResponseEntity<ResponseStructure<?>> deleteProfileImage(Authentication authentication){
        return userService.deleteProfileImage(authentication);
    }
}

