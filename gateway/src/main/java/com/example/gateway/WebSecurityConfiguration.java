package com.example.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
class WebSecurityConfiguration {

    @Bean
    public SecurityWebFilterChain oauthFilterChain(ServerHttpSecurity http) throws Exception {
      return http
        .csrf(csrf -> csrf.disable())
        .authorizeExchange((exchanges) -> exchanges
          .anyExchange().permitAll()
        ).build();
    }
}