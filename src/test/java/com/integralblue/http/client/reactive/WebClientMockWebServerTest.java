package com.integralblue.http.client.reactive;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WebClientMockWebServerTest.TestApplication.class)
/* default */ class WebClientMockWebServerTest {

	private MockWebServer mockWebServer;

	@BeforeEach
	/* default */ void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
	}

	@AfterEach
	/* default */ void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	/* default */ void testWebClient(final @Autowired WebClient.Builder webClientBuilder) throws Exception {
		final String path = "/somePath";
		final String url = mockWebServer.url(path).toString();
		final String response = "success";
		mockWebServer.enqueue(new MockResponse().setBody(response));

		final WebClient webClient = webClientBuilder.build();
		assertThat(webClient.get().uri(url).retrieve().toEntity(String.class).block()).extracting(ResponseEntity::getBody, InstanceOfAssertFactories.STRING).isEqualTo(response);
		final RecordedRequest recordedRequest = mockWebServer.takeRequest();

		assertThat(recordedRequest.getMethod()).isEqualTo("GET");
		assertThat(recordedRequest.getPath()).isEqualTo(path);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@SuppressWarnings({"checkstyle:HideUtilityClassConstructor", "PMD.UseUtilityClass"})
	public static class TestApplication {
	}

}
