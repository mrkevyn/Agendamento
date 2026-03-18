package com.gov.ma.saoluis.agendamento.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Entity
@Table(name = "servico_saude")
public class ServicoSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150, unique = true)
    private String nome;

    @ManyToMany(mappedBy = "servicosSaude")
    private Set<Setor> setores = new HashSet<>();
}