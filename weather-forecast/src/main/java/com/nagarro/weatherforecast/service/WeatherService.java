package com.nagarro.weatherforecast.service;

/**
 *  @author rishabhsinghla
 *  Weather service contains business logic for retrieving weather data from both API's
 *  and merging and transforming the data
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.nagarro.weatherforecast.model.WeatherForecast;

@Service
public class WeatherService {

	private final ExecutorService executorService = Executors.newFixedThreadPool(3);

	@Autowired
	private WebClient accuWeatherClient;

	@Autowired
	private WebClient openWeatherMapClient;

	public CompletableFuture<WeatherForecast> getWeatherForecast(String city, String zipCode, String countryCode) {

		CompletableFuture<Map<String, Object>> accuWeatherLocationKeyFuture = CompletableFuture.supplyAsync(() -> {
			ParameterizedTypeReference<List<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {
			};
			return accuWeatherClient.get()
					.uri(uriBuilder -> uriBuilder.path("/locations/v1/search").queryParam("q", city)
							.queryParam("apikey", "dtRxUGGcQ0Ala7ZGh4WxS33aGvmAzhDg").build())
					.retrieve().bodyToMono(responseType).block().stream().findFirst().orElse(Collections.emptyMap());
		}, executorService);

		CompletableFuture<Map<String, Object>> accuWeatherResponseFuture = accuWeatherLocationKeyFuture
				.thenCompose(locationKey -> {
					if (locationKey.isEmpty()) {
						return CompletableFuture.completedFuture(Collections.emptyMap());
					}

					String locationKeyString = locationKey.get("Key").toString();
					ParameterizedTypeReference<List<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {
					};
					return accuWeatherClient.get()
							.uri(uriBuilder -> uriBuilder.path("/currentconditions/v1/" + locationKeyString)
									.queryParam("apikey", "dtRxUGGcQ0Ala7ZGh4WxS33aGvmAzhDg").build())
							.retrieve().bodyToMono(responseType).toFuture().thenApply(
									responseList -> responseList.stream().findFirst().orElse(Collections.emptyMap()));

				});

		CompletableFuture<Map<String, Object>> openWeatherMapResponseFuture = CompletableFuture.supplyAsync(() -> {
			ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
			};
			return openWeatherMapClient.get()
					.uri(uriBuilder -> uriBuilder.path("/geo/1.0/zip").queryParam("zip", zipCode + "," + countryCode)
							.queryParam("appid", "69017e0b57a66b777aec1aec85cb05a0").build())
					.retrieve().bodyToMono(responseType).block();
		}, executorService);

		CompletableFuture<Map<String, Object>> weatherForecastFuture = openWeatherMapResponseFuture
				.thenCompose(openWeatherMapResponse -> {
					if (openWeatherMapResponse.isEmpty()) {
						return CompletableFuture.completedFuture(Collections.emptyMap());
					}

					Double lat = (Double) openWeatherMapResponse.get("lat");
					Double lon = (Double) openWeatherMapResponse.get("lon");

					ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
					};

					return openWeatherMapClient.get()
							.uri(uriBuilder -> uriBuilder.path("/data/2.5/weather").queryParam("lat", lat)
									.queryParam("lon", lon).queryParam("appid", "69017e0b57a66b777aec1aec85cb05a0")
									.build())
							.retrieve().bodyToMono(responseType).toFuture()
							.thenApply(responseMap -> responseMap != null ? responseMap : Collections.emptyMap());
				});

		return CompletableFuture.allOf(accuWeatherResponseFuture, weatherForecastFuture).thenApplyAsync(ignoredVoid -> {
			Map<String, Object> accuWeatherResponse = accuWeatherResponseFuture.join();
			Map<String, Object> openWeatherMapResponse = weatherForecastFuture.join();
			System.out.println("accuWeatherResponse: " + accuWeatherResponse);
			System.out.println("openWeatherMapResponse: " + openWeatherMapResponse);
			Map<String, Object> mergedResponse = mergeResponses(accuWeatherResponse, openWeatherMapResponse);
			return transformToWeatherForecast(mergedResponse);
		}, executorService);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> mergeResponses(Map<String, Object> accuWeatherResponse,
			Map<String, Object> openWeatherMapResponse) {
		Map<String, Object> mergedResponse = new HashMap<>();

		String accuWeatherText = (String) accuWeatherResponse.get("WeatherText");
		boolean accuWeatherHasPrecipitation = (boolean) accuWeatherResponse.get("HasPrecipitation");
		Object accuWeatherPrecipitationType = accuWeatherResponse.get("PrecipitationType");
		boolean accuWeatherIsDayTime = (boolean) accuWeatherResponse.get("IsDayTime");

		mergedResponse.put("WeatherText", accuWeatherText);
		mergedResponse.put("HasPrecipitation", accuWeatherHasPrecipitation);
		mergedResponse.put("PrecipitationType", accuWeatherPrecipitationType);
		mergedResponse.put("IsDayTime", accuWeatherIsDayTime);

		Map<String, Object> openWeatherMapMain = (Map<String, Object>) openWeatherMapResponse.get("main");
		double openWeatherMapTemperature = (double) openWeatherMapMain.get("temp");
		double openWeatherMapFeelsLike = (double) openWeatherMapMain.get("feels_like");
		int openWeatherMapPressure = (int) openWeatherMapMain.get("pressure");
		int openWeatherMapHumidity = (int) openWeatherMapMain.get("humidity");
		int openWeatherMapVisibility = (int) openWeatherMapResponse.get("visibility");

		mergedResponse.put("Temperature", createTemperatureMap(openWeatherMapTemperature));
		mergedResponse.put("feels_like", createTemperatureMap(openWeatherMapFeelsLike));
		mergedResponse.put("pressure", openWeatherMapPressure);
		mergedResponse.put("humidity", openWeatherMapHumidity);
		mergedResponse.put("visibility", openWeatherMapVisibility);

		Map<String, Object> openWeatherMapWind = (Map<String, Object>) openWeatherMapResponse.get("wind");
		mergedResponse.put("wind", openWeatherMapWind);

		Long openWeatherMapSunriseValue = (Long) mergedResponse.get("sunrise");
		Long openWeatherMapSunsetValue = (Long) mergedResponse.get("sunset");

		long openWeatherMapSunrise = (openWeatherMapSunriseValue != null) ? openWeatherMapSunriseValue : 0;
		long openWeatherMapSunset = (openWeatherMapSunsetValue != null) ? openWeatherMapSunsetValue : 0;

		mergedResponse.put("sunrise", openWeatherMapSunrise);
		mergedResponse.put("sunset", openWeatherMapSunset);

		System.out.println("mergedResponse: " + mergedResponse);
		return mergedResponse;
	}

	@SuppressWarnings("unchecked")
	private WeatherForecast transformToWeatherForecast(Map<String, Object> mergedResponse) {
		WeatherForecast weatherForecast = new WeatherForecast();

		weatherForecast.setWeatherText((String) mergedResponse.get("WeatherText"));
		weatherForecast.setHasPrecipitation((boolean) mergedResponse.get("HasPrecipitation"));
		weatherForecast.setPrecipitationType((String) mergedResponse.get("PrecipitationType"));
		weatherForecast.setDayTime((boolean) mergedResponse.get("IsDayTime"));

		Map<String, Object> temperatureDetails = (Map<String, Object>) mergedResponse.get("Temperature");
		weatherForecast.setTemperature(createTemperatureObject(temperatureDetails));

		Map<String, Object> feelsLikeDetails = (Map<String, Object>) mergedResponse.get("feels_like");
		weatherForecast.setFeelsLike(createTemperatureObject(feelsLikeDetails));

		weatherForecast.setPressure((int) mergedResponse.get("pressure"));
		weatherForecast.setHumidity((int) mergedResponse.get("humidity"));
		weatherForecast.setVisibility((int) mergedResponse.get("visibility"));

		Map<String, Object> windDetails = (Map<String, Object>) mergedResponse.get("wind");
		weatherForecast.setWind(createWindObject(windDetails));

		weatherForecast.setSunrise((long) mergedResponse.get("sunrise"));
		weatherForecast.setSunset((long) mergedResponse.get("sunset"));

		return weatherForecast;
	}

	private WeatherForecast.Temperature createTemperatureObject(Map<String, Object> temperatureDetails) {
		WeatherForecast.Temperature temperature = new WeatherForecast.Temperature();
		temperature.setValue((double) temperatureDetails.get("Value"));
		temperature.setUnit((String) temperatureDetails.get("Unit"));
		return temperature;
	}

	private WeatherForecast.Wind createWindObject(Map<String, Object> windDetails) {
		WeatherForecast.Wind wind = new WeatherForecast.Wind();
		if (windDetails != null) {
			wind.setSpeed(windDetails.get("speed") != null ? (double) windDetails.get("speed") : 0.0);
			wind.setDeg(windDetails.get("deg") != null ? (int) windDetails.get("deg") : 0);
			wind.setGust(windDetails.get("gust") != null ? (double) windDetails.get("gust") : 0.0);
		}
		return wind;
	}

	private Map<String, Object> createTemperatureMap(double value) {
		Map<String, Object> temperatureMap = new HashMap<>();
		temperatureMap.put("Value", value);
		temperatureMap.put("Unit", "C");
		return temperatureMap;
	}

	@PreDestroy
	public void shutdownExecutorService() {
		executorService.shutdown();
	}
}
