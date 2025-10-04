package com.lilo.model.dto;

import com.lilo.model.Video;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
public class VideoOutputDTO {
    private String videoFileName;
    private String videoName;
    private long userId;
    private LocalDateTime timestamp;
	private String ownerFullName;

    private VideoOutputDTO(Video video) {
        videoFileName = video.getVideoFileName();
        videoName = video.getVideoName();
        userId = video.getUser().getId();
        timestamp = video.getTimestamp();
        ownerFullName = video.getUser().getName();
    }
    public static  VideoOutputDTO fromVideo(Video video) {
        return new VideoOutputDTO(video);
    }
}
