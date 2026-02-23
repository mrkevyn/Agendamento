package com.gov.ma.saoluis.agendamento.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // No Linux, caminhos absolutos evitam confusão de localização
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/var/www/agendamento/uploads/");
    }
}
