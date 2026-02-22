package com.lilo.workers;

import com.lilo.enums.VideoStatus;
import com.lilo.model.dto.FileUploadResult;
import com.lilo.model.dto.PendingFileUploadPayload;
import com.lilo.service.FileStorageService;
import com.lilo.service.TemporaryStorageService;
import com.lilo.service.UserService;
import com.lilo.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.lilo.shared.WebSocketConstants.NOTIFICATIONS_QUEUE;
import static com.lilo.shared.WebSocketConstants.QUEUE_ERRORS;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoUploadWorker {
    private final FileStorageService fileStorageService;
    private final TemporaryStorageService temporaryStorageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService  userService;
    private final VideoService videoService;
    @RabbitListener(queues = "uploadQueue")
    @SendToUser(NOTIFICATIONS_QUEUE)
    public void handleUpload(PendingFileUploadPayload fileUploadPayload) {
        try {
            fileStorageService.save(fileUploadPayload.fileName(), temporaryStorageService.load(fileUploadPayload.fileName()).getInputStream());
            videoService.updateStatus(fileUploadPayload.fileName(), VideoStatus.READY);
            log.info("File {} uploaded to permanent storage successfully.", fileUploadPayload.fileName());
            temporaryStorageService.delete(fileUploadPayload.fileName());
            log.info("File {} deleted from temp storage successfully.", fileUploadPayload.fileName());

            var fileUploadResult = new FileUploadResult(fileUploadPayload.fileName(), fileStorageService.getDownloadUrl(fileUploadPayload.fileName()), true, "File Uploaded Successfully");
            messagingTemplate.convertAndSendToUser(userService.findById(fileUploadPayload.userId()).orElseThrow().getEmail() , NOTIFICATIONS_QUEUE, fileUploadResult);
        } catch (IOException e) {
            var fileUploadResult = new FileUploadResult(fileUploadPayload.fileName(), null, false, "Error while handling file upload");
            messagingTemplate.convertAndSendToUser(userService.findById(fileUploadPayload.userId()).orElseThrow().getEmail() , QUEUE_ERRORS, fileUploadResult);
        }
    }
}
