package com.gov.ma.saoluis.agendamento.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_atendimento")
public class TipoAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nome; // Ex: NORMAL, PRIORIDADE, PREFERENCIAL

    @Column(nullable = false, length = 1)
    private String sigla; // Ex: N para Normal, P para Prioridade (Usado na geração da senha)

    @Column(nullable = false)
    private Boolean ativo = true; // Permite ocultar do usuário final sem deletar do banco

    // Construtores
    public TipoAtendimento() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}