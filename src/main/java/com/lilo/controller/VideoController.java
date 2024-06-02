package com.lilo.controller;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lilo.domain.User;
import com.lilo.domain.Video;
import com.lilo.service.UserService;
import com.lilo.service.VideoService;
import com.lilo.service.VideoStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class VideoController {
	private final VideoService videoService;
	private final VideoStorageService videoStorageService;
	private final UserService userService;

	@GetMapping(value = "/videos/{file-name}", produces = "video/mp4")
	public Resource loadVideo(@PathVariable("file-name") String fileName) {
		return videoStorageService.load(fileName);
	}

	@GetMapping(value = "/videos")
	public ResponseEntity<Page<Video>> listVideos(@RequestParam(name = "page", defaultValue = "0") int pageNumber,
			@RequestParam(name = "size", defaultValue = "10") int size) {

		return ResponseEntity.ok(videoService.findAll(pageNumber, size, Sort.by(Order.desc("timestamp"))));
	}

	@PostMapping("/videos")
	public ResponseEntity<String> uploadVideo(@RequestPart MultipartFile multipartFile,String videoName ,Authentication authentication)
			throws IOException {
		User user = userService.findByEmail(authentication.getName());
		videoService.save(multipartFile, user.getId(),videoName);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body("video uploaded successfully.");
	}

	@DeleteMapping("/videos/{file-name}")
	public ResponseEntity<String> deleteVideo(@PathVariable("file-name") String videoFileName, Authentication authentication) {
		User user = userService.findByEmail(authentication.getName());
		Video video = videoService.findByVideoFileName(videoFileName);
		if (video == null || user.getId() != video.getUserId()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("You do not have the authority to delete a video that is not yours.");
		}
		videoService.deleteByVideoFileName(videoFileName);
		return ResponseEntity.status(HttpStatus.OK).body("video deleted successfully.");
	}

}
