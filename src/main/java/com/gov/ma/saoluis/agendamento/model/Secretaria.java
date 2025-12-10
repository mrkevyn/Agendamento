package com.gov.ma.saoluis.agendamento.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    // getters e setters
}
