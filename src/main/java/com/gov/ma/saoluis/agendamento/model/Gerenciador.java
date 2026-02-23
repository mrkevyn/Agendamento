package com.gov.ma.saoluis.agendamento.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @Column(nullable = true)
    private Integer guiche;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secretaria_id", nullable = false) // Mantemos o nullable conforme o banco exige
    private Secretaria secretariaPrincipal;

    // 🔴 ESCOPO: N Secretarias
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "gerenciador_secretaria",
            joinColumns = @JoinColumn(name = "gerenciador_id"),
            inverseJoinColumns = @JoinColumn(name = "secretaria_id")
    )
    private Set<Secretaria> secretarias = new HashSet<>();

    // 🔴 ESCOPO: N Setores
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "gerenciador_setor",
            joinColumns = @JoinColumn(name = "gerenciador_id"),
            inverseJoinColumns = @JoinColumn(name = "setor_id")
    )
    private Set<Setor> setores = new HashSet<>();

    public Gerenciador() {}

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }

    // --- Getters e Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getContato() { return contato; }
    public void setContato(String contato) { this.contato = contato; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public Integer getGuiche() { return guiche; }
    public void setGuiche(Integer guiche) { this.guiche = guiche; }

    public Set<Secretaria> getSecretarias() { return secretarias; }
    public void setSecretarias(Set<Secretaria> secretarias) { this.secretarias = secretarias; }

    public Set<Setor> getSetores() { return setores; }
    public void setSetores(Set<Setor> setores) { this.setores = setores; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    public Secretaria getSecretariaPrincipal() {
        return secretariaPrincipal;
    }

    public void setSecretariaPrincipal(Secretaria secretariaPrincipal) {
        this.secretariaPrincipal = secretariaPrincipal;
    }
}