package com.gov.ma.saoluis.agendamento.repository;
import com.gov.ma.saoluis.agendamento.model.Servico;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    
    List<Servico> findAll();

    List<Servico> findBySecretariaId(Long secretariaId);

    @Query("SELECT DISTINCT s FROM Servico s " +
            "LEFT JOIN FETCH s.gerenciadores " + // Traz os donos se existirem, mas não obriga a ter
            "INNER JOIN s.setores st " +        // Obriga a pertencer ao setor (conforme seu SQL puro)
            "WHERE st.id = :setorId")
    List<Servico> findBySetoresId(@Param("setorId") Long setorId);

    boolean existsByIdAndSetores_Id(Long servicoId, Long setorId);
}
