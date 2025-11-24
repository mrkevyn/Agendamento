package com.gov.ma.saoluis.agendamento.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "servico_id", nullable = false)
    private Long servicoId;

    @Column(name = "hora_agendamento", nullable = false)
    private LocalDateTime horaAgendamento;

    @Column(nullable = false, length = 50)
    private String situacao;

    @Column(name = "tipo_atendimento", nullable = false, length = 30)
    private String tipoAtendimento; // NORMAL, PRIORIDADE, PREFERENCIAL

    @Column(nullable = false, length = 10)
    private String senha; // Ex: P001, N002, F003
    
    private LocalDateTime horaChamada;

    // Getters e Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getServicoId() {
        return servicoId;
    }
    public void setServicoId(Long servicoId) {
        this.servicoId = servicoId;
    }

    public LocalDateTime getHoraAgendamento() {
        return horaAgendamento;
    }
    public void setHoraAgendamento(LocalDateTime horaAgendamento) {
        this.horaAgendamento = horaAgendamento;
    }

    public String getSituacao() {
        return situacao;
    }
    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public String getTipoAtendimento() {
        return tipoAtendimento;
    }
    public void setTipoAtendimento(String tipoAtendimento) {
        this.tipoAtendimento = tipoAtendimento;
    }

    public String getSenha() {
        return senha;
    }
    public void setSenha(String senha) {
        this.senha = senha;
    }
	public LocalDateTime getHoraChamada() {
		return horaChamada;
	}
	public void setHoraChamada(LocalDateTime horaChamada) {
		this.horaChamada = horaChamada;
	}
}
