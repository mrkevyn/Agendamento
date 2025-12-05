package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.DTO.AgendamentoDTO;
import com.gov.ma.saoluis.agendamento.DTO.UltimaChamadaDTO;
import com.gov.ma.saoluis.agendamento.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

	@Query(value = """
        SELECT
            a.id               AS agendamentoId,
            a.hora_agendamento AS horaAgendamento,
            a.situacao         AS situacao,
            a.senha            AS senha,
            a.tipo_atendimento AS tipoAtendimento,

            u.id               AS usuarioId,
            u.nome             AS usuarioNome,

            s.id               AS servicoId,
            s.nome             AS servicoNome

        FROM agendamento a
        LEFT JOIN usuario u ON a.usuario_id = u.id
        LEFT JOIN servico s  ON a.servico_id = s.id
        """, nativeQuery = true)
	List<AgendamentoDTO> buscarAgendamentosComDetalhes();

	long countByTipoAtendimento(String tipoAtendimento);

	@Query("SELECT COUNT(a) FROM Agendamento a " +
		       "WHERE a.tipoAtendimento = :tipoAtendimento " +
		       "AND FUNCTION('DATE', a.horaAgendamento) = :data")
		long countByTipoAtendimentoAndData(@Param("tipoAtendimento") String tipoAtendimento,
		                                   @Param("data") LocalDate data);

	@Query(value = """
		    SELECT * FROM agendamento 
		    WHERE situacao = 'AGENDADO'
		      AND tipo_atendimento = 'NORMAL'
		      AND DATE(hora_agendamento) = CURRENT_DATE
		    ORDER BY hora_agendamento ASC 
		    LIMIT 1
		""", nativeQuery = true)
		Agendamento buscarProximoNormal();

		@Query(value = """
		    SELECT * FROM agendamento 
		    WHERE situacao = 'AGENDADO'
		      AND tipo_atendimento = 'PRIORITARIO'
		      AND DATE(hora_agendamento) = CURRENT_DATE
		    ORDER BY hora_agendamento ASC 
		    LIMIT 1
		""", nativeQuery = true)
		Agendamento buscarProximoPrioridade();

	@Query(value = """
    SELECT
        a.id               AS agendamentoId,
        a.senha            AS senha,
        a.tipo_atendimento AS tipoAtendimento,
        a.hora_chamada     AS horaChamada,

        u.id                AS usuarioId,
        u.nome              AS usuarioNome,

        s.id                AS servicoId,
        s.nome              AS servicoNome

    FROM agendamento a
    LEFT JOIN usuario u ON a.usuario_id = u.id
    LEFT JOIN servico s  ON a.servico_id = s.id

    WHERE a.hora_chamada IS NOT NULL
    ORDER BY a.hora_chamada DESC
    LIMIT 1
""", nativeQuery = true)
	UltimaChamadaDTO buscarUltimaChamada();
}
