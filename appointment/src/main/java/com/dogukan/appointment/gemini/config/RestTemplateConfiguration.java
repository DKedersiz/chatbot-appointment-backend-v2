package com.dogukan.appointment.gemini.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        // Pro modelleri düşünme adımı yüzünden yanıt vermeden önce onlarca saniye bekleyebilir.
        factory.setReadTimeout(Duration.ofSeconds(60));
        return new RestTemplate(factory);
    }

}
