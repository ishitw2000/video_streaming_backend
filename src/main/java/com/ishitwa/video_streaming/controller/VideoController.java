package com.ishitwa.video_streaming.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ishitwa.video_streaming.model.User;
import com.ishitwa.video_streaming.model.Video;
import com.ishitwa.video_streaming.service.StorageService;
import com.ishitwa.video_streaming.service.UserServiceImpl;
import com.ishitwa.video_streaming.service.VideoServiceImpl;
import com.ishitwa.video_streaming.utils.VideoStatus;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.core.io.Resource;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

	@Autowired
	private VideoServiceImpl videoService;

	@Autowired
	private UserServiceImpl userService;

	@Autowired
	private StorageService storageService;

	@GetMapping("/{id}/stream")
	public ResponseEntity<Resource> streamVideo(
			@PathVariable Long id,
			@RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {

		Video video = videoService.getVideoById(id);
		Path videoPath = storageService.retrieveFile(video.getFilePath());
		UrlResource videoResource = new UrlResource(videoPath.toUri());

		long fileLength = videoResource.contentLength();
		long start = 0, end = fileLength - 1;

		if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
			String[] ranges = rangeHeader.substring(6).split("-");
			try {
				start = Long.parseLong(ranges[0]);
				if (ranges.length > 1 && !ranges[1].isEmpty()) {
					end = Long.parseLong(ranges[1]);
				}
			} catch (NumberFormatException ignored) {
			}
		}

		long contentLength = end - start + 1;
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", video.getFileType());
		headers.add("Accept-Ranges", "bytes");
		headers.add("Content-Length", String.valueOf(contentLength));
		headers.add("Content-Range", "bytes" + start + "-" + end + "/" + fileLength);
		InputStream inputStream = videoResource.getInputStream();
		inputStream.skip(start);
		return new ResponseEntity<>(
				new InputStreamResource(inputStream),
				headers,
				(rangeHeader != null) ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Video> uploadVideo(
			@RequestParam("file") MultipartFile file,
			@RequestParam("title") String title,
			@RequestParam(value = "description", required = false) String description) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String username = authentication.getName();

			User user = userService.getUserByUsername(username);

			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			Video uploadedVideo = videoService.uploadVideo(title, description, file, user.getId());

			return ResponseEntity.status(HttpStatus.CREATED).body(uploadedVideo);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping
	public ResponseEntity<List<Video>> getAllVideos() {
		List<Video> videos = videoService.getAllVideos();
		return ResponseEntity.ok(videos);
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Video>> getVideosByUser(@PathVariable Long userId) {
		List<Video> videos = videoService.getAllVideosByUserId(userId);
		for (Video v : videos) {
			System.out.println(v.toString());
		}
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.getUserByUsername(authentication.getName());
		System.out.println(user.toString());
		if (user.getId() != userId) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(videos);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Video> getVideoById(@PathVariable Long id) {
		try {
			Video video = videoService.getVideoById(id);
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User user = userService.getUserByUsername(authentication.getName());
			if (user.getId() != video.getUserId()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
			return ResponseEntity.ok(video);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String username = authentication.getName();
			User user = userService.getUserByUsername(username);
			Video video = videoService.getVideoById(id);
			if (!video.getUserId().equals(user.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
			videoService.deleteVideo(id);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<Video> updateVideoStatus(@PathVariable Long id, @RequestParam VideoStatus status) {
		try {
			Video updatedVideo = videoService.updateVideoStatus(id, status);
			return ResponseEntity.ok(updatedVideo);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}
