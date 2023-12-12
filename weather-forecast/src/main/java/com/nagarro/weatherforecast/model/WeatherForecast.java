package com.nagarro.weatherforecast.model;

/**
 *  @author rishabhsinghla
 *  WeatherForecast model for sending weather data in response
 */

import java.util.Objects;

public class WeatherForecast {

	private String weatherText;
	private boolean hasPrecipitation;
	private String precipitationType;
	private boolean isDayTime;
	private Temperature temperature;
	private Temperature feelsLike;
	private int pressure;
	private int humidity;
	private int visibility;
	private Wind wind;
	private long sunrise;
	private long sunset;

	public String getWeatherText() {
		return weatherText;
	}

	public void setWeatherText(String weatherText) {
		this.weatherText = weatherText;
	}

	public boolean isHasPrecipitation() {
		return hasPrecipitation;
	}

	public void setHasPrecipitation(boolean hasPrecipitation) {
		this.hasPrecipitation = hasPrecipitation;
	}

	public String getPrecipitationType() {
		return precipitationType;
	}

	public void setPrecipitationType(String precipitationType) {
		this.precipitationType = precipitationType;
	}

	public boolean isDayTime() {
		return isDayTime;
	}

	public void setDayTime(boolean dayTime) {
		isDayTime = dayTime;
	}

	public Temperature getTemperature() {
		return temperature;
	}

	public void setTemperature(Temperature temperature) {
		this.temperature = temperature;
	}

	public Temperature getFeelsLike() {
		return feelsLike;
	}

	public void setFeelsLike(Temperature feelsLike) {
		this.feelsLike = feelsLike;
	}

	public int getPressure() {
		return pressure;
	}

	public void setPressure(int pressure) {
		this.pressure = pressure;
	}

	public int getHumidity() {
		return humidity;
	}

	public void setHumidity(int humidity) {
		this.humidity = humidity;
	}

	public int getVisibility() {
		return visibility;
	}

	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	public Wind getWind() {
		return wind;
	}

	public void setWind(Wind wind) {
		this.wind = wind;
	}

	public long getSunrise() {
		return sunrise;
	}

	public void setSunrise(long sunrise) {
		this.sunrise = sunrise;
	}

	public long getSunset() {
		return sunset;
	}

	public void setSunset(long sunset) {
		this.sunset = sunset;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		WeatherForecast that = (WeatherForecast) o;
		return hasPrecipitation == that.hasPrecipitation && isDayTime == that.isDayTime && pressure == that.pressure
				&& humidity == that.humidity && visibility == that.visibility && sunrise == that.sunrise
				&& sunset == that.sunset && Objects.equals(weatherText, that.weatherText)
				&& Objects.equals(precipitationType, that.precipitationType)
				&& Objects.equals(temperature, that.temperature) && Objects.equals(feelsLike, that.feelsLike)
				&& Objects.equals(wind, that.wind);
	}

	@Override
	public int hashCode() {
		return Objects.hash(weatherText, hasPrecipitation, precipitationType, isDayTime, temperature, feelsLike,
				pressure, humidity, visibility, wind, sunrise, sunset);
	}

	@Override
	public String toString() {
		return "WeatherForecast{" + "weatherText='" + weatherText + '\'' + ", hasPrecipitation=" + hasPrecipitation
				+ ", precipitationType='" + precipitationType + '\'' + ", isDayTime=" + isDayTime + ", temperature="
				+ temperature + ", feelsLike=" + feelsLike + ", pressure=" + pressure + ", humidity=" + humidity
				+ ", visibility=" + visibility + ", wind=" + wind + ", sunrise=" + sunrise + ", sunset=" + sunset + '}';
	}

	public static class Temperature {
		private double value;
		private String unit;

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}

		public String getUnit() {
			return unit;
		}

		public void setUnit(String unit) {
			this.unit = unit;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Temperature that = (Temperature) o;
			return Double.compare(that.value, value) == 0 && Objects.equals(unit, that.unit);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value, unit);
		}

		@Override
		public String toString() {
			return "Temperature{" + "value=" + value + ", unit='" + unit + '\'' + '}';
		}
	}

	public static class Wind {
		private double speed;
		private int deg;
		private double gust;

		public double getSpeed() {
			return speed;
		}

		public void setSpeed(double speed) {
			this.speed = speed;
		}

		public int getDeg() {
			return deg;
		}

		public void setDeg(int deg) {
			this.deg = deg;
		}

		public double getGust() {
			return gust;
		}

		public void setGust(double gust) {
			this.gust = gust;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() == o.getClass())
				return false;
			Wind wind = (Wind) o;
			return Double.compare(wind.speed, speed) == 0 && deg == wind.deg && Double.compare(wind.gust, gust) == 0;
		}

		@Override
		public int hashCode() {
			return Objects.hash(speed, deg, gust);
		}

		@Override
		public String toString() {
			return "Wind{" + "speed=" + speed + ", deg=" + deg + ", gust=" + gust + '}';
		}
	}
}
