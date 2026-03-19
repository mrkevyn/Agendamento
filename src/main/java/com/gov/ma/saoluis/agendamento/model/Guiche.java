package com.gov.ma.saoluis.agendamento.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "guiche")
public class Guiche {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numero; // O número que aparece no painel (ex: 01, 02)

    @ManyToOne
    @JoinColumn(name = "setor_id", nullable = false)
    @JsonIgnoreProperties({"guiches", "servicos", "servicosSaude", "configuracoes", "secretaria", "endereco"})
    private Setor setor; // O guichê pertence a um setor específico

    // Getters e Setters

    public void setSetor(Setor setor) {
        this.setor = setor;
    }

    public Setor getSetor() {
        return setor;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }
}