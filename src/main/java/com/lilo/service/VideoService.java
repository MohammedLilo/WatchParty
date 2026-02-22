package com.lilo.service;

import com.lilo.enums.VideoStatus;
import com.lilo.model.Video;
import com.lilo.operationResult.TableOperationResult;
import com.lilo.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
@RequiredArgsConstructor
public abstract class VideoService {
    protected final VideoRepository videoRepository;
    protected final FileStorageService fileStorageService;

    public abstract Video save(MultipartFile multipartFile, long userId, String videoName) throws IOException;

    public List<Video> findByUserId(long userId) {
        return videoRepository.findByUserId(userId);
    }

    public Page<Video> findAll(int pageNumber, int pageSize, Sort sort) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
//        return videoRepository.findAll(pageable);
        return videoRepository.findByStatus(VideoStatus.READY, pageable);
    }

    public Optional<Video> findByVideoFileName(String videoFileName) {
        return videoRepository.findById(videoFileName);
    }

    public int updateStatus(String videoFileName, VideoStatus videoStatus) {
        return  videoRepository.updateStatus(videoFileName, videoStatus);
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

    private void tryDeleteVideoFromStorage(String videoFileName) {
        try {
            fileStorageService.delete(videoFileName);
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to delete video file on storage. File remains: " + videoFileName);
        }
    }

}