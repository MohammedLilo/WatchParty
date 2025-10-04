package com.lilo.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data

public class ApiResponse<T> {
    @JsonProperty("isSuccess")
    private boolean isSuccess;
    private T data;
    private List<ApiError> errors;

    private ApiResponse(boolean isSuccess, T data, ApiError... apiErrors) {
        this.isSuccess = isSuccess;
        this.data = data;
        this.errors = (apiErrors == null) ? null : Arrays.asList(apiErrors);
    }

    private ApiResponse(boolean isSuccess, T data, List<ApiError> apiErrors) {
        this.isSuccess = isSuccess;
        this.data = data;
        this.errors = apiErrors;
    }

    public static <T> ApiResponse<T> withSuccess(T data) {
        return new ApiResponse<>(true, data, (ApiError) null);
    }

//    public static <T> ApiResponse<T> withErrors(ApiError... errors) {
//        return new ApiResponse<>(false, null, errors);
//    }

    public static <T> ApiResponse<T> withErrors(List<ApiError> errors) {
        return new ApiResponse<>(false, null, errors);
    }
    public static <T> ApiResponse<T> withError(ApiError error) {
        return new ApiResponse<>(false, null, error);
    }
    public static <T> ApiResponse<T> withError(int code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message));
    }
}


