package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    @SerializedName("name")
    private String cityName;

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private List<Weather> weather;

    @SerializedName("wind")
    private Wind wind;

    public String getCityName() { return cityName; }
    public Main getMain() { return main; }
    public List<Weather> getWeather() { return weather; }
    public Wind getWind() { return wind; }

    public static class Main {
        @SerializedName("temp")
        private double temperature;

        @SerializedName("humidity")
        private int humidity;

        @SerializedName("temp_min")
        private double tempMin;

        @SerializedName("temp_max")
        private double tempMax;

        public double getTemperature() { return temperature; }
        public int getHumidity() { return humidity; }
        public double getTempMin() { return tempMin; }
        public double getTempMax() { return tempMax; }
    }

    public static class Weather {
        @SerializedName("description")
        private String description;

        @SerializedName("icon")
        private String icon;

        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }

    public static class Wind {
        @SerializedName("speed")
        private double speed;

        @SerializedName("deg")
        private int degree;

        public double getSpeed() { return speed; }
        public int getDegree() { return degree; }
    }
}