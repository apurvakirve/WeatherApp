package com.example.weatherapp.api;

import com.example.weatherapp.model.WeatherResponse;
import com.example.weatherapp.model.ForecastResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("units") String units,
            @Query("appid") String apiKey
    );

    @GET("weather")
    Call<WeatherResponse> getCurrentWeatherByCity(
            @Query("q") String cityName,
            @Query("units") String units,
            @Query("appid") String apiKey
    );

    @GET("forecast")
    Call<ForecastResponse> getForecast(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("units") String units,
            @Query("appid") String apiKey
    );

    @GET("forecast")
    Call<ForecastResponse> getForecastByCity(
            @Query("q") String cityName,
            @Query("units") String units,
            @Query("appid") String apiKey
    );
}