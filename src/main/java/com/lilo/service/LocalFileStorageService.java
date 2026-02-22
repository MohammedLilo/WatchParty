package com.lilo.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.lilo.shared.WebConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Profile("dev")
@Service
public class LocalFileStorageService implements FileStorageService {
	public static final Path ROOT = Paths.get("./uploads");

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
//		FileUtils.forceDelete(this.ROOT.resolve(name).toFile());
		Files.deleteIfExists(ROOT.resolve(fileName));
	}

    @Override
    public String getDownloadUrl(String fileName) {


        String path = WebConstants.staticResourcesUrlPattern.replace("**", "");
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                    .path(path)
                                    .path(fileName)
                                    .toUriString();
    }
}
