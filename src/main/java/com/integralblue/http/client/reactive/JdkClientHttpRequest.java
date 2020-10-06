/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.integralblue.http.client.reactive;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Flow;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.AbstractClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.util.Assert;

/**
 * {@link ClientHttpRequest} implementation for the Java 11 HTTP client.
 *
 * @author Julien Eyraud
 * @author Craig Andrews
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html">Java HttpClient</a>
 */
/* default */ class JdkClientHttpRequest extends AbstractClientHttpRequest {

	private static final Set<String> DISALLOWED_HEADERS = Set.of("connection", "content-length", "date", "expect", "from", "host", "upgrade", "via", "warning");

	private final HttpClient httpClient;

	private final HttpMethod method;

	private final URI uri;

	private final HttpRequest.Builder builder;

	private final DataBufferFactory dataBufferFactory;

	private Mono<ClientHttpResponse> response;


	/* default */ JdkClientHttpRequest(final HttpClient httpClient, final HttpMethod httpMethod, final URI uri, final DataBufferFactory bufferFactory) {
		super();
		Assert.notNull(httpClient, "HttpClient should not be null");
		Assert.notNull(httpMethod, "HttpMethod should not be null");
		Assert.notNull(uri, "URI should not be null");
		Assert.notNull(bufferFactory, "DataBufferFactory should not be null");
		this.httpClient = httpClient;
		this.method = httpMethod;
		this.uri = uri;
		this.builder = HttpRequest.newBuilder(uri);
		this.dataBufferFactory = bufferFactory;
	}

	@Override
	protected void applyHeaders() {
		final HttpHeaders headers = getHeaders();
		for (final Map.Entry<String, List<String>> header : getHeaders().entrySet()) {
			if (!DISALLOWED_HEADERS.contains(header.getKey().toLowerCase(Locale.ROOT))) {
				for (final String value : header.getValue()) {
					this.builder.header(header.getKey(), value);
				}
			}
		}
		if (!headers.containsKey(HttpHeaders.ACCEPT)) {
			this.builder.header(HttpHeaders.ACCEPT, "*/*");
		}
	}

	@Override
	protected void applyCookies() {
		final String cookies = getCookies().values().stream().flatMap(List::stream).map(c -> c.getName() + "=" + c.getValue()).collect(Collectors.joining("; "));
		this.builder.header(HttpHeaders.COOKIE, cookies);
	}

	@Override
	public HttpMethod getMethod() {
		return this.method;
	}

	@Override
	public URI getURI() {
		return this.uri;
	}

	@Override
	public DataBufferFactory bufferFactory() {
		return this.dataBufferFactory;
	}

	@Override
	public Mono<Void> writeWith(final Publisher<? extends DataBuffer> body) {
		return doCommit(() -> {
			final Flow.Publisher<ByteBuffer> flowAdapter = JdkFlowAdapter.publisherToFlowPublisher(Flux.from(body).map(DataBuffer::asByteBuffer));
			final long contentLength = getHeaders().getContentLength();
			final HttpRequest.BodyPublisher bodyPublisher = contentLength >= 0 ? HttpRequest.BodyPublishers.fromPublisher(flowAdapter, contentLength)
					: HttpRequest.BodyPublishers.fromPublisher(flowAdapter);
			this.response = Mono
					.fromCompletionStage(() -> this.httpClient.sendAsync(this.builder.method(this.method.name(), bodyPublisher).build(), HttpResponse.BodyHandlers.ofPublisher()))
					.map(r -> new JdkClientHttpResponse(r, this.dataBufferFactory));
			return Mono.empty();
		});
	}

	@Override
	public Mono<Void> writeAndFlushWith(final Publisher<? extends Publisher<? extends DataBuffer>> body) {
		return writeWith(Flux.from(body).flatMap(Function.identity()));
	}

	@Override
	public Mono<Void> setComplete() {
		if (isCommitted()) {
			return Mono.empty();
		}
		else {
			return doCommit(() -> {
				this.response = Mono
						.fromCompletionStage(() -> this.httpClient.sendAsync(this.builder.method(this.method.name(), HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofPublisher()))
						.map(r -> new JdkClientHttpResponse(r, this.dataBufferFactory));
				return Mono.empty();
			});
		}
	}

	public Mono<ClientHttpResponse> getResponse() {
		return this.response;
	}
}
