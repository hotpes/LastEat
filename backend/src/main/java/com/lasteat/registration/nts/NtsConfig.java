package com.lasteat.registration.nts;

import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class NtsConfig {

	// 국세청이 늦게 응답해도 요청 스레드가 오래 묶이지 않도록 연결·응답 시간을 제한한다
	@Bean
	NtsClient ntsClient(NtsProperties properties) {
		HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(properties.timeout());
		return new NtsApiClient(RestClient.builder().requestFactory(requestFactory), properties);
	}

}
