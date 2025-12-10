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
        WHERE s.secretaria_id = :secretariaId
        """, nativeQuery = true)
	List<AgendamentoDTO> buscarAgendamentosPorSecretaria(@Param("secretariaId") Long secretariaId);


	long countByTipoAtendimento(String tipoAtendimento);

	@Query("SELECT COUNT(a) FROM Agendamento a " +
		       "WHERE a.tipoAtendimento = :tipoAtendimento " +
		       "AND FUNCTION('DATE', a.horaAgendamento) = :data")
		long countByTipoAtendimentoAndData(@Param("tipoAtendimento") String tipoAtendimento,
		                                   @Param("data") LocalDate data);

	@Query("""
    SELECT a
    FROM Agendamento a
    WHERE a.servico.secretaria IS NOT NULL
      AND a.servico.secretaria.id = :secretariaId
      AND a.tipoAtendimento = 'NORMAL'
      AND a.situacao = 'AGENDADO'
    ORDER BY a.horaAgendamento ASC
""")
	List<Agendamento> buscarProximoNormal(@Param("secretariaId") Long secretariaId, org.springframework.data.domain.Pageable pageable);

	@Query("""
    SELECT a
    FROM Agendamento a
    WHERE a.servico.secretaria IS NOT NULL
      AND a.servico.secretaria.id = :secretariaId
      AND a.tipoAtendimento = 'PRIORIDADE'
      AND a.situacao = 'AGENDADO'
    ORDER BY a.horaAgendamento ASC
""")
	List<Agendamento> buscarProximoPrioridade(@Param("secretariaId") Long secretariaId, org.springframework.data.domain.Pageable pageable);

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

	@Query("""
    SELECT COUNT(a) 
    FROM Agendamento a
    WHERE a.servico.secretaria.id = :secretariaId
      AND a.tipoAtendimento = :tipo
      AND DATE(a.horaAgendamento) = :data
""")
	long countBySecretariaAndTipoAndData(Integer secretariaId, String tipo, LocalDate data);

	@Query("""
    SELECT s.secretaria.id
    FROM Servico s
    WHERE s.id = :servicoId
""")
	Long findSecretariaIdByServicoId(@Param("servicoId") Long servicoId);

	@Query("""
    SELECT a
    FROM Agendamento a
    WHERE a.servico.secretaria IS NOT NULL
      AND a.situacao = 'AGENDADO'
      AND a.senha = :senha
    ORDER BY a.horaAgendamento ASC
""")
	List<Agendamento> buscarPorSenha(@Param("senha") String senha, org.springframework.data.domain.Pageable pageable);
}
