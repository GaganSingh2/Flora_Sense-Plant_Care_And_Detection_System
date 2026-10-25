package org.gagan.florasense.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gagan.florasense.Enum.Role;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserResponse {
    private Integer userId;
    private String username;
    private String email;
    private String profileImageUrl;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
