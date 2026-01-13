package com.gov.ma.saoluis.agendamento.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "configuracao_atendimento")
public class ConfiguracaoAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Secretaria dona do horário
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "secretaria_id", nullable = false)
    private Secretaria secretaria;

    // ⏰ Bloco
    private LocalTime horaInicio;
    private LocalTime horaFim;

    // 🔹 Regra escolhida
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRegraAtendimento tipoRegra;

    // 🅰️ Usado quando regra = POR_QUANTIDADE
    private Integer quantidadeAtendimentos;

    // 🅱️ Usado quando regra = POR_INTERVALO
    private Integer intervaloMinutos;

    private Integer numeroGuiches;

    // 📆 Dias da semana
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<DiaSemana> diasAtendimento;

    // ⏱️ Horários gerados
    @OneToMany(
            mappedBy = "configuracao",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<HorarioAtendimento> horarios = new HashSet<>();

    private Boolean ativo = true;

    // getters e setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Secretaria getSecretaria() {
        return secretaria;
    }

    public void setSecretaria(Secretaria secretaria) {
        this.secretaria = secretaria;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }

    public TipoRegraAtendimento getTipoRegra() {
        return tipoRegra;
    }

    public void setTipoRegra(TipoRegraAtendimento tipoRegra) {
        this.tipoRegra = tipoRegra;
    }

    public Integer getQuantidadeAtendimentos() {
        return quantidadeAtendimentos;
    }

    public void setQuantidadeAtendimentos(Integer quantidadeAtendimentos) {
        this.quantidadeAtendimentos = quantidadeAtendimentos;
    }

    public Integer getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setIntervaloMinutos(Integer intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }

    public Integer getNumeroGuiches() {
        return numeroGuiches;
    }

    public void setNumeroGuiches(Integer numeroGuiches) {
        this.numeroGuiches = numeroGuiches;
    }

    public Set<DiaSemana> getDiasAtendimento() {
        return diasAtendimento;
    }

    public void setDiasAtendimento(Set<DiaSemana> diasAtendimento) {
        this.diasAtendimento = diasAtendimento;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Set<HorarioAtendimento> getHorarios() {
        return horarios;
    }

    public void setHorarios(Set<HorarioAtendimento> horarios) {
        this.horarios = horarios;
    }
}
