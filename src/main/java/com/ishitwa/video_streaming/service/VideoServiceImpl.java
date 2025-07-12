package com.ishitwa.video_streaming.service;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ishitwa.video_streaming.model.Video;
import com.ishitwa.video_streaming.repository.VideoRepository;
import com.ishitwa.video_streaming.utils.VideoStatus;

import jakarta.transaction.Transactional;

@Service
public class VideoServiceImpl implements VideoService {

	@Autowired
	private VideoRepository videoRepository;

	@Autowired
	private StorageService storageService;

	@Override
	public Video uploadVideo(String title, String description, MultipartFile file, Long userId) {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("Video file cannot be empty");
		}

		if (title == null || title.isEmpty()) {
			throw new IllegalArgumentException("Title cannot be empty");
		}

		String filePath = storageService.saveFile(file);

		Video video = new Video();
		video.setTitle(title);
		video.setDescription(description);
		video.setFilePath(filePath);
		video.setFileType(file.getContentType());
		video.setSize(file.getSize());
		video.setUserId(userId);
		video.setUploadTime(Time.valueOf(LocalTime.now()));
		video.setStatus(VideoStatus.UPLOADED);

		return videoRepository.save(video);

	}

	@Override
	public Video getVideoById(Long id) {
		Optional<Video> video = videoRepository.findById(id);
		if (video.isPresent()) {
			return video.get();
		} else {
			throw new IllegalArgumentException("Video not found with id: " + id);
		}
	}

	@Override
	@Transactional
	public void deleteVideo(Long id) {
		Optional<Video> videoOpt = videoRepository.findById(id);
		if (videoOpt.isPresent()) {
			Video video = videoOpt.get();
			storageService.deleteFile(video.getFilePath());
			videoRepository.delete(video);
		} else {
			throw new IllegalArgumentException("Video not found with id: " + id);
		}
	}

	@Override
	@Transactional
	public Video updateVideoStatus(Long id, VideoStatus status) {
		Optional<Video> videoOpt = videoRepository.findById(id);
		if (videoOpt.isPresent()) {
			Video video = videoOpt.get();
			video.setStatus(status);
			return videoRepository.save(video);
		} else {
			throw new IllegalArgumentException("Video not found with id: " + id);
		}
	}

	@Override
	public List<Video> getAllVideosByUserId(Long userId) {
		return videoRepository.findAllByUserId(userId);
	}

	@Override
	public List<Video> getAllVideos() {
		return videoRepository.findAll();
	}

}
