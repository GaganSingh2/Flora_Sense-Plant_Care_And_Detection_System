package org.gagan.florasense.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {

    @NotNull(message = "Account status is required")
    private Boolean isActive;

}
