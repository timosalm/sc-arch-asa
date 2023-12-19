package com.example.orderservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServletBearerExchangeFilterFunction;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class WebSecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(WebSecurityConfiguration.class);

    @Profile("!oauth")
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((authz) -> authz.anyRequest().permitAll())
                .build();
    }

    @Profile("oauth")
    @Bean
    public SecurityFilterChain oauthFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests((authz) -> authz
            .requestMatchers("/api/**").authenticated()
            .anyRequest().permitAll()
        )
        .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
        .build();
    }

    @LoadBalanced
    @Profile("oauth")
    @Primary
    @Bean
    public WebClient.Builder oauthWebClientBuilder() {
        return WebClient.builder().filter(new ServletBearerExchangeFilterFunction());
    }
}


