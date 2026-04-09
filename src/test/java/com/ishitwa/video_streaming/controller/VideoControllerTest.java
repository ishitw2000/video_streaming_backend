package com.ishitwa.video_streaming.controller;

import com.ishitwa.video_streaming.model.User;
import com.ishitwa.video_streaming.model.Video;
import com.ishitwa.video_streaming.service.StorageService;
import com.ishitwa.video_streaming.service.UserServiceImpl;
import com.ishitwa.video_streaming.service.VideoServiceImpl;
import com.ishitwa.video_streaming.utils.VideoStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoControllerTest {
    @Mock
    private VideoServiceImpl videoService;
    @Mock
    private UserServiceImpl userService;
    @Mock
    private StorageService storageService;
    @Mock
    private MultipartFile file;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private VideoController videoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // ── uploadVideo ───────────────────────────────────────────────────────────

    @Test
    void testUploadVideo_Success() {
        when(authentication.getName()).thenReturn("user1");
        User user = new User();
        user.setId(1L);
        when(userService.getUserByUsername("user1")).thenReturn(user);
        Video video = new Video();
        when(videoService.uploadVideo(anyString(), anyString(), eq(file), eq(1L))).thenReturn(video);
        ResponseEntity<Video> response = videoController.uploadVideo(file, "title", "desc");
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(video, response.getBody());
    }

    @Test
    void testUploadVideo_UserNotFound_Returns401() {
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(null);
        ResponseEntity<Video> response = videoController.uploadVideo(file, "title", "desc");
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testUploadVideo_ServiceThrows_Returns500() {
        when(authentication.getName()).thenReturn("user1");
        User user = new User();
        user.setId(1L);
        when(userService.getUserByUsername("user1")).thenReturn(user);
        when(videoService.uploadVideo(anyString(), anyString(), eq(file), eq(1L)))
                .thenThrow(new RuntimeException("storage failure"));
        ResponseEntity<Video> response = videoController.uploadVideo(file, "title", "desc");
        assertEquals(500, response.getStatusCodeValue());
    }

    // ── getAllVideos ──────────────────────────────────────────────────────────

    @Test
    void testGetAllVideos() {
        Video v1 = new Video();
        Video v2 = new Video();
        when(videoService.getAllVideos()).thenReturn(Arrays.asList(v1, v2));
        ResponseEntity<List<Video>> response = videoController.getAllVideos();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    // ── getVideosByUser ───────────────────────────────────────────────────────

    @Test
    void testGetVideosByUser_Authorized() {
        User user = new User();
        user.setId(1L);
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(user);
        Video v1 = new Video();
        when(videoService.getAllVideosByUserId(1L)).thenReturn(Arrays.asList(v1));
        ResponseEntity<List<Video>> response = videoController.getVideosByUser(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetVideosByUser_DifferentUser_Returns401() {
        User user = new User();
        user.setId(1L);
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(user);
        ResponseEntity<List<Video>> response = videoController.getVideosByUser(2L);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testGetVideosByUser_UserNotFound_Returns401() {
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(null);
        ResponseEntity<List<Video>> response = videoController.getVideosByUser(1L);
        assertEquals(401, response.getStatusCodeValue());
    }

    // ── getVideoById ──────────────────────────────────────────────────────────

    @Test
    void testGetVideoById_Authorized() {
        Video video = new Video();
        video.setUserId(1L);
        when(videoService.getVideoById(1L)).thenReturn(video);
        User user = new User();
        user.setId(1L);
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(user);
        ResponseEntity<Video> response = videoController.getVideoById(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(video, response.getBody());
    }

    @Test
    void testGetVideoById_DifferentOwner_Returns401() {
        Video video = new Video();
        video.setUserId(2L);
        when(videoService.getVideoById(1L)).thenReturn(video);
        User user = new User();
        user.setId(1L);
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(user);
        ResponseEntity<Video> response = videoController.getVideoById(1L);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testGetVideoById_UserNotFound_Returns401() {
        Video video = new Video();
        video.setUserId(1L);
        when(videoService.getVideoById(1L)).thenReturn(video);
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(null);
        ResponseEntity<Video> response = videoController.getVideoById(1L);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testGetVideoById_VideoNotFound_Returns404() {
        when(videoService.getVideoById(1L)).thenThrow(new IllegalArgumentException("not found"));
        ResponseEntity<Video> response = videoController.getVideoById(1L);
        assertEquals(404, response.getStatusCodeValue());
    }

    // ── streamVideo ───────────────────────────────────────────────────────────

    @Test
    void testStreamVideo_NoRangeHeader() throws Exception {
        Video video = new Video();
        video.setFilePath("video.mp4");
        when(videoService.getVideoById(1L)).thenReturn(video);
        Path path = Files.createTempFile("stream-success", ".mp4");
        Files.write(path, new byte[100]);
        when(storageService.retrieveFile("video.mp4")).thenReturn(path);
        ResponseEntity<ResourceRegion> response = videoController.streamVideo(1L, new HttpHeaders());
        assertEquals(206, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0L, response.getBody().getPosition());
    }

    @Test
    void testStreamVideo_WithRangeHeader() throws Exception {
        Video video = new Video();
        video.setFilePath("video.mp4");
        when(videoService.getVideoById(1L)).thenReturn(video);
        Path path = Files.createTempFile("stream-range", ".mp4");
        Files.write(path, new byte[100]);
        when(storageService.retrieveFile("video.mp4")).thenReturn(path);
        HttpHeaders rangeHeaders = new HttpHeaders();
        rangeHeaders.setRange(List.of(HttpRange.createByteRange(10, 49)));
        ResponseEntity<ResourceRegion> response = videoController.streamVideo(1L, rangeHeaders);
        assertEquals(206, response.getStatusCodeValue());
        assertEquals(10L, response.getBody().getPosition());
        assertEquals(40L, response.getBody().getCount());
    }

    @Test
    void testStreamVideo_VideoNotFound_Returns404() throws Exception {
        when(videoService.getVideoById(99L)).thenThrow(new IllegalArgumentException("not found"));
        ResponseEntity<ResourceRegion> response = videoController.streamVideo(99L, new HttpHeaders());
        assertEquals(404, response.getStatusCodeValue());
    }

    // ── deleteVideo ───────────────────────────────────────────────────────────

    @Test
    void testDeleteVideo_Success() {
        when(authentication.getName()).thenReturn("user1");
        User user = new User();
        user.setId(1L);
        when(userService.getUserByUsername("user1")).thenReturn(user);
        Video video = new Video();
        video.setUserId(1L);
        when(videoService.getVideoById(1L)).thenReturn(video);
        doNothing().when(videoService).deleteVideo(1L);
        ResponseEntity<Void> response = videoController.deleteVideo(1L);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testDeleteVideo_Forbidden_DifferentOwner() {
        when(authentication.getName()).thenReturn("user1");
        User user = new User();
        user.setId(1L);
        when(userService.getUserByUsername("user1")).thenReturn(user);
        Video video = new Video();
        video.setUserId(2L);
        when(videoService.getVideoById(1L)).thenReturn(video);
        ResponseEntity<Void> response = videoController.deleteVideo(1L);
        assertEquals(403, response.getStatusCodeValue());
        verify(videoService, never()).deleteVideo(anyLong());
    }

    @Test
    void testDeleteVideo_UserNotFound_Returns401() {
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(null);
        ResponseEntity<Void> response = videoController.deleteVideo(1L);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testDeleteVideo_ServiceThrows_Returns500() {
        when(authentication.getName()).thenReturn("user1");
        User user = new User();
        user.setId(1L);
        when(userService.getUserByUsername("user1")).thenReturn(user);
        Video video = new Video();
        video.setUserId(1L);
        when(videoService.getVideoById(1L)).thenReturn(video);
        doThrow(new RuntimeException()).when(videoService).deleteVideo(1L);
        ResponseEntity<Void> response = videoController.deleteVideo(1L);
        assertEquals(500, response.getStatusCodeValue());
    }

    // ── updateVideoStatus ─────────────────────────────────────────────────────

    @Test
    void testUpdateVideoStatus_Success() {
        when(authentication.getName()).thenReturn("user1");
        User user = new User();
        user.setId(1L);
        when(userService.getUserByUsername("user1")).thenReturn(user);
        Video video = new Video();
        video.setUserId(1L);
        when(videoService.getVideoById(1L)).thenReturn(video);
        Video updated = new Video();
        updated.setStatus(VideoStatus.READY);
        when(videoService.updateVideoStatus(1L, VideoStatus.READY)).thenReturn(updated);
        ResponseEntity<Video> response = videoController.updateVideoStatus(1L, VideoStatus.READY);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(VideoStatus.READY, response.getBody().getStatus());
    }

    @Test
    void testUpdateVideoStatus_Forbidden_DifferentOwner() {
        when(authentication.getName()).thenReturn("user1");
        User user = new User();
        user.setId(1L);
        when(userService.getUserByUsername("user1")).thenReturn(user);
        Video video = new Video();
        video.setUserId(2L);
        when(videoService.getVideoById(1L)).thenReturn(video);
        ResponseEntity<Video> response = videoController.updateVideoStatus(1L, VideoStatus.READY);
        assertEquals(403, response.getStatusCodeValue());
        verify(videoService, never()).updateVideoStatus(anyLong(), any());
    }

    @Test
    void testUpdateVideoStatus_UserNotFound_Returns401() {
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(null);
        ResponseEntity<Video> response = videoController.updateVideoStatus(1L, VideoStatus.READY);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testUpdateVideoStatus_VideoNotFound_Returns404() {
        when(authentication.getName()).thenReturn("user1");
        User user = new User();
        user.setId(1L);
        when(userService.getUserByUsername("user1")).thenReturn(user);
        when(videoService.getVideoById(1L)).thenThrow(new IllegalArgumentException("not found"));
        ResponseEntity<Video> response = videoController.updateVideoStatus(1L, VideoStatus.READY);
        assertEquals(404, response.getStatusCodeValue());
    }
}
