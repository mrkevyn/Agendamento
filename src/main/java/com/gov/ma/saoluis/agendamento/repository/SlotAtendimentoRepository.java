package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.SlotAtendimento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SlotAtendimentoRepository extends JpaRepository<SlotAtendimento, Long> {

    List<SlotAtendimento> findByConfiguracaoIdAndDataOrderByHora(Long configuracaoId, LocalDate data);

    Optional<SlotAtendimento> findByConfiguracaoIdAndDataAndHora(Long configuracaoId, LocalDate data, LocalTime hora);


    boolean existsByConfiguracaoIdAndDataAndReservadosGreaterThan(
            Long configuracaoId,
            LocalDate data,
            int reservados
    );

    void deleteByConfiguracaoIdAndData(Long configuracaoId, LocalDate data);

    // idempotente: não quebra transação se já existir
    @Modifying
    @Query(value = """
        INSERT INTO slot_atendimento (ativo, capacidade, configuracao_id, data, hora, reservados)
        VALUES (true, :capacidade, :configuracaoId, :data, :hora, 0)
        ON CONFLICT (configuracao_id, data, hora) DO NOTHING
        """, nativeQuery = true)
    void insertIfNotExists(
            @Param("configuracaoId") Long configuracaoId,
            @Param("data") LocalDate data,
            @Param("hora") LocalTime hora,
            @Param("capacidade") int capacidade
    );

    // use isso só no POST /agendamentos
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s from SlotAtendimento s
        where s.configuracao.id = :configuracaoId
          and s.data = :data
          and s.hora = :hora
    """)
    Optional<SlotAtendimento> lockSlot(
            @Param("configuracaoId") Long configuracaoId,
            @Param("data") LocalDate data,
            @Param("hora") LocalTime hora
    );

    List<SlotAtendimento> findByConfiguracaoSetorIdAndAtivoTrueAndDataOrderByHoraAsc(
            Long setorId, LocalDate data
    );

    List<SlotAtendimento> findByConfiguracaoSetorIdAndAtivoTrueAndDataGreaterThanEqualOrderByDataAscHoraAsc(
            Long setorId, LocalDate data
    );

    @Modifying
    @Query("""
  delete from SlotAtendimento s
  where s.configuracao.id = :configuracaoId
    and s.data = :data
    and s.hora = :hora
""")
    int deleteByConfiguracaoIdAndDataAndHora(
            @Param("configuracaoId") Long configuracaoId,
            @Param("data") LocalDate data,
            @Param("hora") LocalTime hora
    );

    List<SlotAtendimento> findByConfiguracaoSetorIdAndDataAndAtivoTrue(
            Long setorId,
            LocalDate data
    );

    @Modifying
    @Query("""
        UPDATE SlotAtendimento s 
        SET s.capacidade = :novaCapacidade 
        WHERE s.configuracao.id = :configuracaoId 
          AND s.data >= CURRENT_DATE
    """)
    void atualizarCapacidadeFutura(
            @Param("configuracaoId") Long configuracaoId,
            @Param("novaCapacidade") int novaCapacidade
    );
}