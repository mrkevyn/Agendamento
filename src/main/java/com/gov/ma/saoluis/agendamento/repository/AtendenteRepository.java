package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.Atendente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtendenteRepository extends JpaRepository<Atendente, Long> {

    List<Atendente> findBySecretariaId(Long secretariaId);

    Optional<Atendente> findByAcesso(String acesso);
}
