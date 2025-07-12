package com.ishitwa.video_streaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import com.ishitwa.video_streaming.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	User findByUsername(String username);

	void deleteByUsername(String username);

	@NonNull
	User save(@NonNull User user);
}
