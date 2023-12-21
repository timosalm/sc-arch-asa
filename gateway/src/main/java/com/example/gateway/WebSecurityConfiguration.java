package com.example.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
class WebSecurityConfiguration {

    @Profile("!oauth")
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
      return http
              .csrf(csrf -> csrf.disable())
              .authorizeExchange((exchanges) -> exchanges
                .anyExchange().permitAll()
              ).build();
    }

    @Profile("oauth")
    @Bean
    public SecurityWebFilterChain oauthFilterChain(ServerHttpSecurity http) {
      return http
              .csrf(csrf -> csrf.disable())
              .authorizeExchange((exchanges) -> exchanges
                .pathMatchers("/**").authenticated()
              )
              .oauth2Login(Customizer.withDefaults())
              .oauth2Client(Customizer.withDefaults())
              .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
              .build();
    }

    @Profile("oauth")
    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.client.provider.sso.issuer-uri}") String issuerUri) {
        return ReactiveJwtDecoders.fromOidcIssuerLocation(issuerUri);
    }

}