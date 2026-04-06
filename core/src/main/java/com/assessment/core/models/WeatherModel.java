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

    private String temperature;
    private String description;


    @PostConstruct
    protected void init() throws Exception {
        String requestedCity = getCity();
        try{
            String jsonResponse = weatherService.getForecast(requestedCity, currentResource);
            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                this.temperature = jsonObject.has("temperature") ? jsonObject.get("temperature").getAsString() : "N/A";
                this.description = jsonObject.has("description") ? jsonObject.get("description").getAsString() : "N/A";
            }
        } catch (Exception e) {
            LOG.error("Error {}", requestedCity, e);
        }
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
