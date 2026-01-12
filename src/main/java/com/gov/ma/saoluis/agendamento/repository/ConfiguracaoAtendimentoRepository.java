package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.ConfiguracaoAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConfiguracaoAtendimentoRepository
        extends JpaRepository<ConfiguracaoAtendimento, Long> {

    List<ConfiguracaoAtendimento> findBySecretariaIdAndAtivoTrue(Long secretariaId);

    @Query("""
        SELECT c
        FROM ConfiguracaoAtendimento c
        WHERE c.secretaria.id = :secretariaId
          AND c.ativo = true
    """)
    List<ConfiguracaoAtendimento> findAtivasPorSecretaria(Long secretariaId);
}
