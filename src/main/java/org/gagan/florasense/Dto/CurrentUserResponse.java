package org.gagan.florasense.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CurrentUserResponse {
    private Integer userId;
    private String username;
    private String email;
    private String profileImageUrl;
    private String role;
}
