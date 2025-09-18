package com.ishitwa.video_streaming.service;

import com.ishitwa.video_streaming.model.Video;
import com.ishitwa.video_streaming.repository.VideoRepository;
import com.ishitwa.video_streaming.utils.VideoStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoServiceImplTest {
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private StorageServiceImpl storageService;
    @Mock
    private MultipartFile file;
    @InjectMocks
    private VideoServiceImpl videoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadVideo_Success() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("video/mp4");
        when(file.getSize()).thenReturn(12345L);
        when(storageService.saveFile(file)).thenReturn("/path/to/video.mp4");
        Video video = new Video();
        when(videoRepository.save(any(Video.class))).thenReturn(video);
        Video result = videoService.uploadVideo("Title", "Description", file, 1L);
        assertNotNull(result);
        verify(videoRepository).save(any(Video.class));
    }

    @Test
    void testUploadVideo_EmptyFile() {
        when(file.isEmpty()).thenReturn(true);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            videoService.uploadVideo("Title", "Description", file, 1L);
        });
        assertEquals("Video file cannot be empty", ex.getMessage());
    }

    @Test
    void testUploadVideo_EmptyTitle() {
        when(file.isEmpty()).thenReturn(false);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            videoService.uploadVideo("", "Description", file, 1L);
        });
        assertEquals("Title cannot be empty", ex.getMessage());
    }

    @Test
    void testUploadVideo_NullTitle() {
        when(file.isEmpty()).thenReturn(false);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            videoService.uploadVideo(null, "Description", file, 1L);
        });
        assertEquals("Title cannot be empty", ex.getMessage());
    }
}

