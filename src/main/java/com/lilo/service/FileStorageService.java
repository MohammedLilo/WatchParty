package com.lilo.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public interface FileStorageService {

    void save(String fileName, InputStream inputStream) throws IOException;


    Resource load(String fileName);

    public void delete(String fileName) throws IOException;
    String getDownloadUrl(String fileName);
}
