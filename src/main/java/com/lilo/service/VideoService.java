package com.lilo.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.lilo.operationResult.TableOperationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lilo.model.Video;
import com.lilo.repository.VideoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {
    private final VideoRepository videoRepository;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    public List<Video> findByUserId(long userId) {
        return videoRepository.findByUserId(userId);
    }

    public Page<Video> findAll(int pageNumber, int pageSize, Sort sort) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        return videoRepository.findAll(pageable);
    }

    public Video save(MultipartFile multipartFile, long userId, String videoName) throws IOException {
        String fileName = UUID.randomUUID().toString();// + ".mp4";
        fileStorageService.save(fileName, multipartFile);
        return videoRepository.save(new Video(fileName, videoName, userId, LocalDateTime.now()));
    }

    public TableOperationResult deleteIfUserIsOwner(String videoFileName, long userId) {
        boolean isVideoExists = videoRepository.existsById(videoFileName);
        if (!isVideoExists)
            return TableOperationResult.fromFailure("Video not found!", HttpStatus.NOT_FOUND.value());

        int affectedRows = videoRepository.deleteIfUserIsOwner(videoFileName, userId);
        if (affectedRows == 0)
            return TableOperationResult.fromFailure("Only the video owner can delete this video", HttpStatus.FORBIDDEN.value());

        tryDeleteVideoFromStorage(videoFileName);
        return TableOperationResult.fromSuccess();
    }

    public Optional<Video> findByVideoFileName(String videoFileName) {
        return videoRepository.findById(videoFileName);
    }

    private void tryDeleteVideoFromStorage(String videoFileName) {
        try {
            fileStorageService.delete(videoFileName);
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to delete video file on storage. File remains: " + videoFileName);
        }
    }
}
