package com.gov.ma.saoluis.agendamento.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Agendamentos",
                version = "v1",
                description = "API para configuração e controle de atendimentos"
        )
)
public class OpenApiConfig {
}
