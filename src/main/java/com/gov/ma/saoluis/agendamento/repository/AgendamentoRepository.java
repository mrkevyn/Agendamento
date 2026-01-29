package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.DTO.AgendamentoDTO;
import com.gov.ma.saoluis.agendamento.DTO.UltimaChamadaDTO;
import com.gov.ma.saoluis.agendamento.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
            s.nome             AS servicoNome,

            sec.id             AS secretariaId,
            sec.nome           AS secretariaNome

        FROM agendamento a
        LEFT JOIN usuario u ON a.usuario_id = u.id
        LEFT JOIN servico s ON a.servico_id = s.id
        LEFT JOIN secretaria sec ON s.secretaria_id = sec.id
        WHERE a.id = :agendamentoId
        """, nativeQuery = true)
	List<AgendamentoDTO> buscarAgendamentosComDetalhes(@Param("agendamentoId") Long agendamentoId);

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
            s.nome             AS servicoNome,

            sec.id             AS secretariaId,
            sec.nome           AS secretariaNome

        FROM agendamento a
        LEFT JOIN usuario u ON a.usuario_id = u.id
        LEFT JOIN servico s ON a.servico_id = s.id
        LEFT JOIN secretaria sec ON s.secretaria_id = sec.id
        """, nativeQuery = true)
	List<AgendamentoDTO> buscarTodosAgendamentosComDetalhes();

	@Query(value = """
        SELECT
            a.id               AS agendamentoId,
            a.hora_agendamento AS horaAgendamento,
            a.situacao         AS situacao,
            a.senha            AS senha,
            a.tipo_atendimento AS tipoAtendimento,

            u.id               AS usuarioId,
            COALESCE(u.nome, a.nome_cidadao) AS usuarioNome,

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

        u.id               AS usuarioId,
        u.nome             AS usuarioNome,

        s.id               AS servicoId,
        s.nome             AS servicoNome,

        g.guiche           AS guiche

    FROM agendamento a
    LEFT JOIN usuario u     ON a.usuario_id = u.id
    LEFT JOIN servico s     ON a.servico_id = s.id
    LEFT JOIN gerenciador g ON a.gerenciador_id = g.id

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
      AND a.senha = :senha
      AND a.situacao IN ('AGENDADO', 'REAGENDADO')
    ORDER BY a.horaAgendamento ASC
""")
	List<Agendamento> buscarPorSenha(
			@Param("senha") String senha,
			Pageable pageable
	);

	@Query(value = """
    select senha
    from agendamento
    where configuracao_atendimento_id = :configId
      and tipo_atendimento = :tipo
      and date(hora_agendamento) = :data
    order by id desc
    limit 1
""", nativeQuery = true)
	String findUltimaSenhaDoDia(@Param("configId") Long configId,
								@Param("tipo") String tipo,
								@Param("data") LocalDate data);


	@Query("""
        select a.senha
        from Agendamento a
        where a.servico.secretaria.id = :secretariaId
          and upper(a.tipoAtendimento) = upper(:tipo)
          and a.horaAgendamento >= :inicio
          and a.horaAgendamento < :fim
        order by a.id desc
    """)
	List<String> findUltimaSenhaDoDiaParaEspontaneo(
			@Param("secretariaId") Long secretariaId,
			@Param("tipo") String tipo,
			@Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim,
			Pageable pageable
	);

	@Query("""
    select a
    from Agendamento a
    where a.senha = :senha
      and a.servico.secretaria.id = :secretariaId
    order by a.id desc
""")
	List<Agendamento> buscarPorSenhaESecetaria(
			@Param("secretariaId") Long secretariaId,
			@Param("senha") String senha,
			Pageable pageable
	);

}
