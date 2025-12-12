package com.gov.ma.saoluis.agendamento.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Agendamentos - Prefeitura de São Luís")
                        .description("Documentação automática dos endpoints de agendamento")
                        .version("1.0.0"));
    }
}
