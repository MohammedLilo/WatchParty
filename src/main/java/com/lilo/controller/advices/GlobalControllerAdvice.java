package com.lilo.controller.advices;

import com.lilo.exception.UnauthorizedAccessException;
import com.lilo.model.dto.ApiError;
import com.lilo.model.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.FileNotFoundException;
import java.util.List;


@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<?> handleFileNotFoundException(FileNotFoundException ex) {

        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String errorMessage = "An unexpected error occurred: Unable to access file" + ex.getMessage();
        ApiResponse<?> apiResponse = ApiResponse.withError(statusCode, errorMessage);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(apiResponse);
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsableException(AsyncRequestNotUsableException ex) {
        log.info("An error occurred: {}", ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        List<ApiError> errors = ex.getConstraintViolations().stream()
                .map(violation -> new ApiError(HttpStatus.BAD_REQUEST.value(), violation.getMessage()))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.withErrors(errors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

        log.error("Data Integrity Violation: {}", ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Data conflict or constraint violation occurred"
        );
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.withError(error));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.withError(HttpStatus.NOT_FOUND.value(), "Resource not found"));
    }


    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<?> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.withError(HttpStatus.UNAUTHORIZED.value(), ex.getApiMessage()));
    }

//    @ExceptionHandler(JwtAuthenticationException.class)
//    public ResponseEntity<?> handleJwtAuthenticationException(JwtAuthenticationException ex) {
//        return ResponseEntity
//                .status(HttpStatus.UNAUTHORIZED)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(ApiResponse.withError(HttpStatus.UNAUTHORIZED.value(), ex.getApiMessage()));
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.withError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred"));
    }

}
