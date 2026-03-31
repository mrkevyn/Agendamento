package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.TipoAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoAtendimentoRepository extends JpaRepository<TipoAtendimento, Long> {
    // Busca pelo nome dentro de uma secretaria específica
    Optional<TipoAtendimento> findByNomeAndSecretaria_Id(String nome, Long secretariaId);

    // Lista apenas os ativos de uma secretaria
    List<TipoAtendimento> findBySecretaria_IdAndAtivoTrue(Long secretariaId);

    // Lista todos de uma secretaria (para o admin)
    List<TipoAtendimento> findBySecretaria_Id(Long secretariaId);
}