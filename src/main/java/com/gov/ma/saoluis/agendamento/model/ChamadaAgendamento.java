package com.gov.ma.saoluis.agendamento.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chamada_agendamento")
public class ChamadaAgendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @ManyToOne
    @JoinColumn(name = "gerenciador_id", nullable = false)
    private Gerenciador gerenciador;

    @ManyToOne
    @JoinColumn(name = "secretaria_id")
    private Secretaria secretaria;

    private String senha;

    @Column(name = "tipo_atendimento")
    private String tipoAtendimento;

    private LocalDateTime dataChamada;

    @Column(name = "guiche")
    private Integer guiche;

    // getters e setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    public Gerenciador getGerenciador() {
        return gerenciador;
    }

    public void setGerenciador(Gerenciador gerenciador) {
        this.gerenciador = gerenciador;
    }

    public Secretaria getSecretaria() {
        return secretaria;
    }

    public void setSecretaria(Secretaria secretaria) {
        this.secretaria = secretaria;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipoAtendimento() {
        return tipoAtendimento;
    }

    public void setTipoAtendimento(String tipoAtendimento) {
        this.tipoAtendimento = tipoAtendimento;
    }

    public LocalDateTime getDataChamada() {
        return dataChamada;
    }

    public void setDataChamada(LocalDateTime dataChamada) {
        this.dataChamada = dataChamada;
    }

    public Integer getGuiche() {
        return guiche;
    }

    public void setGuiche(Integer guiche) {
        this.guiche = guiche;
    }

}
