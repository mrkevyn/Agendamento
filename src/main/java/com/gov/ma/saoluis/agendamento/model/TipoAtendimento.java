package com.gov.ma.saoluis.agendamento.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_atendimento") // sem uniqueConstraints
public class TipoAtendimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 1)
    private String sigla;

    @Column(nullable = false)
    private Integer peso = 0;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secretaria_id", nullable = false)
    private Secretaria secretaria;

    // Construtores
    public TipoAtendimento() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }

    public Integer getPeso() {
        return peso;
    }

    public void setPeso(Integer peso) {
        this.peso = peso;
    }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Secretaria getSecretaria() { return secretaria; }
    public void setSecretaria(Secretaria secretaria) { this.secretaria = secretaria; }
}