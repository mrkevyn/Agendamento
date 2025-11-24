package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.DTO.AgendamentoDTO;
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
		        a.id AS agendamento_id,
		        a.hora_agendamento,
		        a.situacao,
		        a.senha,
		        a.tipo_atendimento,
		        u.id AS usuario_id,
		        u.nome AS usuario_nome,
		        u.data_nascimento AS data_nascimento,
		        s.id AS servico_id,
		        s.nome AS servico_nome
		    FROM agendamento a
		    JOIN usuario u ON a.usuario_id = u.id
		    JOIN servico s ON a.servico_id = s.id
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
		      AND tipo_atendimento = 'PRIORIDADE'
		      AND DATE(hora_agendamento) = CURRENT_DATE
		    ORDER BY hora_agendamento ASC 
		    LIMIT 1
		""", nativeQuery = true)
		Agendamento buscarProximoPrioridade();

		@Query("SELECT a FROM Agendamento a WHERE a.horaChamada IS NOT NULL ORDER BY a.horaChamada DESC LIMIT 1")
		Agendamento findTopByOrderByHoraChamadaDesc();

}
