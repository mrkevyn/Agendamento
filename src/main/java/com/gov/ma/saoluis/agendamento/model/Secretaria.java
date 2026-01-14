package com.gov.ma.saoluis.agendamento.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "secretaria")
@Getter
@Setter
public class Secretaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String sigla;

    @Column(nullable = false)
    private boolean ativo = true;
    private boolean visivel = true;

    @OneToMany(
            mappedBy = "secretaria",
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnoreProperties("secretaria")
    private Set<ConfiguracaoAtendimento> configuracoes = new HashSet<>();

    // getters e setters
}
