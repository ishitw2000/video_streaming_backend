package com.ishitwa.video_streaming.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ishitwa.video_streaming.model.Video;
import com.ishitwa.video_streaming.utils.VideoStatus;

@Service
public interface VideoService {
	Video uploadVideo(String title, String description, MultipartFile file, Long userId);

	Video getVideoById(Long id);

	void deleteVideo(Long id);

	Video updateVideoStatus(Long id, VideoStatus status);

	List<Video> getAllVideosByUserId(Long userId);

	List<Video> getAllVideos();
}
