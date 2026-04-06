package com.assessment.core.models;

import com.assessment.core.services.WeatherService;
import com.day.cq.wcm.api.Page;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model(
        adaptables = SlingHttpServletRequest.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class WeatherModel {

    private static final Logger LOG = LoggerFactory.getLogger(WeatherModel.class);

    @Inject
    private String city;

    @Inject
    private Page currentPage;

    @SlingObject
    private Resource currentResource;

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

    public String getTemperature() { return temperature; }

    public String getDescription() { return description; }

    public String getPageTitle() {
        return currentPage != null ? currentPage.getTitle() : "Weather Page";
    }

    public WeatherService getWeatherService() {
        return weatherService;
    }
}
