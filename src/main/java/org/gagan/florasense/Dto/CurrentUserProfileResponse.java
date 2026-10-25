package org.gagan.florasense.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CurrentUserProfileResponse {
    private Integer userId;
    private String username;
    private String email;
    private String profileImageUrl;
    private String role;
    private LocalDateTime createdAt;
    private Boolean isPushNotificationEnabled;
    private Boolean isEmailNotificationEnabled;
}
