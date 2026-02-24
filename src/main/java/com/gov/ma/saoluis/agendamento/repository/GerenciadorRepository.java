package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.Gerenciador;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GerenciadorRepository extends JpaRepository<Gerenciador, Long> {

    List<Gerenciador> findBySecretarias_Id(Long secretariaId);

    Optional<Gerenciador> findByCpfOrEmail(String cpf, String email);

    // 🔴 REMOVIDOS OS MÉTODOS QUE USAVAM Integer guiche (causavam erro de boot)
    // boolean existsByGuicheAndSetores_Id(Integer guiche, Long setorId);
    // boolean existsByGuicheAndSetores_IdAndIdNot(Integer guiche, Long setorId, Long id);

    // ✅ NOVO: Validação por ID da entidade Guiche
    @Query("SELECT COUNT(g) > 0 FROM Gerenciador g WHERE g.guiche.id = :guicheId")
    boolean existsByGuicheId(@Param("guicheId") Long guicheId);

    // ✅ NOVO: Validação por ID da entidade Guiche (exceto o próprio ID para edição)
    @Query("SELECT COUNT(g) > 0 FROM Gerenciador g WHERE g.guiche.id = :guicheId AND g.id <> :id")
    boolean existsByGuicheIdAndIdNot(@Param("guicheId") Long guicheId, @Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Gerenciador g SET g.guiche = null")
    void limparTodosOsGuiches();

    @Modifying
    @Transactional
    @Query("UPDATE Gerenciador g SET g.guiche = null WHERE g.id = :id")
    void limparGuichePorId(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Gerenciador g SET g.guiche = null WHERE g.id = :id") // Certifique-se do WHERE g.id = :id
    void desvincularGuiche(@Param("id") Long id);
}