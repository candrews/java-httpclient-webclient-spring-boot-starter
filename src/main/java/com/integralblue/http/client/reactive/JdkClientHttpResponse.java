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

import java.net.HttpCookie;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow;

import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * {@link ClientHttpResponse} implementation for the Java 11 HTTP client.
 *
 * @author Julien Eyraud
 * @author Craig Andrews
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html">Java HttpClient</a>
 */
/* default */ class JdkClientHttpResponse implements ClientHttpResponse {

	private final HttpResponse<Flow.Publisher<List<ByteBuffer>>> response;

	private final DataBufferFactory bufferFactory;


	/* default */ JdkClientHttpResponse(final HttpResponse<Flow.Publisher<List<ByteBuffer>>> response, final DataBufferFactory bufferFactory) {
		super();
		this.response = response;
		this.bufferFactory = bufferFactory;
	}

	@Override
	public HttpStatus getStatusCode() {
		return HttpStatus.valueOf(getRawStatusCode());
	}

	@Override
	public int getRawStatusCode() {
		return this.response.statusCode();
	}

	@Override
	public MultiValueMap<String, ResponseCookie> getCookies() {
		return this.response
				.headers()
				.allValues(HttpHeaders.SET_COOKIE)
				.stream()
				.map(HttpCookie::parse)
				.flatMap(List::stream)
				.map(httpCookie -> ResponseCookie
						.from(httpCookie.getName(), httpCookie.getValue())
						.domain(httpCookie.getDomain())
						.httpOnly(httpCookie.isHttpOnly())
						.maxAge(httpCookie.getMaxAge())
						.path(httpCookie.getPath())
						.secure(httpCookie.getSecure())
						.build())
				.collect(LinkedMultiValueMap::new, (m, v) -> m.add(v.getName(), v), LinkedMultiValueMap::addAll);
	}

	@Override
	public Flux<DataBuffer> getBody() {
		return JdkFlowAdapter
				.flowPublisherToFlux(this.response.body())
				.flatMap(Flux::fromIterable)
				.map(this.bufferFactory::wrap)
				.doOnDiscard(DataBuffer.class, DataBufferUtils::release);
	}

	@Override
	public HttpHeaders getHeaders() {
		return this.response.headers().map().entrySet().stream().collect(HttpHeaders::new, (headers, entry) -> headers.addAll(entry.getKey(), entry.getValue()), HttpHeaders::addAll);
	}
}
