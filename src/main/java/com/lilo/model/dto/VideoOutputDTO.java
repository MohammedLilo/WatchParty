package com.lilo.model.dto;

import com.lilo.model.Video;
import com.lilo.shared.WebConstants;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
public class VideoOutputDTO {
    private String videoUrl;
    private String videoName;
    private long userId;
    private LocalDateTime timestamp;
	private String ownerFullName;

    private VideoOutputDTO(Video video) {
        videoUrl = video.getVideoFileName();
        videoName = video.getVideoName();
        userId = video.getUser().getId();
        timestamp = video.getTimestamp();
        ownerFullName = video.getUser().getName();
    }
    public static  VideoOutputDTO fromVideo(Video video, String baseUrl) {
        String path = WebConstants.videosUrlPattern.replace("**", "");
        String thumbnailUrl = String.format("%s%s%s",
                baseUrl,
                path,
                video.getVideoFileName()
        );

        return new VideoOutputDTO(video);
    }
}
