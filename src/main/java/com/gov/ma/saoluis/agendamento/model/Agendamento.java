package com.gov.ma.saoluis.agendamento.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "agendamento")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @Column(name = "hora_agendamento", nullable = false)
    private LocalDateTime horaAgendamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoAgendamento situacao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_atendimento_id", nullable = false)
    private TipoAtendimento tipoAtendimento; // NORMAL, PRIORIDADE, PREFERENCIAL

    @Column(nullable = false, length = 10)
    private String senha; // Ex: P001, N002, F003
    
    private LocalDateTime horaChamada;

    @Column(name = "hora_finalizado") // 🟢 Novo campo
    private LocalDateTime horaFinalizado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gerenciador_id")
    private Gerenciador gerenciador;

    @Column(name = "nome_cidadao", length = 255)
    private String nome_cidadao;

    @Column(name = "cpf", length = 11, nullable = false)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "celular", length = 20)
    private String celular;

    @Column(name = "email", length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "secretaria_id", nullable = false)
    private Secretaria secretaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_agendamento", nullable = false)
    private TipoAgendamento tipoAgendamento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @Column(name = "ultimo_ping")
    private LocalDateTime ultimoPing;

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

    public void setTipoAtendimento(TipoAtendimento tipoAtendimento) {
        this.tipoAtendimento = tipoAtendimento;
    }

    public TipoAtendimento getTipoAtendimento() {
        return tipoAtendimento;
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

    public LocalDateTime getHoraFinalizado() {
        return horaFinalizado;
    }

    public void setHoraFinalizado(LocalDateTime horaFinalizado) {
        this.horaFinalizado = horaFinalizado;
    }

    public Gerenciador getAtendente() {
        return gerenciador;
    }

    public void setAtendente(Gerenciador gerenciador) {
        this.gerenciador = gerenciador;
    }

    public String getNomeCidadao(){
        return nome_cidadao;
    }

    public void setNomeCidadao(String nome_cidadao){
        this.nome_cidadao = nome_cidadao;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSecretaria(Secretaria secretaria) {
        this.secretaria = secretaria;
    }

    public Secretaria getSecretaria() {
        return secretaria;
    }

    public TipoAgendamento getTipoAgendamento() {
        return tipoAgendamento;
    }

    public void setTipoAgendamento(TipoAgendamento tipoAgendamento) {
        this.tipoAgendamento = tipoAgendamento;
    }

    public Setor getSetor() {
        return setor;
    }

    public void setSetor(Setor setor) {
        this.setor = setor;
    }

    public LocalDateTime getUltimoPing() {
        return ultimoPing;
    }

    public void setUltimoPing(LocalDateTime ultimoPing) {
        this.ultimoPing = ultimoPing;
    }
}
