package com.gov.ma.saoluis.agendamento.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "servico")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String descricao;

    // ➜ Guichê associado ao serviço (opcional)
    @Column(nullable = true)
    private Integer guiche;

    // ➜ Tempo médio de atendimento (em minutos, por exemplo)
    @Column(name = "tempo_atendimento", nullable = true)
    private Integer tempoAtendimento;

    // ➜ Dias de atendimento (ex: "SEG-SEX", "SEG,TER,QUI")
    @Column(nullable = true, length = 50)
    private String dias;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "secretaria_id", nullable = false)
    private Secretaria secretaria;
}
