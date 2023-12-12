package com.nagarro.weatherforecast.controller;

/**
 *  @author rishabhsinghla
 *  Weather controller for getting weather data
 */

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nagarro.weatherforecast.model.WeatherForecast;
import com.nagarro.weatherforecast.service.WeatherService;

@RestController
@RequestMapping("/weather")
public class WeatherController {

	@Autowired
	private WeatherService weatherForecastService;

	@GetMapping
	public CompletableFuture<WeatherForecast> getWeatherForecast(@RequestParam String city, @RequestParam String zip,
			@RequestParam String country) {
		return weatherForecastService.getWeatherForecast(city, zip, country);
	}
}
