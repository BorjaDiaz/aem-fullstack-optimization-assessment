package com.assessment.core.models;

import com.assessment.core.services.WeatherService;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class WeatherModelTest {

    @InjectMocks
    private WeatherModel weatherModel;

    @Mock
    private WeatherService weatherService;

    @Mock
    private Page currentPage;

    @Mock
    private Resource currentResource;

    @BeforeEach
    void setUp() throws Exception {
        // Simulamos la ciudad inyectada para el test
        Field cityField = WeatherModel.class.getDeclaredField("city");
        cityField.setAccessible(true);
        cityField.set(weatherModel, "Madrid");
    }

    @Test
    void testInitSuccess() {
        // Simulamos que el servicio devuelve un JSON válido
        String mockJsonResponse = "{\"temperature\":\"22°C\", \"description\":\"Sunny\"}";
        lenient().when(weatherService.getForecast(anyString(), any())).thenReturn(mockJsonResponse);

        // Ejecutamos el método init()
        weatherModel.init();

        // Comprobamos que el modelo parseó bien el JSON
        assertEquals("22°C", weatherModel.getTemperature());
        assertEquals("Sunny", weatherModel.getDescription());
        assertEquals("Madrid", weatherModel.getCity());
    }

    @Test
    void testInitWithEmptyResponse() {
        // Simulamos que el servicio falla y devuelve null
        lenient().when(weatherService.getForecast(anyString(), any())).thenReturn(null);

        // Ejecutamos el método init()
        weatherModel.init();

        // Como el json es nulo, las variables no se rellenan y quedan como null en Java
        assertNull(weatherModel.getTemperature());
        assertNull(weatherModel.getDescription());
    }
}