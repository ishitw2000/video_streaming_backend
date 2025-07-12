package com.ishitwa.video_streaming.service;

import java.nio.file.Path;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface StorageService {

	String saveFile(MultipartFile file);

	Path retrieveFile(String filePath);

	void deleteFile(String filePath);

	boolean fileExists(String filePath);

}
