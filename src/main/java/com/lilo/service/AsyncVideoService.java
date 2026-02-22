package com.lilo.service;

import com.lilo.enums.VideoStatus;
import com.lilo.model.Video;
import com.lilo.model.dto.PendingFileUploadPayload;
import com.lilo.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Profile("uat")
@Slf4j
public class AsyncVideoService extends VideoService {
    private final RabbitTemplate rabbitTemplate;
    private final TemporaryStorageService tempStorage;
    private final AsyncTaskExecutor taskExecutor;

    public AsyncVideoService(VideoRepository videoRepository, FileStorageService fileStorageService, RabbitTemplate rabbitTemplate, TemporaryStorageService tempStorage, AsyncTaskExecutor taskExecutor) {
        super(videoRepository, fileStorageService);
        this.rabbitTemplate = rabbitTemplate;
        this.tempStorage = tempStorage;
        this.taskExecutor = taskExecutor;
    }


    @Override
    public Video save(MultipartFile multipartFile, long userId, String videoName) throws IOException {
        String fileName = UUID.randomUUID().toString();
        InputStream inputStream = multipartFile.getInputStream();

        Video pendingVideo = new Video(fileName, videoName, userId, LocalDateTime.now(), VideoStatus.PROCESSING);
        videoRepository.save(pendingVideo);

        taskExecutor.execute(() -> {
        try(inputStream){
            log.info("Background task started on virtual thread: {}", Thread.currentThread());
            tempStorage.save(fileName, multipartFile.getInputStream());

            PendingFileUploadPayload payload = new PendingFileUploadPayload(fileName, userId);
            rabbitTemplate.convertAndSend("video.exchange", "video.upload.routing", payload);

            log.info("Video upload queued for file: {}", fileName);
        }catch(Exception e){
            log.error("Background disk write failed for {}", fileName, e);
            videoRepository.updateStatus(fileName, VideoStatus.FAILED);
            }
        });
        return pendingVideo;
    }


}