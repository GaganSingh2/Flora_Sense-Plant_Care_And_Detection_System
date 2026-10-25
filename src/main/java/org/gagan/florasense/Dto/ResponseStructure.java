package org.gagan.florasense.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseStructure<T>{
    private Integer statusCode;
    private String message;
    private T data;


}
