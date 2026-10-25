package org.gagan.florasense.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Setter
public class ErrorResponse {
    private int statusCode;
    private String message;
    private LocalDateTime timestamp;
}
