package com.integralblue.http.client.reactive;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;

/**
 * Configuration for the {@link ClientHttpConnector} to use the Java 11 HTTP client.
 *
 * @author Craig Andrews
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html">Java HttpClient</a>
 */
@AutoConfigureBefore({WebClientAutoConfiguration.class, ClientHttpConnectorAutoConfiguration.class})
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ClientHttpConnector.class, HttpClient.class})
@ConditionalOnMissingBean(ClientHttpConnector.class)
public class JdkClientHttpConnectorConfiguration {
	@Bean
	@ConditionalOnMissingBean
	public Builder httpClientBuilder() {
		return HttpClient.newBuilder();
	}

	@Bean
	@ConditionalOnMissingBean
	public HttpClient httpClient(final @Autowired Builder builder) {
		return builder.build();
	}

	@Bean
	public JdkClientHttpConnector jdkClientHttpConnector(final HttpClient httpClient) {
		return new JdkClientHttpConnector(httpClient, new DefaultDataBufferFactory());
	}
}
