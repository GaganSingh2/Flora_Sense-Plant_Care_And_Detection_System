package org.gagan.florasense.Exception;

import org.gagan.florasense.Dto.ErrorResponse;
import org.gagan.florasense.Dto.ResponseStructure;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
//import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.databind.exc.InvalidFormatException;
import java.time.LocalDateTime;
import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");

        ErrorResponse error = new ErrorResponse(
                400,
                message,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ){
        String message = "Invalid request body";
        Throwable cause = ex.getMostSpecificCause();

        if(cause instanceof InvalidFormatException invalidFormatException) {
            Class<?> targetType = invalidFormatException.getTargetType();

            if(targetType.isEnum()){
                Object invalidValue = invalidFormatException.getValue();
                Object[] validValues = targetType.getEnumConstants();

                message = "Invalid value '" + invalidValue
                        + "' for " + targetType.getSimpleName()
                        + ". Allowed values: "
                        +Arrays.toString(validValues);
            }
        }
        ResponseStructure<?> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage(message);
        response.setData(null);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ){
        String message = "Invalid parameter value";
        if(ex instanceof MethodArgumentTypeMismatchException mismatchException){
            String parameterName = mismatchException.getName();
            Object invalidValue = mismatchException.getValue();
            Class<?> requiredType = mismatchException.getRequiredType();
            message = "Invalid value '" + invalidValue
                    + "' for parameter: '" + parameterName + "'";

            if (requiredType != null && requiredType.isEnum()) {
                Object[] allowedValues = requiredType.getEnumConstants();
                message = message+". Allowed values: "
                        +Arrays.toString(allowedValues);
            }
        }
        ResponseStructure<?> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage(message);
        response.setData(null);
        return  ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(
                409,
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(
                409,
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException ex) {
        ErrorResponse error = new ErrorResponse(
                400,
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidEmailOrPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmailOrPasswordException(InvalidEmailOrPasswordException ex) {
        ErrorResponse error = new ErrorResponse(
                400,
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccountDeactivatedException.class)
    public ResponseEntity<ErrorResponse> handleAccountDeactivatedException(AccountDeactivatedException ex) {
        ErrorResponse error = new ErrorResponse(
                401,
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ResponseStructure<?>> handleInvalidFileException(
            InvalidFileException ex) {

        ResponseStructure<String> structure = new ResponseStructure<>();

        structure.setStatusCode(HttpStatus.BAD_REQUEST.value());
        structure.setMessage(ex.getMessage());
        structure.setData(null);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(structure);
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<ResponseStructure<?>> handleImageNotFoundException(ImageNotFoundException ex) {
        ResponseStructure<String> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.NOT_FOUND.value());
        response.setMessage("Profile image not found");
        response.setData(null);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(FileOperationException.class)
    public ResponseEntity<ResponseStructure<?>> handleFileOperationException(FileOperationException ex) {
        ResponseStructure<String> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage(ex.getMessage());
        response.setData(null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseStructure<?>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ResponseStructure<Object> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.NOT_FOUND.value());
        response.setMessage(ex.getMessage());
        response.setData(null);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseStructure<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ResponseStructure<String> response = new ResponseStructure<>();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage(ex.getMessage());
        response.setData(null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
