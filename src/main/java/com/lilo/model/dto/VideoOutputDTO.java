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

    private VideoOutputDTO(Video video,  String videoUrl) {
        this.videoUrl = videoUrl;
        this.videoName = video.getVideoName();
        this.userId = (video.getUser() != null)? video.getUser().getId() : -1;
        this.timestamp = video.getTimestamp();
        this.ownerFullName = (video.getUser() != null)? video.getUser().getName() : null;
    }
    public static  VideoOutputDTO fromVideo(Video video, String baseUrl) {
        String path = WebConstants.videosUrlPattern.replace("**", "");
        String videoUrl = String.format("%s%s%s",
                baseUrl,
                path,
                video.getVideoFileName()
        );

        return new VideoOutputDTO(video, videoUrl);
    }
}
