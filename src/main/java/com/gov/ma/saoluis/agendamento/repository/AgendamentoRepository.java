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
import java.util.Optional;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

	@Query(value = "select * from agendamento where id = :id", nativeQuery = true)
	Optional<Agendamento> findByIdNativo(@Param("id") Long id);

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
        a.tipo_agendamento AS tipoAgendamento,

        a.gerenciador_id   AS gerenciadorId,
        g.guiche           AS guiche,

        u.id               AS usuarioId,
        COALESCE(u.nome, a.nome_cidadao) AS usuarioNome,

        s.id               AS servicoId,
        s.nome             AS servicoNome,

        a.endereco_id      AS enderecoId,
        e.logradouro       AS enderecoLogradouro,
        e.numero           AS enderecoNumero,
        e.bairro           AS enderecoBairro,
        e.cep              AS enderecoCep,
        e.complemento      AS enderecoComplemento,
        e.cidade           AS enderecoCidade,
        e.uf               AS enderecoUf,

        a.secretaria_id    AS secretariaId,
        sec.nome           AS secretariaNome

    FROM agendamento a
    LEFT JOIN usuario u      ON a.usuario_id = u.id
    LEFT JOIN servico s      ON a.servico_id = s.id
    LEFT JOIN gerenciador g  ON g.id = a.gerenciador_id
    LEFT JOIN endereco e     ON e.id = a.endereco_id
    LEFT JOIN secretaria sec ON sec.id = a.secretaria_id

    WHERE a.endereco_id = :enderecoId
      AND a.hora_agendamento >= CURRENT_DATE
      AND a.hora_agendamento < CURRENT_DATE + INTERVAL '1 day'
    ORDER BY a.hora_agendamento ASC
""", nativeQuery = true)
	List<AgendamentoDTO> buscarAgendamentosPorEndereco(@Param("enderecoId") Long enderecoId);

	long countByTipoAtendimento(String tipoAtendimento);

	@Query("SELECT COUNT(a) FROM Agendamento a " +
		       "WHERE a.tipoAtendimento = :tipoAtendimento " +
		       "AND FUNCTION('DATE', a.horaAgendamento) = :data")
		long countByTipoAtendimentoAndData(@Param("tipoAtendimento") String tipoAtendimento,
		                                   @Param("data") LocalDate data);

	@Query("""
    SELECT a
    FROM Agendamento a
    WHERE a.endereco.id = :enderecoId
      AND a.tipoAtendimento = 'NORMAL'
      AND a.situacao IN ('AGENDADO', 'REAGENDADO', 'EM_ATENDIMENTO')
      AND a.horaAgendamento >= :inicio
      AND a.horaAgendamento < :fim
    ORDER BY a.horaAgendamento ASC
""")
	List<Agendamento> buscarProximoNormalHoje(
			@Param("enderecoId") Long enderecoId,
			@Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim,
			Pageable pageable
	);

	@Query("""
    SELECT a
    FROM Agendamento a
    WHERE a.endereco.id = :enderecoId
      AND a.tipoAtendimento = 'PRIORIDADE'
      AND a.situacao IN ('AGENDADO', 'REAGENDADO', 'EM_ATENDIMENTO')
      AND a.horaAgendamento >= :inicio
      AND a.horaAgendamento < :fim
    ORDER BY a.horaAgendamento ASC
""")
	List<Agendamento> buscarProximoPrioridadeHoje(
			@Param("enderecoId") Long enderecoId,
			@Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim,
			Pageable pageable
	);

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
    WHERE a.endereco.id = :enderecoId
      AND a.senha = :senha
      AND a.situacao IN ('AGENDADO', 'REAGENDADO', 'EM_ATENDIMENTO')
      AND a.horaAgendamento >= :inicio
      AND a.horaAgendamento < :fim
    ORDER BY a.horaAgendamento ASC
""")
	List<Agendamento> buscarPorSenhaHoje(
			@Param("enderecoId") Long enderecoId,
			@Param("senha") String senha,
			@Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim,
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
    select a.senha
    from Agendamento a
    where a.endereco.id = :enderecoId
      and upper(a.tipoAtendimento) = upper(:tipo)
      and a.horaAgendamento >= :inicio
      and a.horaAgendamento < :fim
    order by a.id desc
""")
	List<String> findUltimaSenhaDoDiaParaEspontaneoPorEndereco(
			@Param("enderecoId") Long enderecoId,
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
