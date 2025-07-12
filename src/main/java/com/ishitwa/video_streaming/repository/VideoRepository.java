package com.ishitwa.video_streaming.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import com.ishitwa.video_streaming.model.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
	Video findByTitle(String title);

	void deleteByTitle(String title);

	@NonNull
	Video save(@NonNull Video video);

	Video findByUserId(Long userId);

	List<Video> findAllByUserId(Long userId);
}
