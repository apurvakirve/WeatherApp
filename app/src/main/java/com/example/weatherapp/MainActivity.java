package com.example.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.weatherapp.adapter.ForecastAdapter;
import com.example.weatherapp.api.WeatherApi;
import com.example.weatherapp.model.ForecastResponse;
import com.example.weatherapp.model.WeatherResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String API_KEY = "7985e46a4685ce8018393671f582501f"; // Replace with your OpenWeatherMap API key
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private WeatherApi weatherApi;
    private ForecastAdapter forecastAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Geocoder geocoder;

    private TextInputEditText citySearchInput;
    private MaterialButton searchButton;
    private MaterialButton locationButton;
    private TextView cityName, temperature, weatherDescription, humidity, windSpeed, windDirection;
    private ImageView weatherIcon;
    private RecyclerView forecastRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupRetrofit();
        setupLocationClient();
        setupSwipeRefresh();
        setupRecyclerView();
        setupSearchFunctionality();
        setupLocationButton();

        geocoder = new Geocoder(this, Locale.getDefault());
        setupLocationUpdates();
        checkLocationPermission();
    }

    private void initializeViews() {
        citySearchInput = findViewById(R.id.citySearchInput);
        searchButton = findViewById(R.id.searchButton);
     locationButton = findViewById(R.id.locationButton);
        cityName = findViewById(R.id.cityName);
        temperature = findViewById(R.id.temperature);
        weatherDescription = findViewById(R.id.weatherDescription);
        humidity = findViewById(R.id.humidity);
        windSpeed = findViewById(R.id.windSpeed);
        windDirection = findViewById(R.id.windDirection);
        weatherIcon = findViewById(R.id.weatherIcon);
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
    }

    private void setupSearchFunctionality() {
        searchButton.setOnClickListener(v -> performSearch());

        citySearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String city = citySearchInput.getText().toString().trim();
        if (!city.isEmpty()) {
            fetchWeatherByCity(city);
            fetchForecastByCity(city);
        }
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApi = retrofit.create(WeatherApi.class);
    }

    private void setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            String city = citySearchInput.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeatherByCity(city);
                fetchForecastByCity(city);
            } else {
                checkLocationPermission();
            }
        });
    }

    private void setupRecyclerView() {
        forecastAdapter = new ForecastAdapter(this);
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        forecastRecyclerView.setAdapter(forecastAdapter);
    }

    private void setupLocationButton() {
        locationButton.setOnClickListener(v -> checkLocationPermission());
    }

    private String getWindDirection(int degrees) {
        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int) Math.round(((degrees % 360) / 22.5));
        return directions[index % 16];
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000)
                .setMinUpdateDistanceMeters(1000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocationInfo(location);
                    break;
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateLocationInfo(Location location) {
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String cityNameStr = address.getLocality();
                if (cityNameStr != null) {
                    citySearchInput.setText(cityNameStr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        fetchWeatherData(location.getLatitude(), location.getLongitude());
        fetchForecastData(location.getLatitude(), location.getLongitude());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            updateLocationInfo(location);
                        } else {
                            setupLocationUpdates(); // Request location updates if last location is null
                        }
                    });
        }
    }

    private void fetchWeatherByCity(String city) {
        weatherApi.getCurrentWeatherByCity(city, "metric", API_KEY)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateCurrentWeather(response.body());
                        } else {
                            Toast.makeText(MainActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Error fetching weather", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void fetchForecastByCity(String city) {
        weatherApi.getForecastByCity(city, "metric", API_KEY)
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateForecast(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<ForecastResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Error fetching forecast", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchWeatherData(double lat, double lon) {
        weatherApi.getCurrentWeather(lat, lon, "metric", API_KEY)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateCurrentWeather(response.body());
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Error fetching weather", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void fetchForecastData(double lat, double lon) {
        weatherApi.getForecast(lat, lon, "metric", API_KEY)
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateForecast(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<ForecastResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Error fetching forecast", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCurrentWeather(WeatherResponse weather) {
        cityName.setText(weather.getCityName());
        temperature.setText(String.format(Locale.getDefault(), "%.0f°", weather.getMain().getTemperature()));

        if (!weather.getWeather().isEmpty()) {
            String description = weather.getWeather().get(0).getDescription();
            weatherDescription.setText(description.substring(0, 1).toUpperCase() + description.substring(1));

            String iconCode = weather.getWeather().get(0).getIcon();
            String iconUrl = "https://openweathermap.org/img/w/" + iconCode + ".png";
            Glide.with(this)
                    .load(iconUrl)
                    .into(weatherIcon);
        }

        humidity.setText(String.format(Locale.getDefault(), "%d%%", weather.getMain().getHumidity()));
        windSpeed.setText(String.format(Locale.getDefault(), "%.1f m/s", weather.getWind().getSpeed()));

        String direction = getWindDirection(weather.getWind().getDegree());
        windDirection.setText(String.format("Direction: %s", direction));
    }

    private void updateForecast(ForecastResponse forecast) {
        List<ForecastResponse.DailyForecast> dailyForecasts = new ArrayList<>();
        String currentDate = "";

        for (ForecastResponse.DailyForecast f : forecast.getList()) {
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(f.getDate() * 1000));
            if (!date.equals(currentDate)) {
                dailyForecasts.add(f);
                currentDate = date;
                if (dailyForecasts.size() >= 3) break;
            }
        }

        forecastAdapter.setForecasts(dailyForecasts);
    }
}