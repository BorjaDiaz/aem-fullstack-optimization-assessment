package com.assessment.core.services.impl;

import com.assessment.core.services.WeatherService;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.osgi.service.component.annotations.Component;

@Component(service = WeatherService.class, immediate = true)
public class WeatherServiceImpl implements WeatherService {

    private static final String API_KEY = "legacy-weather-api-key-12345";
    private static final String ENDPOINT = "https://goweather.xyz/weather/%s?apikey=%s";

    @Override
    public String getForecast(String city) throws Exception {
        URL url = new URL(String.format(
                ENDPOINT,
                URLEncoder.encode(city, StandardCharsets.UTF_8),
                API_KEY));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
