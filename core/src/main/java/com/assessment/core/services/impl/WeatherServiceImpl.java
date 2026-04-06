package com.assessment.core.services.impl;

import com.assessment.core.config.WeatherConfig;
import com.assessment.core.services.WeatherService;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = WeatherService.class, immediate = true)
public class WeatherServiceImpl implements WeatherService {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(WeatherServiceImpl.class);

    @Override
    public String getForecast(String city, Resource resource) {
        if (resource == null){
            return null;
        }
        WeatherConfig config = resource.adaptTo(ConfigurationBuilder.class).as(WeatherConfig.class);

        if (cache.containsKey(city)) {
            return cache.get(city);
        }

        try {
            String urlString = String.format(config.endpoint(),
                    URLEncoder.encode(city, StandardCharsets.UTF_8), config.apiKey());

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (conn.getResponseCode() == 200) {
                String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                cache.put(city, response);
                return response;
            }
        } catch (Exception e) {
            LOG.error("Error {}", city, e);

        }

        return null;
    }
}
