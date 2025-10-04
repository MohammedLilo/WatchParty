package com.lilo.controller;

import com.lilo.model.dto.ApiError;
import com.lilo.model.dto.ApiResponse;
import com.lilo.operationResult.TableOperationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.List;

public class BaseController {
    protected <T> ResponseEntity<?> buildSuccessResponse(T data) {
        return ResponseEntity.ok(ApiResponse.withSuccess(data));
    }

    protected <T> ResponseEntity<?> buildSuccessResponse(HttpStatus statusCode, T data) {
        return ResponseEntity.ok(ApiResponse.withSuccess(data));
    }

    protected ResponseEntity<?> buildBindingErrorResponse(BindingResult bindingResult) {
        List<ApiError> errors = bindingResult.getFieldErrors().stream()
                .map(error -> new ApiError(HttpStatus.BAD_REQUEST.value(), error.getField() + ": " + error.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiResponse.withErrors(errors));
    }

    protected ResponseEntity<?> buildErrorResponse(ApiError apiError) {
        return ResponseEntity
                .status(apiError.getCode())
                .body(ApiResponse.withError(apiError));
    }

    protected ResponseEntity<?> buildErrorResponse(HttpStatus status, String message) {
        ApiError error = new ApiError(status.value(), message);
        return buildErrorResponse(error);
    }

    protected ResponseEntity<?> buildErrorResponse(TableOperationResult tableOperationResult) {
        ApiError error = new ApiError(tableOperationResult.getSuggestedStatusCode(), tableOperationResult.getErrorMessage());
        return buildErrorResponse(error);
    }
}
