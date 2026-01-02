package com.lilo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
public class FileStorageService {
	public static final Path ROOT = Paths.get("./uploads");

	@PostConstruct
	void init() throws IOException {
		Files.createDirectories(ROOT);
	}

	public void save(String fileName, MultipartFile multipartFile) throws IOException {
		Files.copy(multipartFile.getInputStream(), ROOT.resolve(fileName));
	}

	public Resource load(String fileName) {
		return new FileSystemResource(ROOT.resolve(fileName));
	}

	public void delete(String videoFileName) throws IOException {
//		FileUtils.forceDelete(this.ROOT.resolve(name).toFile());
		Files.deleteIfExists(ROOT.resolve(videoFileName));
	}
}
