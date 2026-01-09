package com.lilo.service;

import com.lilo.model.DefaultPartyThumbnail;
import com.lilo.repository.DefaultPartyThumbnailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultPartyThumbnailService {
    private final DefaultPartyThumbnailRepository defaultPartyThumbnailRepository;

    public Optional<DefaultPartyThumbnail> findRandomly() {
        return defaultPartyThumbnailRepository.findRandomly();
    }

    public boolean existsByFileName(String thumbnailFileName) {
        return defaultPartyThumbnailRepository.existsByThumbnailFileName(thumbnailFileName);
    }
}
