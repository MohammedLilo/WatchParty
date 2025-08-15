package com.lilo.controller;

import java.io.IOException;
import java.security.Principal;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.lilo.model.User;
import com.lilo.model.Video;
import com.lilo.service.UserService;
import com.lilo.service.VideoService;
import com.lilo.service.VideoStorageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class VideoController {
	private final VideoService videoService;
	private final VideoStorageService videoStorageService;
	private final UserService userService;

	@GetMapping("/videos-library")
	public String getVideosLibraryPage() {
		return "videos-page.html";
	}

	@GetMapping(value = "/videos/{file-name}", produces = "video/mp4")
	@ResponseBody
	public Resource loadVideo(@PathVariable("file-name") String fileName) {
		return videoStorageService.load(fileName);
	}

	@GetMapping(value = "/videos")
	@ResponseBody
	public ResponseEntity<Page<Video>> listVideos(@RequestParam(name = "page", defaultValue = "0") int pageNumber,
			@RequestParam(name = "size", defaultValue = "6") int size,@RequestParam(name="sortBy",defaultValue = "timestamp")String sortBy) {

		return ResponseEntity.ok(videoService.findAll(pageNumber, size, Sort.by(Order.desc(sortBy))));
	}

	@PostMapping("/videos")
	@ResponseBody
	public ResponseEntity<String> uploadVideo(@RequestPart MultipartFile multipartFile,@RequestPart String videoName ,Principal principal)
			throws IOException {
		User user = userService.findByEmail(principal.getName());
		videoService.save(multipartFile, user.getId(),videoName);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body("video uploaded successfully.");
	}

	@DeleteMapping("/videos/{file-name}")
	@ResponseBody
	public ResponseEntity<String> deleteVideo(@PathVariable("file-name") String videoFileName, Principal principal) {
		User user = userService.findByEmail(principal.getName());
		Video video = videoService.findByVideoFileName(videoFileName);
		if (video == null || user.getId() != video.getUserId()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("You do not have the authority to delete a video that is not yours.");
		}
		videoService.deleteByVideoFileName(videoFileName);
		return ResponseEntity.status(HttpStatus.OK).body("video deleted successfully.");
	}

}
