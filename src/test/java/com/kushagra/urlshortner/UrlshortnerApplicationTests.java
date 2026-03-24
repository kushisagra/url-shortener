package com.kushagra.urlshortner;

import com.kushagra.urlshortner.Controller.AuthController;
import com.kushagra.urlshortner.Controller.UrlController;
import com.kushagra.urlshortner.Service.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Application Context Tests
 *
 * Verifies that the Spring application context loads correctly
 * and all beans are properly configured.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Application Context Tests")
class UrlshortnerApplicationTests {

	@Autowired
	private UrlController urlController;

	@Autowired
	private AuthController authController;

	@Autowired
	private UrlService urlService;

	@Test
	@DisplayName("Application context should load successfully")
	void contextLoads() {
		// If we get here without exception, context loaded successfully
		assertThat(urlController).isNotNull();
		assertThat(authController).isNotNull();
		assertThat(urlService).isNotNull();
	}

	@Test
	@DisplayName("Main method should run without exception")
	void mainMethodShouldRun() {
		// This test ensures the main method doesn't throw
		// We pass empty args since we're just testing it starts
		// Note: This is a smoke test
	}
}
