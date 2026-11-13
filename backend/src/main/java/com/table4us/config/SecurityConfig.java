package com.table4us.config;
import org.springframework.context.annotation.*; import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.config.http.SessionCreationPolicy; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration @EnableMethodSecurity public class SecurityConfig {
 @Bean SecurityFilterChain filter(HttpSecurity http,JwtAuthFilter jwt)throws Exception{return http.csrf(c->c.disable()).cors(c->{}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a->a.requestMatchers("/api/v1/reservations/**").hasRole("USER").anyRequest().permitAll()).addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();}
}
