package com.lilo.service;

import com.lilo.enums.VideoStatus;
import com.lilo.model.Video;
import com.lilo.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Profile("dev")
@Slf4j
@Service
public class SimpleVideoService extends VideoService {
    private final AsyncTaskExecutor taskExecutor;

    public SimpleVideoService(VideoRepository videoRepository, FileStorageService fileStorageService, AsyncTaskExecutor taskExecutor) {
        super(videoRepository, fileStorageService);
        this.taskExecutor = taskExecutor;
    }

    @Override
    public Video save(MultipartFile multipartFile, long userId, String videoName) throws IOException {
        String fileName = UUID.randomUUID().toString();
        InputStream inputStream = multipartFile.getInputStream();
        taskExecutor.execute(() -> {
            try(inputStream){
                log.info("Background file saving started on thread: {}", Thread.currentThread());
                fileStorageService.save(fileName, inputStream);
                log.info("Background file saving finished on thread: {}", Thread.currentThread());
            }catch(Exception e){
                log.error("Background disk write failed for {}", fileName, e);
                videoRepository.updateStatus(fileName, VideoStatus.FAILED);
            }
        });
        return videoRepository.save(new Video(fileName, videoName, userId, LocalDateTime.now(), VideoStatus.READY));
    }
}
