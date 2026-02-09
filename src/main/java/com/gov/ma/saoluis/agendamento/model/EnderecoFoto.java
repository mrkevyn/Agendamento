package com.gov.ma.saoluis.agendamento.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "endereco_foto")
@Getter
@Setter
public class EnderecoFoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String caminho; // ex: /uploads/enderecos/12/foto1.jpg

    @Column(length = 100)
    private String descricao;

    @Column(nullable = false)
    private boolean principal = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id", nullable = false)
    @JsonBackReference
    private Endereco endereco;
}
