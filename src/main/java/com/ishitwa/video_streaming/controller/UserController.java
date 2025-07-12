package com.ishitwa.video_streaming.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ishitwa.video_streaming.dao.UserLogin;
import com.ishitwa.video_streaming.model.User;
import com.ishitwa.video_streaming.service.UserServiceImpl;
import com.ishitwa.video_streaming.utils.JwtUtil;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
public class UserController {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserServiceImpl userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostMapping("/register")
	public User registerUser(@RequestBody User user) {
		User entity = userService.registerUser(user);
		return entity;
	}

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody UserLogin login) {
		User user = userService.getUserByUsername(login.getUsername());
		if (user != null && passwordEncoder.matches(login.getPassword(), user.getPassword())) {
			String token = jwtUtil.generateToken(user.getUsername());
			return ResponseEntity.ok(Collections.singletonMap("token", token));
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login Failed");
	}

}
