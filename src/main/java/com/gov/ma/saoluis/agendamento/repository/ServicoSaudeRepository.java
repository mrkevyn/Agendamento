package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.ServicoSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoSaudeRepository extends JpaRepository<ServicoSaude, Long> {
    List<ServicoSaude> findBySetoresId(Long setorId);

    boolean existsByIdAndSetores_Id(Long id, Long setorId);
}