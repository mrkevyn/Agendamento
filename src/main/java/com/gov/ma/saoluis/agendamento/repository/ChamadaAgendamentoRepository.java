package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.DTO.UltimaChamadaDTO;
import com.gov.ma.saoluis.agendamento.model.Agendamento;
import com.gov.ma.saoluis.agendamento.model.ChamadaAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChamadaAgendamentoRepository
        extends JpaRepository<ChamadaAgendamento, Long> {

    @Query(value = """
    SELECT
        ca.agendamento_id   AS agendamentoId,
        ca.senha            AS senha,
        ca.tipo_atendimento AS tipoAtendimento,
        ca.data_chamada     AS horaChamada, -- alias para horário

        a.nome_cidadao      AS nomeCidadao,

        u.id                AS usuarioId,
        u.nome              AS usuarioNome,

        s.id                AS servicoId,
        s.nome              AS servicoNome,

        g.guiche            AS guiche

    FROM chamada_agendamento ca
    JOIN agendamento a       ON ca.agendamento_id = a.id
    JOIN servico s           ON a.servico_id = s.id

    LEFT JOIN usuario u      ON a.usuario_id = u.id
    LEFT JOIN gerenciador g  ON ca.gerenciador_id = g.id

    WHERE g.setor_id = :setorId
      AND ca.data_chamada >= :inicio
      AND ca.data_chamada < :fim

    ORDER BY ca.data_chamada DESC
    LIMIT 5
""", nativeQuery = true)
    List<UltimaChamadaDTO> buscarUltimasChamadasPorSetorEHorario(
            @Param("setorId") Long setorId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("""
    SELECT c
    FROM ChamadaAgendamento c
    WHERE c.agendamento = :agendamento
      AND c.dataChamada >= :inicio
      AND c.dataChamada < :fim
""")
    List<ChamadaAgendamento> findByAgendamentoAndDataChamadaBetween(
            @Param("agendamento") Agendamento agendamento,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
