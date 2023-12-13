package com.example.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
class WebSecurityConfiguration {

    @Profile("!oauth")
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
      return http
        .csrf(csrf -> csrf.disable())
        .authorizeExchange((exchanges) -> exchanges
          .anyExchange().permitAll()
        ).build();
    }

    @Profile("oauth")
    @Bean
    public SecurityWebFilterChain oauthFilterChain(ServerHttpSecurity http) throws Exception {
        return http
        .authorizeExchange((exchanges) -> exchanges
          .pathMatchers("/services/**").authenticated()
          .pathMatchers("/**").permitAll()
        )
        .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
        .build();
    }
}