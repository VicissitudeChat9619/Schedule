package com.myschedule.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NapCatConfig {

    @Value("${napcat.api-url}")
    private String napcatApiUrl;

    @Value("${napcat.timeout}")
    private int timeout;

    @Bean
    public RestTemplate napcatRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }

    @Bean
    public String napcatApiUrl() {
        return napcatApiUrl;
    }
}
