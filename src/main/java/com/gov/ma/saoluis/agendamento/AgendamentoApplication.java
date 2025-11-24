package com.gov.ma.saoluis.agendamento;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.gov.ma.saoluis.agendamento.model.Servico;
import com.gov.ma.saoluis.agendamento.repository.ServicoRepository;

@SpringBootApplication
public class AgendamentoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgendamentoApplication.class, args);
    }

    @Bean
    CommandLineRunner run(ServicoRepository servicoRepository) {
        return args -> {
            // Faz um SELECT de todos os serviços
            List<Servico> servicos = servicoRepository.findAll();
            
            // Imprime cada serviço
            for (Servico s : servicos) {
                //System.out.println("ID: " + s.getId() + ", Nome: " + s.getNome() + ", Descrição: " + s.getDescricao());
            }
        };
    }
}
