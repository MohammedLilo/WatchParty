package com.lilo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "video")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {
	@Id
	@Column(name = "video_file_name")
	private String videoFileName;

	@Column(name="video_name")
	private String videoName;

	@Column(name = "user_id")
	private long userId;

	private LocalDateTime timestamp;

	@Transient
	private String userName;

	public Video(String videoFileName, String videoName, long userId, LocalDateTime timestamp) {
		this.videoFileName = videoFileName;
		this.videoName = videoName;
		this.userId = userId;
		this.timestamp = timestamp;
	}
}
