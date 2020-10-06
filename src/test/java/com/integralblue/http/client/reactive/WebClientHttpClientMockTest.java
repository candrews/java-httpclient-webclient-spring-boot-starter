package com.integralblue.http.client.reactive;

import java.net.http.HttpClient;

import com.pgssoft.httpclient.HttpClientMock;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WebClientHttpClientMockTest.TestApplication.class)
/* default */ class WebClientHttpClientMockTest {

	@Autowired
	private HttpClientMock httpClient;

	@BeforeEach
	/* default */ void before() {
		httpClient.reset();
	}

	@Test
	/* default */ void testWebClient(final @Autowired WebClient.Builder webClientBuilder) {
		final String url = "https://example.com/somepath";
		final String body = "success";
		httpClient.onGet(url).doReturn(body);
		final WebClient webClient = webClientBuilder.build();
		assertThat(webClient.get().uri(url).retrieve().toEntity(String.class).block()).extracting(ResponseEntity::getBody, InstanceOfAssertFactories.STRING).isEqualTo(body);
		httpClient.verify().get(url).called();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@SuppressWarnings({"checkstyle:HideUtilityClassConstructor", "PMD.UseUtilityClass"})
	public static class TestApplication {
		@Bean
		public HttpClient httpClient() {
			return new HttpClientMock();
		}
	}

}
