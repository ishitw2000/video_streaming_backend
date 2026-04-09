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

import java.util.List;
import java.util.Optional;

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

    @Test
    void testUploadVideo_InvalidFileType() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            videoService.uploadVideo("Title", "Description", file, 1L);
        });
        assertEquals("Invalid file type. Only video files are allowed.", ex.getMessage());
    }

    @Test
    void testUploadVideo_NullContentType() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(null);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            videoService.uploadVideo("Title", "Description", file, 1L);
        });
        assertEquals("Invalid file type. Only video files are allowed.", ex.getMessage());
    }

    @Test
    void testUploadVideo_StorageFailure() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("video/mp4");
        when(storageService.saveFile(file)).thenThrow(new RuntimeException("disk full"));
        assertThrows(RuntimeException.class, () ->
                videoService.uploadVideo("Title", "Desc", file, 1L));
    }

    @Test
    void testGetVideoById_Found() {
        Video video = new Video();
        when(videoRepository.findById(10L)).thenReturn(Optional.of(video));

        Video result = videoService.getVideoById(10L);

        assertSame(video, result);
    }

    @Test
    void testGetVideoById_NotFound() {
        when(videoRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> videoService.getVideoById(10L));

        assertEquals("Video not found with id: 10", ex.getMessage());
    }

    @Test
    void testDeleteVideo_Success() {
        Video video = new Video();
        video.setFilePath("stored.mp4");
        when(videoRepository.findById(2L)).thenReturn(Optional.of(video));

        videoService.deleteVideo(2L);

        verify(storageService).deleteFile("stored.mp4");
        verify(videoRepository).delete(video);
    }

    @Test
    void testDeleteVideo_NotFound() {
        when(videoRepository.findById(2L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> videoService.deleteVideo(2L));

        assertEquals("Video not found with id: 2", ex.getMessage());
        verify(storageService, never()).deleteFile(anyString());
        verify(videoRepository, never()).delete(any(Video.class));
    }

    @Test
    void testUpdateVideoStatus_Success() {
        Video video = new Video();
        video.setStatus(VideoStatus.UPLOADED);
        when(videoRepository.findById(5L)).thenReturn(Optional.of(video));
        when(videoRepository.save(video)).thenReturn(video);

        Video result = videoService.updateVideoStatus(5L, VideoStatus.READY);

        assertEquals(VideoStatus.READY, result.getStatus());
        verify(videoRepository).save(video);
    }

    @Test
    void testUpdateVideoStatus_NotFound() {
        when(videoRepository.findById(5L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> videoService.updateVideoStatus(5L, VideoStatus.READY));

        assertEquals("Video not found with id: 5", ex.getMessage());
    }

    @Test
    void testGetAllVideosByUserId() {
        List<Video> videos = List.of(new Video(), new Video());
        when(videoRepository.findAllByUserId(7L)).thenReturn(videos);

        List<Video> result = videoService.getAllVideosByUserId(7L);

        assertEquals(2, result.size());
    }

    @Test
    void testGetAllVideos() {
        List<Video> videos = List.of(new Video());
        when(videoRepository.findAll()).thenReturn(videos);

        List<Video> result = videoService.getAllVideos();

        assertEquals(1, result.size());
    }
}

