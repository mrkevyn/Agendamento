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

    List<ConfiguracaoAtendimento> findBySetorId(Long setorId);

    List<ConfiguracaoAtendimento> findBySetorIdAndAtivoTrue(Long setorId);

    List<ConfiguracaoAtendimento> findBySetorIdAndAtivo(Long setorId, Boolean ativo);

    @Query("""
        select a.senha
        from Agendamento a
        where a.setor.id = :setorId
          and upper(a.tipoAtendimento) = upper(:tipo)
          and date(a.horaAgendamento) = :data
        order by a.id desc
    """)
    List<String> findUltimaSenhaDoDia(
            Long setorId,
            String tipo,
            LocalDate data,
            Pageable pageable
    );

    @Query("""
       SELECT c
       FROM ConfiguracaoAtendimento c
       WHERE c.setor.id = :setorId
       AND c.ativo = true
       """)
    List<ConfiguracaoAtendimento> findAtivasPorSetor(@Param("setorId") Long setorId);
}
