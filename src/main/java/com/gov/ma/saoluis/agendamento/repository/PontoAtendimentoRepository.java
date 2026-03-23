package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.PontoAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PontoAtendimentoRepository extends JpaRepository<PontoAtendimento, Long> {

    /**
     * Verifica se o Ponto de Atendimento está vinculado a algum Gerenciador (Atendente) ativo.
     * Nota: Esta query acessa a entidade Gerenciador para validar a ocupação.
     */
    @Query("SELECT COUNT(g) > 0 FROM Gerenciador g WHERE g.pontoAtendimento.id = :pontoAtendimentoId")
    boolean existsByPontoAtendimentoId(@Param("pontoAtendimentoId") Long pontoAtendimentoId);

    /**
     * Lista todos os pontos de atendimento vinculados a um setor específico.
     */
    List<PontoAtendimento> findBySetorId(Long setorId);

    /**
     * Busca por número dentro de um setor (útil para validações de duplicidade manual)
     */
    List<PontoAtendimento> findByNumeroAndSetorId(Integer numero, Long setorId);
}