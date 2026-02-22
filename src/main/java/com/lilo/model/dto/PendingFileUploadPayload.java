package com.lilo.model.dto;

import java.io.Serializable;

public record PendingFileUploadPayload(String fileName, long userId)implements Serializable {
}
