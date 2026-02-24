package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.repository.GerenciadorRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GerenciadorScheduler {

    private final GerenciadorRepository gerenciadorRepository;

    public GerenciadorScheduler(GerenciadorRepository gerenciadorRepository) {
        this.gerenciadorRepository = gerenciadorRepository;
    }

    // Roda todos os dias às 00:00:00
    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void resetarGuichesDiarios() {
        // Esta query vai setar null em todos os guichês de todos os gerenciadores
        gerenciadorRepository.limparTodosOsGuiches();
        System.out.println("Sistema: Todos os guichês foram liberados para o novo dia.");
    }
}