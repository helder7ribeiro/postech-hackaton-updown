package org.fiap.updown.infrastructure.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter jwtFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtFilter);
        registration.addUrlPatterns("/api/v1/jobs/*", "/api/v1/jobs");
        registration.setName("jwtAuthenticationFilter");
        registration.setOrder(1);
        return registration;
    }
}
