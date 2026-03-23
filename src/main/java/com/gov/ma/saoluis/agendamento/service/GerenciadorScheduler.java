package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.utils.DataUtils;
import com.gov.ma.saoluis.agendamento.repository.GerenciadorRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class GerenciadorScheduler {

    private final GerenciadorRepository gerenciadorRepository;

    public GerenciadorScheduler(GerenciadorRepository gerenciadorRepository) {
        this.gerenciadorRepository = gerenciadorRepository;
    }

    // 🟢 'zone' garante que o Spring olhe para o relógio de São Luís, não do servidor
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = DataUtils.STR_ZONE)
    public void resetarGuichesDiarios() {
        gerenciadorRepository.limparTodosOsPontosAtendimento();

        // Exemplo de uso da constante ZONE_SLZ no log
        System.out.println("Sistema: Todos os guichês liberados em: " + LocalDateTime.now(DataUtils.ZONE_SLZ));
    }
}