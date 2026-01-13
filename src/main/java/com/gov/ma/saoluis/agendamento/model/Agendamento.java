package com.gov.ma.saoluis.agendamento.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "agendamento")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "configuracao_atendimento_id", nullable = false)
    private ConfiguracaoAtendimento configuracao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @Column(name = "hora_agendamento", nullable = false)
    private LocalDateTime horaAgendamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoAgendamento situacao;

    @Column(name = "tipo_atendimento", nullable = false, length = 30)
    private String tipoAtendimento; // NORMAL, PRIORIDADE, PREFERENCIAL

    @Column(nullable = false, length = 10)
    private String senha; // Ex: P001, N002, F003
    
    private LocalDateTime horaChamada;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gerenciador_id")
    private Gerenciador gerenciador;

    @Column(name = "nome_cidadao", length = 255)
    private String nome_cidado;

    // Getters e Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public LocalDateTime getHoraAgendamento() {
        return horaAgendamento;
    }
    public void setHoraAgendamento(LocalDateTime horaAgendamento) {
        this.horaAgendamento = horaAgendamento;
    }

    public SituacaoAgendamento getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoAgendamento situacao) {
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

    public Gerenciador getAtendente() {
        return gerenciador;
    }

    public void setAtendente(Gerenciador gerenciador) {
        this.gerenciador = gerenciador;
    }

    // getters e setters
    public ConfiguracaoAtendimento getConfiguracao() {
        return configuracao;
    }

    public void setConfiguracao(ConfiguracaoAtendimento configuracao) {
        this.configuracao = configuracao;
    }

    public String getNome_cidado(){
        return nome_cidado;
    }

    public void setNome_cidado(String nome_cidado){
        this.nome_cidado = nome_cidado;
    }
}
