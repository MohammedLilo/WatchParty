package com.lilo.model.dto;

import java.io.Serializable;

public record FileUploadResult(String fileName, String fileUrl, boolean isSuccess, String message) implements Serializable {
}
