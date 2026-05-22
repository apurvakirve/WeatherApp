package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {
    @SerializedName("list")
    private List<DailyForecast> list;

    public List<DailyForecast> getList() { return list; }

    public static class DailyForecast {
        @SerializedName("dt")
        private long date;

        @SerializedName("main")
        private WeatherResponse.Main main;

        @SerializedName("weather")
        private List<WeatherResponse.Weather> weather;

        @SerializedName("wind")
        private WeatherResponse.Wind wind;

        public long getDate() { return date; }
        public WeatherResponse.Main getMain() { return main; }
        public List<WeatherResponse.Weather> getWeather() { return weather; }
        public WeatherResponse.Wind getWind() { return wind; }
    }
}