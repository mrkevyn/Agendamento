package com.gov.ma.saoluis.agendamento.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "setor")
public class Setor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id")
    @JsonIgnore
    private Endereco endereco;

    // 🔗 NOVO VÍNCULO: MUITOS SETORES PERTENCEM A UMA SECRETARIA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secretaria_id")
    @JsonIgnore
    private Secretaria secretaria;

    // ✅ Lado inverso do N:N
    @ManyToMany(mappedBy = "setores")
    @JsonIgnore
    private Set<Servico> servicos = new HashSet<>();

    @OneToMany(mappedBy = "setor", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<ConfiguracaoAtendimento> configuracoes;

    @ManyToMany
    @JoinTable(
            name = "setor_servico_saude",
            joinColumns = @JoinColumn(name = "setor_id"),
            inverseJoinColumns = @JoinColumn(name = "servico_saude_id")
    )
    private Set<ServicoSaude> servicosSaude = new HashSet<>();
}