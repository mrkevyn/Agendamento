package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.DTO.AgendamentoDTO;
import com.gov.ma.saoluis.agendamento.DTO.UltimaChamadaDTO;
import com.gov.ma.saoluis.agendamento.model.Agendamento;
import com.gov.ma.saoluis.agendamento.model.SituacaoAgendamento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

	// Adicione isto no seu AgendamentoRepository
	boolean existsByGerenciadorIdAndSituacaoIn(Long gerenciadorId, List<String> situacoes);

	@Query(value = "select * from agendamento where id = :id", nativeQuery = true)
	Optional<Agendamento> findByIdNativo(@Param("id") Long id);

	List<Agendamento> findBySituacaoInAndUltimoPingBefore(List<SituacaoAgendamento> situacoes, LocalDateTime limite);

	@Query(value = """
        SELECT
            a.id               AS agendamentoId,
            a.hora_agendamento AS horaAgendamento,
            a.situacao         AS situacao,
            a.senha            AS senha,
            ta.nome            AS tipoAtendimento,

            u.id               AS usuarioId,
            u.nome             AS usuarioNome,

            s.id               AS servicoId,
            s.nome             AS servicoNome,

            sec.id             AS secretariaId,
            sec.nome           AS secretariaNome

        FROM agendamento a
        LEFT JOIN tipo_atendimento ta ON a.tipo_atendimento_id = ta.id
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
            ta.nome            AS tipoAtendimento,

            u.id               AS usuarioId,
            u.nome             AS usuarioNome,

            s.id               AS servicoId,
            s.nome             AS servicoNome,

            sec.id             AS secretariaId,
            sec.nome           AS secretariaNome

        FROM agendamento a
        LEFT JOIN tipo_atendimento ta ON a.tipo_atendimento_id = ta.id
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
        a.tipo_agendamento AS tipoAgendamento,
        a.observacao       AS observacao,
        
        ta.id              AS tipoAtendimentoId,
        ta.nome            AS tipoAtendimento,
        ta.sigla           AS tipoAtendimentoSigla,
        ta.peso            AS tipoAtendimentoPeso,

        a.gerenciador_id   AS gerenciadorId,
        gui.numero         AS guiche,

        u.id               AS usuarioId,
        COALESCE(u.nome, a.nome_cidadao) AS usuarioNome,

        COALESCE(s.id, ss.id)     AS servicoId,
        COALESCE(s.nome, ss.nome) AS servicoNome,

        setor.id           AS setorId,
        setor.nome         AS setorNome,

        e.id               AS enderecoId,
        e.logradouro       AS enderecoLogradouro,
        e.bairro           AS enderecoBairro,

        a.secretaria_id    AS secretariaId,
        sec.nome           AS secretariaNome

    FROM agendamento a
    LEFT JOIN tipo_atendimento ta ON a.tipo_atendimento_id = ta.id
    LEFT JOIN usuario u           ON a.usuario_id = u.id
    LEFT JOIN servico s           ON a.servico_id = s.id
    LEFT JOIN servico_saude ss    ON a.servico_saude_id = ss.id
    LEFT JOIN gerenciador g       ON g.id = a.gerenciador_id
    LEFT JOIN guiche gui          ON gui.id = g.guiche_id
    INNER JOIN setor setor        ON a.setor_id = setor.id
    INNER JOIN endereco e         ON setor.endereco_id = e.id
    LEFT JOIN secretaria sec      ON sec.id = a.secretaria_id

    WHERE a.setor_id = :setorId
      -- 🟢 NOVA LÓGICA DE PERFIL:
      AND (
          :perfil = 'CADASTRO' -- Se for cadastro, libera tudo do setor
          OR
          (
              -- Lógica para Atendentes comuns (filtra por serviços vinculados)
              EXISTS (
                  SELECT 1 FROM gerenciador_servico sg_inner\s
                  WHERE sg_inner.gerenciador_id = :gerenciadorId\s
                  AND (sg_inner.servico_id = a.servico_id OR sg_inner.servico_id = a.servico_saude_id)
              )
              OR
              (
                  NOT EXISTS (SELECT 1 FROM gerenciador_servico sg_check WHERE sg_check.gerenciador_id = :gerenciadorId)
                  AND NOT EXISTS (SELECT 1 FROM gerenciador_servico sg_global WHERE sg_global.servico_id = a.servico_id OR sg_global.servico_id = a.servico_saude_id)
              )
          )
      )
      -- Mantém o filtro de data/hospital
      AND (
          (:isHospital = true AND a.situacao IN ('AGENDADO', 'CHAMADO', 'EM_ATENDIMENTO', 'REAGENDADO', 'ATENDIDO', 'FALTOU'))
          OR
          (:isHospital = false AND a.hora_agendamento >= CURRENT_DATE AND a.hora_agendamento < CURRENT_DATE + INTERVAL '1 day')
      )

    -- 🟢 GROUP BY garante que não duplique e permite o ORDER BY funcionar
    GROUP BY 
        a.id, ta.id, gui.id, u.id, s.id, ss.id, setor.id, e.id, sec.id

    ORDER BY 
        CASE 
            WHEN a.situacao IN ('EM_ATENDIMENTO', 'CHAMADO') THEN 1
            WHEN :isHospital = false AND (a.situacao IN ('AGENDADO', 'REAGENDADO')) 
                 AND (a.tipo_agendamento = 'ESPONTANEO' OR a.hora_agendamento <= :agora) THEN 2
            WHEN :isHospital = true AND a.situacao IN ('AGENDADO', 'REAGENDADO') THEN 2
            ELSE 4
        END ASC,
        COALESCE(ta.peso, 0) ASC,
        a.hora_agendamento ASC
""", nativeQuery = true)
	List<AgendamentoDTO> buscarAgendamentosPorSetor(
			@Param("setorId") Long setorId,
			@Param("agora") java.sql.Timestamp agora,
			@Param("isHospital") boolean isHospital,
			@Param("gerenciadorId") Long gerenciadorId,
			@Param("perfil") String perfil
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
      SELECT a
      FROM Agendamento a
      WHERE a.setor.id = :setorId
        AND a.tipoAtendimento.peso = 0 
        AND a.situacao IN ('AGENDADO', 'REAGENDADO')
        AND (
            (a.tipoAgendamento = 'AGENDADO' AND a.horaAgendamento >= :inicio AND a.horaAgendamento <= :agora)
            OR 
            (a.tipoAgendamento = 'ESPONTANEO' AND a.horaAgendamento >= :inicio AND a.horaAgendamento <= :fim)
        )
      ORDER BY a.horaAgendamento ASC, a.id ASC
  """)
	List<Agendamento> buscarProximoNormalHoje(
			@Param("setorId") Long setorId,
			@Param("inicio") LocalDateTime inicio,
			@Param("agora") LocalDateTime agora,
			@Param("fim") LocalDateTime fim,
			Pageable pageable
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
       SELECT a
       FROM Agendamento a
       WHERE a.setor.id = :setorId
         AND a.tipoAtendimento.peso > 0 
         AND a.situacao IN ('AGENDADO', 'REAGENDADO')
         AND (
             (a.tipoAgendamento = 'AGENDADO' AND a.horaAgendamento >= :inicio AND a.horaAgendamento <= :agora)
             OR 
             (a.tipoAgendamento = 'ESPONTANEO' AND a.horaAgendamento >= :inicio AND a.horaAgendamento <= :fim)
         )
       ORDER BY a.tipoAtendimento.peso ASC, a.horaAgendamento ASC
   """)
	List<Agendamento> buscarProximoPrioridadeHoje(
			@Param("setorId") Long setorId,
			@Param("inicio") LocalDateTime inicio,
			@Param("agora") LocalDateTime agora, // 🟢 Adicionamos o 'agora'
			@Param("fim") LocalDateTime fim,
			Pageable pageable
	);

	@Query("""
    SELECT a
    FROM Agendamento a
    WHERE a.setor.id = :setorId
      AND a.senha = :senha
      AND a.situacao IN ('AGENDADO', 'REAGENDADO', 'EM_ATENDIMENTO', 'FALTOU')
      AND a.horaAgendamento >= :inicio
      AND a.horaAgendamento < :fim
    ORDER BY a.horaAgendamento ASC
""")
	List<Agendamento> buscarPorSenhaHoje(
			@Param("setorId") Long setorId,
			@Param("senha") String senha,
			@Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim,
			Pageable pageable
	);

	@Query(value = """
    select a.senha
    from agendamento a
    join tipo_atendimento ta on ta.id = a.tipo_atendimento_id 
    where a.setor_id = :setorId
      AND ta.sigla = :sigla
      and date(a.hora_agendamento) = :data
    order by a.id desc
    limit 1
""", nativeQuery = true)
	String findUltimaSenhaDoDia(@Param("setorId") Long setorId,
								@Param("sigla") String sigla,
								@Param("data") LocalDate data);

	@Query("""
    select a.senha
    from Agendamento a
    where a.setor.id = :setorId
      and upper(a.tipoAtendimento.nome) = upper(:tipo)
      and a.horaAgendamento >= :inicio
      and a.horaAgendamento < :fim
    order by a.id desc
""")
	List<String> findUltimaSenhaDoDiaParaEspontaneoPorSetor(
			@Param("setorId") Long setorId,
			@Param("tipo") String tipo,
			@Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim,
			Pageable pageable
	);

	@Query("""
    SELECT a 
    FROM Agendamento a 
    LEFT JOIN a.usuario u
    WHERE a.cpf = :cpf 
       OR u.login = :cpf 
    ORDER BY a.horaAgendamento DESC
""")
	List<Agendamento> buscarHistoricoPorCpf(@Param("cpf") String cpf);
}
