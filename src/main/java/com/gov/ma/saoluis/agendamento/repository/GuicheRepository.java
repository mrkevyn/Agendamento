package com.gov.ma.saoluis.agendamento.repository;

import com.gov.ma.saoluis.agendamento.model.Guiche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuicheRepository extends JpaRepository<Guiche, Long> {

    @Query("SELECT COUNT(g) > 0 FROM Gerenciador g WHERE g.guiche.id = :guicheId")
    boolean existsByGuicheId(@Param("guicheId") Long guicheId);

    List<Guiche> findBySetorId(Long setorId);
}