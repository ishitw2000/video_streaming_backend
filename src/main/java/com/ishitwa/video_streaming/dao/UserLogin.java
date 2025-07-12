package com.ishitwa.video_streaming.dao;

import lombok.Data;

@Data
public class UserLogin {
	private String username;
	private String password;

	public UserLogin() {
	}

	public UserLogin(String username, String password) {
		this.username = username;
		this.password = password;
	}
}
