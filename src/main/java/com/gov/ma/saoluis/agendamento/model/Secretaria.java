package com.gov.ma.saoluis.agendamento.model;

import jakarta.persistence.*;

@Entity
@Table(name = "secretaria", schema = "public")
public class Secretaria {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 220)
    private String nome;

    @Column(name = "sigla", nullable = false, length = 12)
    private String sigla;

    @Column(name = "visivel", nullable = false)
    private Boolean visivel;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    /* =======================
       CONSTRUTORES
    ======================== */

    public Secretaria() {
    }

    public Secretaria(Integer id, String nome, String sigla, Boolean visivel, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.sigla = sigla;
        this.visivel = visivel;
        this.ativo = ativo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public Boolean getVisivel() {
        return visivel;
    }

    public void setVisivel(Boolean visivel) {
        this.visivel = visivel;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
