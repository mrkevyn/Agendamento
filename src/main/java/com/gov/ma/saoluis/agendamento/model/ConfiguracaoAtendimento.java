package com.gov.ma.saoluis.agendamento.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
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

    private LocalTime pausaInicio; // ex: 13:00
    private LocalTime pausaFim;    // ex: 14:00

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
    @CollectionTable(
            name = "configuracao_datas",
            joinColumns = @JoinColumn(name = "configuracao_id")
    )
    @Column(name = "data", nullable = false)
    private Set<LocalDate> datasAtendimento = new HashSet<>();

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

    public LocalTime getPausaInicio() {
        return pausaInicio;
    }

    public void setPausaInicio(LocalTime pausaInicio) {
        this.pausaInicio = pausaInicio;
    }

    public LocalTime getPausaFim() {
        return pausaFim;
    }

    public void setPausaFim(LocalTime pausaFim) {
        this.pausaFim = pausaFim;
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

    public void setDatasAtendimento(Set<LocalDate> datasAtendimento) {
        this.datasAtendimento = datasAtendimento;
    }

    public Set<LocalDate> getDatasAtendimento() {
        return datasAtendimento;
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
