package com.lilo.service;

import com.lilo.model.DefaultThumbnail;
import com.lilo.repository.DefaultThumbnailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultThumbnailService {
    private final DefaultThumbnailRepository defaultThumbnailRepository;

    public Optional<DefaultThumbnail> findRandomly(){
        return defaultThumbnailRepository.findRandomThumbnail();
    }
}
