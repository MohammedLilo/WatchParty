package com.lilo.model;

import java.time.LocalDateTime;

import com.lilo.enums.VideoStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "video",
        indexes = {
                @Index(name = "idx_video_status", columnList = "status"),
                @Index(name = "idx_video_user_id", columnList = "user_id")
        }
)@Data
@NoArgsConstructor
public class Video {
	@Id
	@Column(name = "video_file_name")
	private String videoFileName;

	@Column(name="video_name")
	private String videoName;

	@Column(name = "user_id")
	private Long userId;

	private LocalDateTime timestamp;


    private VideoStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id",insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_video_user"))
    private User user;

	public Video(String videoFileName, String videoName, Long userId, LocalDateTime timestamp, VideoStatus status) {
		this.videoFileName = videoFileName;
		this.videoName = videoName;
		this.userId = userId;
		this.timestamp = timestamp;
        this.status = status;
	}

}
