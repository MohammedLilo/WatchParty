package com.lilo.service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class TemporaryStorageService {
    public static final Path ROOT = Paths.get("./temp-uploads");

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(ROOT);
    }

    public void save(String fileName, InputStream inputStream) throws IOException {
        Files.copy(inputStream, ROOT.resolve(fileName));
    }

    public Resource load(String fileName) {
        return new FileSystemResource(ROOT.resolve(fileName));
    }

    public void delete(String fileName) throws IOException {
        Files.deleteIfExists(ROOT.resolve(fileName));
    }

}
