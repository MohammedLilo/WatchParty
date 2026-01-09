package com.lilo.service;

import com.lilo.model.DefaultUserProfilePicture;
import com.lilo.repository.DefaultUserProfilePictureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultUserProfilePictureService {
    private final DefaultUserProfilePictureRepository defaultUserProfilePictureRepository;

    public Optional<DefaultUserProfilePicture> findRandomly() {
        return defaultUserProfilePictureRepository.findRandomly();
    }

    public boolean existsByFileName(String pictureFileName) {
        return defaultUserProfilePictureRepository.existsByPictureFileName(pictureFileName);
    }
}
