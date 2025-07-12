package com.ishitwa.video_streaming.service;

import com.ishitwa.video_streaming.model.User;

public interface UserService {
	User registerUser(User user);

	User getUserByUsername(String username);

	void deleteUser(String username);

	User updateUser(User user);
}
