package com.ishitwa.video_streaming.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StorageServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveFile_Success() {
        StorageServiceImpl storageService = new StorageServiceImpl(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", new byte[] {1, 2, 3});

        String storedName = storageService.saveFile(file);

        assertNotNull(storedName);
        assertTrue(storedName.endsWith("_video.mp4"));
        assertTrue(storageService.fileExists(storedName));
    }

    @Test
    void testSaveFile_EmptyFile() {
        StorageServiceImpl storageService = new StorageServiceImpl(tempDir.toString());
        MockMultipartFile empty = new MockMultipartFile("file", "video.mp4", "video/mp4", new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> storageService.saveFile(empty));

        assertEquals("Cannot save empty file", ex.getMessage());
    }

    @Test
    void testSaveFile_PathTraversalName() {
        StorageServiceImpl storageService = new StorageServiceImpl(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "../video.mp4", "video/mp4", new byte[] {1});

        RuntimeException ex = assertThrows(RuntimeException.class, () -> storageService.saveFile(file));

        assertTrue(ex.getMessage().contains("Failed to store file"));
    }

    @Test
    void testRetrieveFile_Success() throws IOException {
        StorageServiceImpl storageService = new StorageServiceImpl(tempDir.toString());
        Path file = Files.write(tempDir.resolve("demo.mp4"), new byte[] {9, 8});

        Path result = storageService.retrieveFile("demo.mp4");

        assertEquals(file, result);
    }

    @Test
    void testRetrieveFile_NotFound() {
        StorageServiceImpl storageService = new StorageServiceImpl(tempDir.toString());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> storageService.retrieveFile("missing.mp4"));

        assertTrue(ex.getMessage().contains("Failed to retrieve file missing.mp4"));
    }

    @Test
    void testDeleteFile_Success() throws IOException {
        StorageServiceImpl storageService = new StorageServiceImpl(tempDir.toString());
        Files.write(tempDir.resolve("delete.mp4"), new byte[] {1});

        storageService.deleteFile("delete.mp4");

        assertFalse(Files.exists(tempDir.resolve("delete.mp4")));
    }

    @Test
    void testDeleteFile_NotFound() {
        StorageServiceImpl storageService = new StorageServiceImpl(tempDir.toString());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> storageService.deleteFile("missing.mp4"));

        assertTrue(ex.getMessage().contains("Failed to delete file missing.mp4"));
    }

    @Test
    void testFileExists_False() {
        StorageServiceImpl storageService = new StorageServiceImpl(tempDir.toString());
        assertFalse(storageService.fileExists("missing.mp4"));
    }

    @Test
    void testFileExists_True() throws IOException {
        StorageServiceImpl storageService = new StorageServiceImpl(tempDir.toString());
        Files.write(tempDir.resolve("exists.mp4"), new byte[]{1});
        assertTrue(storageService.fileExists("exists.mp4"));
    }

    @Test
    void testRetrieveFile_PathTraversal_Blocked() {
        StorageServiceImpl storageService = new StorageServiceImpl(tempDir.toString());
        assertThrows(IllegalArgumentException.class,
                () -> storageService.retrieveFile("../outside.mp4"));
    }
}

