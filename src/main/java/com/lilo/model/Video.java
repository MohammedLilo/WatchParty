package com.lilo.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "video")
@Data
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id",insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_video_user"))
    private User user;

	public Video(String videoFileName, String videoName, Long userId, LocalDateTime timestamp) {
		this.videoFileName = videoFileName;
		this.videoName = videoName;
		this.userId = userId;
		this.timestamp = timestamp;
	}
}
