package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.Gerenciador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GerenciadorRepository extends JpaRepository<Gerenciador, Long> {

    List<Gerenciador> findBySecretariaId(Long secretariaId);

    Optional<Gerenciador> findByCpfOrEmail(String cpf, String email);
}
