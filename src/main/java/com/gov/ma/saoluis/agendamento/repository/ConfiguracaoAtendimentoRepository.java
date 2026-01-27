package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.ConfiguracaoAtendimento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    @Query("""
    select a.senha
    from Agendamento a
    where a.configuracao.secretaria.id = :secretariaId
      and upper(a.tipoAtendimento) = upper(:tipo)
      and date(a.horaAgendamento) = :data
    order by a.id desc
""")
    List<String> findUltimaSenhaDoDia(
            @Param("secretariaId") Long secretariaId,
            @Param("tipo") String tipo,
            @Param("data") LocalDate data,
            Pageable pageable
    );
}


