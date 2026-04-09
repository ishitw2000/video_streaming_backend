package com.ishitwa.video_streaming.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VideoStatusTest {

    @Test
    void testGetStatus() {
        assertEquals("UPLOADED", VideoStatus.UPLOADED.getStatus());
        assertEquals("PROCESSING", VideoStatus.PROCESSING.getStatus());
        assertEquals("READY", VideoStatus.READY.getStatus());
        assertEquals("FAILED", VideoStatus.FAILED.getStatus());
    }
}

