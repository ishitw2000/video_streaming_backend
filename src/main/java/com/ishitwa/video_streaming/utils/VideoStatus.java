package com.ishitwa.video_streaming.utils;

public enum VideoStatus {
	UPLOADED,
	PROCESSING,
	READY,
	FAILED;

	public String getStatus() {
		return this.name();
	}
}
