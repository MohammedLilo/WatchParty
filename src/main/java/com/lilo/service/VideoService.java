package com.lilo.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lilo.domain.Video;
import com.lilo.repository.VideoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoService {
	private final VideoRepository videoRepository;
	private final VideoStorageService videoStorageService;
	private final UserService userService;

	public List<Video> findByUserId(long userId) {
		return videoRepository.findByUserId(userId);
	}

	public Page<Video> findAll(int pageNumber, int pageSize, Sort sort) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		Page<Video> page = videoRepository.findAll(pageable);
		page.forEach(video -> video.setUserName(userService.findById(video.getUserId()).getName()));
		return page;
	}

	public void save(MultipartFile multipartFile, long userId,String videoName) throws IOException {
		String fileName = UUID.randomUUID().toString() + ".mp4";
		videoStorageService.save(fileName, multipartFile);
		videoRepository.save(new Video(fileName,videoName, userId, LocalDateTime.now()));
	}

	public void deleteByVideoFileName(String videoFileName) {
		try {
			videoStorageService.delete(videoFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		videoRepository.deleteById(videoFileName);
	}

	public Video findByVideoFileName(String videoFileName) {
		return videoRepository.findById(videoFileName).orElse(null);
	}
}
