package com.assessment.core.services;
import org.apache.sling.api.resource.Resource;

public interface WeatherService {

    String getForecast(String city, Resource resource);
}

