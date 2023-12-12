package com.nagarro.weatherforecast.config;

/**
 *  @author rishabhsinghla
 *  Web Client configs for weather api's
 */

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Configuration
public class WeatherApiConfig {

	@Value("${accuweather.api.url}")
	private String accuWeatherApiUrl;

	@Value("${accuweather.api.key}")
	private String accuWeatherApiKey;

	@Value("${openweathermap.api.url}")
	private String openWeatherMapApiUrl;

	@Value("${openweathermap.api.key}")
	private String openWeatherMapApiKey;

	@Bean
	public WebClient accuWeatherClient() {
		return WebClient.builder().baseUrl(accuWeatherApiUrl)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.defaultUriVariables(Collections.singletonMap("url", accuWeatherApiUrl)).filter(logRequest())
				.filter(logResponse()).build();
	}

	@Bean
	public WebClient openWeatherMapClient() {
		return WebClient.builder().baseUrl(openWeatherMapApiUrl)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.defaultUriVariables(Collections.singletonMap("url", openWeatherMapApiUrl)).filter(logRequest())
				.filter(logResponse()).build();
	}

	private ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
			clientRequest.headers()
					.forEach((name, values) -> values.forEach(value -> System.out.println(name + "=" + value)));
			return Mono.just(clientRequest);
		});
	}

	private ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
			System.out.println("Response: " + clientResponse.statusCode());
			clientResponse.headers().asHttpHeaders()
					.forEach((name, values) -> values.forEach(value -> System.out.println(name + "=" + value)));
			return Mono.just(clientResponse);
		});
	}
}
