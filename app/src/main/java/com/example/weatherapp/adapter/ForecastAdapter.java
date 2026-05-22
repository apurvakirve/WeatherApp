package com.example.weatherapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.R;
import com.example.weatherapp.model.ForecastResponse.DailyForecast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {
    private List<DailyForecast> forecasts;
    private Context context;

    public ForecastAdapter(Context context) {
        this.context = context;
    }

    public void setForecasts(List<DailyForecast> forecasts) {
        this.forecasts = forecasts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        DailyForecast forecast = forecasts.get(position);

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
        String date = sdf.format(new Date(forecast.getDate() * 1000));
        holder.dayDate.setText(date);

        // Set weather description
        if (!forecast.getWeather().isEmpty()) {
            String description = forecast.getWeather().get(0).getDescription();
            holder.weatherDescription.setText(description.substring(0, 1).toUpperCase() + description.substring(1));

            // Load weather icon
            String iconCode = forecast.getWeather().get(0).getIcon();
            String iconUrl = "https://openweathermap.org/img/w/" + iconCode + ".png";
            Glide.with(context)
                    .load(iconUrl)
                    .into(holder.weatherIcon);
        }

        // Set temperatures
        holder.maxTemp.setText(String.format(Locale.getDefault(), "%.0f°", forecast.getMain().getTempMax()));
        holder.minTemp.setText(String.format(Locale.getDefault(), "%.0f°", forecast.getMain().getTempMin()));
    }

    @Override
    public int getItemCount() {
        return forecasts != null ? forecasts.size() : 0;
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView dayDate, weatherDescription, maxTemp, minTemp;
        ImageView weatherIcon;

        ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            dayDate = itemView.findViewById(R.id.dayDate);
            weatherDescription = itemView.findViewById(R.id.weatherDescription);
            maxTemp = itemView.findViewById(R.id.maxTemp);
            minTemp = itemView.findViewById(R.id.minTemp);
            weatherIcon = itemView.findViewById(R.id.weatherIcon);
        }
    }
}