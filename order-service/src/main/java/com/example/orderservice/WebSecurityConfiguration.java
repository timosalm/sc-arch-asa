package com.example.orderservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.config.Customizer;

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
        .oauth2ResourceServer(Customizer.withDefaults())
        .build();
    }

    @Profile("oauth")
    @Primary
    @Bean
    RestTemplate oauthRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.additionalInterceptors((request, body, execution) -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                var token = (AbstractOAuth2Token) authentication.getCredentials();
                request.getHeaders().setBearerAuth(token.getTokenValue());
                log.info("Adding authentication header to outgoing request");
            }
            return execution.execute(request, body);
        }).build();
    }
}