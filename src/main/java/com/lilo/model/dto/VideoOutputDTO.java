package com.lilo.model.dto;

import com.lilo.model.Video;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
public class VideoOutputDTO {
    private String videoFileName;
    private String videoUrl;
    private String videoName;
    private long userId;
    private LocalDateTime timestamp;
	private String ownerFullName;

    private VideoOutputDTO(Video video,  String videoUrl) {
        this.videoFileName = video.getVideoFileName();
        this.videoUrl = videoUrl;
        this.videoName = video.getVideoName();
        this.userId = (video.getUser() != null)? video.getUser().getId() : -1;
        this.timestamp = video.getTimestamp();
        this.ownerFullName = (video.getUser() != null)? video.getUser().getName() : null;
    }
    public static  VideoOutputDTO fromVideo(Video video, String videoUrl) {
//        String path = WebConstants.videosUrlPattern.replace("**", "");
//        String videoUrl = String.format("%s%s%s",
//                baseUrl,
//                path,
//                video.getVideoFileName()
//        );

        return new VideoOutputDTO(video, videoUrl);
    }
}
