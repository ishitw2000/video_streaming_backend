package com.ishitwa.video_streaming.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.util.StringUtils;

@Service
public class StorageServiceImpl implements StorageService {

	@Value("${storage.upload-dir}")
	private String uploadDir;

	private final Path rootLocation;

	public StorageServiceImpl() {
		this.rootLocation = Path.of(uploadDir);
		init();
	}

	private void init() {
		try {
			if (!java.nio.file.Files.exists(rootLocation)) {
				java.nio.file.Files.createDirectories(rootLocation);
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not initialize storage", e);
		}
	}

	@Override
	public String saveFile(MultipartFile file) {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("Cannot save empty file");
		}
		String originalFilename = file.getOriginalFilename();
		String fileName = StringUtils.cleanPath(originalFilename != null ? originalFilename : "unknown_file");
		String uniqueFilename = UUID.randomUUID().toString() + "_" + fileName;
		try {
			if (fileName.contains("..")) {
				throw new IllegalArgumentException(
						"Cannot store file with relative path outside current directory: " + fileName);
			}
			Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename)).normalize().toAbsolutePath();
			if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
				throw new IllegalArgumentException("Cannot store file outside current directory: " + fileName);
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
			}
			return uniqueFilename;
		} catch (Exception e) {
			throw new RuntimeException("Failed to store file " + fileName, e);
		}
	}

	@Override
	public Path retrieveFile(String filename) {
		try {
			Path file = rootLocation.resolve(filename);
			if (Files.exists(file)) {
				return file;
			} else {
				throw new RuntimeException("File not found: " + filename);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to retrieve file " + filename, e);
		}
	}

	@Override
	public void deleteFile(String filePath) {
		try {
			Path file = rootLocation.resolve(filePath);
			if (Files.exists(file)) {
				Files.delete(file);
			} else {
				throw new RuntimeException("File not found: " + filePath);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to delete file " + filePath, e);
		}
	}

	@Override
	public boolean fileExists(String filePath) {
		Path file = rootLocation.resolve(filePath);
		return Files.exists(file);
	}

}
