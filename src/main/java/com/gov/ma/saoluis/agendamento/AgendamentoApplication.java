package com.gov.ma.saoluis.agendamento;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgendamentoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgendamentoApplication.class, args);
    }

    @Bean
    CommandLineRunner run() {
        return args -> {

        };
    }
}
