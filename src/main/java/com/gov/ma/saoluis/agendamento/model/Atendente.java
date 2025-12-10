package com.gov.ma.saoluis.agendamento.model;

import jakarta.persistence.*;

@Entity
@Table(name = "atendente")
public class Atendente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 200)
    private String acesso; // nome + nomeSecretaria

    @Column(nullable = false)
    private Integer guiche;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "secretaria_id", nullable = false)
    private Secretaria secretaria;

    // ➤ Construtor padrão (necessário para o Hibernate)
    public Atendente() {
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getAcesso() {
        return acesso;
    }

    public void setAcesso(String acesso) {
        this.acesso = acesso;
    }

    public Integer getGuiche() {
        return guiche;
    }

    public void setGuiche(Integer guiche) {
        this.guiche = guiche;
    }

    public Secretaria getSecretaria() {
        return secretaria;
    }

    public void setSecretaria(Secretaria secretaria) {
        this.secretaria = secretaria;
    }
}
