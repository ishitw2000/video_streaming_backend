package com.ishitwa.video_streaming;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
class VideoStreamingApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainStartsSpringApplication() {
		try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
			VideoStreamingApplication.main(new String[] {"--spring.main.banner-mode=off"});
			springApplication.verify(() -> SpringApplication.run(VideoStreamingApplication.class,
					new String[] {"--spring.main.banner-mode=off"}));
		}
	}

}
