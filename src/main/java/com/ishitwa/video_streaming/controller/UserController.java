package com.ishitwa.video_streaming.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ishitwa.video_streaming.dao.UserLogin;
import com.ishitwa.video_streaming.model.User;
import com.ishitwa.video_streaming.service.UserServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
public class UserController {

	@Autowired
	private UserServiceImpl userService;

	@PostMapping("/register")
	public User registerUser(@RequestBody User user) {
		User entity = userService.registerUser(user);
		return entity;
	}

	@PostMapping("/login")
	public String loginUser(@RequestBody UserLogin login) {
		User user = userService.getUserByUsername(login.getUsername());
		return user != null ? "Login successful" : "Login failed";
	}

}
