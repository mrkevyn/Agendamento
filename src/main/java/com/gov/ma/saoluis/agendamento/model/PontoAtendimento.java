package com.gov.ma.saoluis.agendamento.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "ponto_atendimento") // Renomeado no banco também
public class PontoAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numero;

    @Column(name = "descricao", length = 100)
    private String descricao; // Ex: "Consultório", "Guichê", "Triagem"

    @ManyToOne
    @JoinColumn(name = "setor_id", nullable = false)
    @JsonIgnoreProperties({"pontosAtendimento", "servicos", "servicosSaude", "configuracoes", "secretaria", "endereco"})
    private Setor setor;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Setor getSetor() { return setor; }
    public void setSetor(Setor setor) { this.setor = setor; }
}