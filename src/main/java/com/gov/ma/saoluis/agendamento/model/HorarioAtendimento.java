package com.gov.ma.saoluis.agendamento.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "horario_atendimento")
public class HorarioAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "configuracao_id")
    @JsonIgnore
    private ConfiguracaoAtendimento configuracao;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false)
    private Boolean ocupado = false;

    // =========================
    // Getters e Setters
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConfiguracaoAtendimento getConfiguracao() {
        return configuracao;
    }

    public void setConfiguracao(ConfiguracaoAtendimento configuracao) {
        this.configuracao = configuracao;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public Boolean getOcupado() {
        return ocupado;
    }

    public void setOcupado(Boolean ocupado) {
        this.ocupado = ocupado;
    }
}