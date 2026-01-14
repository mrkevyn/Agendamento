package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.DTO.UltimaChamadaDTO;
import com.gov.ma.saoluis.agendamento.model.ChamadaAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChamadaAgendamentoRepository
        extends JpaRepository<ChamadaAgendamento, Long> {

    @Query(value = """
        SELECT
            ca.agendamento_id   AS agendamentoId,
            ca.senha            AS senha,
            ca.tipo_atendimento AS tipoAtendimento,
            ca.data_chamada     AS horaChamada,

            u.id                AS usuarioId,
            u.nome              AS usuarioNome,

            s.id                AS servicoId,
            s.nome              AS servicoNome,

            g.guiche            AS guiche

        FROM chamada_agendamento ca
        JOIN agendamento a   ON ca.agendamento_id = a.id
        LEFT JOIN usuario u  ON a.usuario_id = u.id
        LEFT JOIN servico s  ON a.servico_id = s.id
        LEFT JOIN gerenciador g ON ca.gerenciador_id = g.id

        ORDER BY ca.data_chamada DESC
        LIMIT 1
    """, nativeQuery = true)
    UltimaChamadaDTO buscarUltimaChamada();
}
