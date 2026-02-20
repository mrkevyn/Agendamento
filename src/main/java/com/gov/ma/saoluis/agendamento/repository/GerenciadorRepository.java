package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.Gerenciador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GerenciadorRepository extends JpaRepository<Gerenciador, Long> {

    // ✅ Este é o correto para buscar em coleções ManyToMany
    List<Gerenciador> findBySecretarias_Id(Long secretariaId);

    Optional<Gerenciador> findByCpfOrEmail(String cpf, String email);

    // ✅ Métodos para validação de guichê em Setores (Coleção)
    boolean existsByGuicheAndSetores_Id(Integer guiche, Long setorId);

    boolean existsByGuicheAndSetores_IdAndIdNot(Integer guiche, Long setorId, Long id);

    /* 🗑️ REMOVIDOS:
       - findBySecretariaId (Causava o erro de boot por estar no singular)
       - existsByGuicheAndSetorId (Singular não existe mais na entidade)
       - existsByGuicheAndSetorIdAndIdNot (Singular não existe mais na entidade)
    */
}