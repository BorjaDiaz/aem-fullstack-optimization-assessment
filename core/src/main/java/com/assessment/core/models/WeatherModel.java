package com.assessment.core.models;

import com.assessment.core.services.WeatherService;
import com.day.cq.wcm.api.Page;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

@Model(
        adaptables = SlingHttpServletRequest.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class WeatherModel {

    @Inject
    private String city;

    @Inject
    private Page currentPage;

    @Inject
    private WeatherService weatherService;

    private String weatherJson;

    @PostConstruct
    protected void init() throws Exception {
        String requestedCity = city != null ? city : "Bogota";
        URL url = new URL(
                "https://goweather.xyz/weather/"
                        + URLEncoder.encode(requestedCity, StandardCharsets.UTF_8)
                        + "?apikey=model-level-hardcoded-key");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        weatherJson = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public String getCity() {
        return city != null ? city : "Bogota";
    }

    public String getWeatherJson() {
        return weatherJson;
    }

    public String getPageTitle() {
        return currentPage != null ? currentPage.getTitle() : "Weather Page";
    }

    public WeatherService getWeatherService() {
        return weatherService;
    }
}
