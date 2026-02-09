package com.gov.ma.saoluis.agendamento.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "gerenciador",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "cpf"),
                @UniqueConstraint(columnNames = "email")
        }
)
public class Gerenciador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(nullable = false, length = 124, unique = true)
    private String contato;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false, length = 30)
    private String perfil;

    // ➜ Guichê opcional
    @Column(nullable = true)
    private Integer guiche;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "secretaria_id", nullable = false)
    private Secretaria secretaria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "endereco_id", nullable = false)
    private Endereco endereco;

    // Construtor padrão (Hibernate)
    public Gerenciador() {
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setContato(String contato){
        this.contato = contato;
    }

    public String getContato(){
        return contato;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

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

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }
}
