package com.ishitwa.video_streaming.controller;

import com.ishitwa.video_streaming.model.User;
import com.ishitwa.video_streaming.model.Video;
import com.ishitwa.video_streaming.service.StorageService;
import com.ishitwa.video_streaming.service.UserServiceImpl;
import com.ishitwa.video_streaming.service.VideoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
    void testUploadVideo_Unauthorized() {
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(null);
        ResponseEntity<Video> response = videoController.uploadVideo(file, "title", "desc");
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testUploadVideo_InternalError() {
        when(authentication.getName()).thenReturn("user1");
        User user = new User();
        user.setId(1L);
        when(userService.getUserByUsername("user1")).thenReturn(user);
        when(videoService.uploadVideo(anyString(), anyString(), eq(file), eq(1L))).thenThrow(new RuntimeException());
        ResponseEntity<Video> response = videoController.uploadVideo(file, "title", "desc");
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testGetAllVideos() {
        Video v1 = new Video();
        Video v2 = new Video();
        when(videoService.getAllVideos()).thenReturn(Arrays.asList(v1, v2));
        ResponseEntity<List<Video>> response = videoController.getAllVideos();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetVideosByUser_Authorized() {
        Video v1 = new Video();
        when(videoService.getAllVideosByUserId(1L)).thenReturn(Arrays.asList(v1));
        User user = new User();
        user.setId(1L);
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(user);
        ResponseEntity<List<Video>> response = videoController.getVideosByUser(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetVideosByUser_Unauthorized() {
        Video v1 = new Video();
        when(videoService.getAllVideosByUserId(2L)).thenReturn(Arrays.asList(v1));
        User user = new User();
        user.setId(1L);
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(user);
        ResponseEntity<List<Video>> response = videoController.getVideosByUser(2L);
        assertEquals(401, response.getStatusCodeValue());
    }

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
    void testGetVideoById_Unauthorized() {
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
    void testGetVideoById_NotFound() {
        when(videoService.getVideoById(1L)).thenThrow(new IllegalArgumentException());
        User user = new User();
        user.setId(1L);
        when(authentication.getName()).thenReturn("user1");
        when(userService.getUserByUsername("user1")).thenReturn(user);
        ResponseEntity<Video> response = videoController.getVideoById(1L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testStreamVideo_Success() throws Exception {
        Video video = new Video();
        video.setFileType("video/mp4");
        video.setFilePath("/path/to/video.mp4");
        when(videoService.getVideoById(1L)).thenReturn(video);
        Path path = mock(Path.class);
        when(storageService.retrieveFile("/path/to/video.mp4")).thenReturn(path);
        org.springframework.core.io.UrlResource resource = mock(org.springframework.core.io.UrlResource.class);
        when(resource.contentLength()).thenReturn(100L);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[100]));
        // Mock UrlResource instantiation
        try (var mocked = org.mockito.Mockito.mockStatic(org.springframework.core.io.UrlResource.class)) {
            mocked.when(() -> new org.springframework.core.io.UrlResource(path.toUri())).thenReturn(resource);
            ResponseEntity<Resource> response = videoController.streamVideo(1L, null);
            assertEquals(200, response.getStatusCodeValue());
            assertTrue(response.getHeaders().get("Content-Type").contains("video/mp4"));
        }
    }

    @Test
    void testStreamVideo_RangeHeader() throws Exception {
        Video video = new Video();
        video.setFileType("video/mp4");
        video.setFilePath("/path/to/video.mp4");
        when(videoService.getVideoById(1L)).thenReturn(video);
        Path path = mock(Path.class);
        when(storageService.retrieveFile("/path/to/video.mp4")).thenReturn(path);
        org.springframework.core.io.UrlResource resource = mock(org.springframework.core.io.UrlResource.class);
        when(resource.contentLength()).thenReturn(100L);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[100]));
        try (var mocked = org.mockito.Mockito.mockStatic(org.springframework.core.io.UrlResource.class)) {
            mocked.when(() -> new org.springframework.core.io.UrlResource(path.toUri())).thenReturn(resource);
            ResponseEntity<Resource> response = videoController.streamVideo(1L, "bytes=10-49");
            assertEquals(206, response.getStatusCodeValue());
            assertTrue(response.getHeaders().get("Content-Range").get(0).startsWith("bytes10-49/"));
        }
    }

    @Test
    void testDeleteVideo_Success() {
        doNothing().when(videoService).deleteVideo(1L);
        ResponseEntity<Void> response = videoController.deleteVideo(1L);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeleteVideo_Failure() {
        doThrow(new IllegalArgumentException()).when(videoService).deleteVideo(1L);
        ResponseEntity<Void> response = videoController.deleteVideo(1L);
        assertEquals(404, response.getStatusCodeValue());
    }

    // Additional tests can be added here
}
