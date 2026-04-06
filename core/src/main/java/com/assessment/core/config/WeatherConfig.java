package com.assessment.core.config;

import org.apache.sling.caconfig.annotation.Configuration;
import org.apache.sling.caconfig.annotation.Property;

@Configuration(label = "Assessment Weather Configuration")
public @interface WeatherConfig {

    @Property(label = "API Key")
    String apiKey() default "legacy-weather-api-key-12345";

    @Property(label = "API Endpoint")
    String endpoint() default "https://goweather.xyz/weather/%s?apikey=%s";

    @Property(label = "Cache TTL (Seconds)")
    int cacheTtl() default 3600;
}