package com.lilo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lilo.model.Video;

public interface VideoRepository extends JpaRepository<Video, String> {
	List<Video> findByUserId(long userId);

}
