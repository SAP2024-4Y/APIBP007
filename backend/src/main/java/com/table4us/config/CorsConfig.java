package com.table4us.config;
import org.springframework.context.annotation.*; import org.springframework.web.servlet.config.annotation.*;
@Configuration public class CorsConfig implements WebMvcConfigurer {
 public void addCorsMappings(CorsRegistry registry){registry.addMapping("/**").allowedOrigins("http://localhost:5173").allowedMethods("GET","POST","PUT","DELETE","OPTIONS").allowedHeaders("*");}
}
