package com.gov.ma.saoluis.agendamento.repository;
import com.gov.ma.saoluis.agendamento.model.Servico;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    
    List<Servico> findAll();

    List<Servico> findBySecretariaId(Long secretariaId);

    @EntityGraph(attributePaths = {"gerenciadores"})
    List<Servico> findBySetoresId(Long setorId);

    boolean existsByIdAndSetores_Id(Long servicoId, Long setorId);
}
