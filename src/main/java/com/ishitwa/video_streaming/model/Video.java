package com.ishitwa.video_streaming.model;

import java.sql.Time;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import com.ishitwa.video_streaming.utils.VideoStatus;

@Data
@Entity
public class Video {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String title;
	private String description;
	private String filePath;
	private String fileType;
	private Long size;
	private Long userId;
	private Time uploadTime;
	private VideoStatus status;
}
