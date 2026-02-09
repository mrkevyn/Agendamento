package com.gov.ma.saoluis.agendamento.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "endereco")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String bairro;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(length = 9)
    private String cep;

    @Column(length = 200)
    private String complemento;

    // 🌍 COORDENADAS GEOGRÁFICAS
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    // 🔁 LADO INVERSO (evita loop no JSON)
    @JsonIgnore
    @ManyToMany(mappedBy = "enderecos")
    private Set<Servico> servicos = new HashSet<>();
}
