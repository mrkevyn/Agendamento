package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.Setor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SetorRepository extends JpaRepository<Setor, Long> {

    // Este método buscará todos os setores que pertencem a um ID de endereço específico
    List<Setor> findByEnderecoId(Long enderecoId);
}