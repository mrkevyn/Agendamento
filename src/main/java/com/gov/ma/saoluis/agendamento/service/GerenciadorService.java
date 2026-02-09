package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.GerenciadorDTO;
import com.gov.ma.saoluis.agendamento.config.UsuarioLogadoUtil;
import com.gov.ma.saoluis.agendamento.model.Endereco;
import com.gov.ma.saoluis.agendamento.model.Gerenciador;
import com.gov.ma.saoluis.agendamento.model.Secretaria;
import com.gov.ma.saoluis.agendamento.repository.EnderecoRepository;
import com.gov.ma.saoluis.agendamento.repository.GerenciadorRepository;
import com.gov.ma.saoluis.agendamento.repository.SecretariaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GerenciadorService {

    private final GerenciadorRepository gerenciadorRepository;
    private final SecretariaRepository secretariaRepository;
    private final LogService logService;
    private final EnderecoRepository enderecoRepository;


    public GerenciadorService(GerenciadorRepository gerenciadorRepository,
                            SecretariaRepository secretariaRepository,
                            LogService logService,
                              EnderecoRepository enderecoRepository) {
        this.gerenciadorRepository = gerenciadorRepository;
        this.secretariaRepository = secretariaRepository;
        this.logService = logService;
        this.enderecoRepository = enderecoRepository;
    }

    // ➤ Criar gerenciador
    public Gerenciador criar(GerenciadorDTO dto) {

        Secretaria secretaria = secretariaRepository.findById(dto.secretariaId())
                .orElseThrow(() -> new RuntimeException("Secretaria não encontrada"));

        Endereco endereco = enderecoRepository.findById(dto.enderecoId())
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));

        // 🔒 VALIDAR GUICHÊ ÚNICO
        if (dto.guiche() != null) {
            boolean ocupado = gerenciadorRepository
                    .existsByGuicheAndEnderecoId(dto.guiche(), endereco.getId());

            if (ocupado) {
                throw new RuntimeException(
                        "Guichê " + dto.guiche() + " já está em uso neste endereço"
                );
            }
        }

        Gerenciador g = new Gerenciador();
        g.setNome(dto.nome());
        g.setCpf(dto.cpf());
        g.setContato(dto.contato());
        g.setEmail(dto.email());
        g.setSenha(dto.senha());
        g.setPerfil(dto.perfil());
        g.setGuiche(dto.guiche());
        g.setSecretaria(secretaria);
        g.setEndereco(endereco);

        Gerenciador salvo = gerenciadorRepository.save(g);

        Long usuarioLogadoId = UsuarioLogadoUtil.getUsuarioId();

        String acaoLog = "ATENDENTE".equalsIgnoreCase(salvo.getPerfil())
                ? "ADMIN_CRIACAO_ATENDENTE"
                : "GERENCIADOR_CRIADO";

        logService.registrar(
                usuarioLogadoId,
                "SISTEMA",
                acaoLog,
                "Gerenciador ID: " + salvo.getId() + ", Nome: " + salvo.getNome() + ", CPF: " + salvo.getCpf() + ", Secretaria ID: " + salvo.getSecretaria().getId() );

        return salvo;
    }

    // ➤ Atualizar atendente
    public Gerenciador editar(Long id, GerenciadorDTO dto) {

        Gerenciador g = gerenciadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gerenciador não encontrado"));

        Secretaria secretaria = secretariaRepository.findById(dto.secretariaId())
                .orElseThrow(() -> new RuntimeException("Secretaria não encontrada"));

        // 🔒 VALIDAR GUICHÊ ÚNICO (exceto ele mesmo)
        if (dto.guiche() != null) {
            boolean ocupado = gerenciadorRepository
                    .existsByGuicheAndSecretariaIdAndIdNot(
                            dto.guiche(),
                            secretaria.getId(),
                            id
                    );

            if (ocupado) {
                throw new RuntimeException(
                        "Guichê " + dto.guiche() + " já está em uso nesta secretaria"
                );
            }
        }

        g.setNome(dto.nome());
        g.setCpf(dto.cpf());
        g.setEmail(dto.email());
        g.setSenha(dto.senha());
        g.setGuiche(dto.guiche());
        g.setSecretaria(secretaria);

        return gerenciadorRepository.save(g);
    }

    // ➤ Listar todos
    public List<Gerenciador> listarTodos() {
        return gerenciadorRepository.findAll();
    }

    // ➤ Listar por secretaria
    public List<Gerenciador> listarPorSecretaria(Long secretariaId) {
        return gerenciadorRepository.findBySecretariaId(secretariaId);
    }

    // ➤ Buscar por ID
    public Gerenciador buscarPorId(Long id) {
        return gerenciadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));
    }

    // ➤ Remover
    public void remover(Long id) {
        Gerenciador at = gerenciadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));
        gerenciadorRepository.delete(at);
    }

    public Gerenciador login(String login, String senha) {

        Gerenciador gerenciador = gerenciadorRepository
                .findByCpfOrEmail(login, login)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!gerenciador.getSenha().equals(senha)) {
            throw new RuntimeException("Senha inválida");
        }

        // 🔹 REGISTRAR LOG DE LOGIN
        logService.registrar(
                gerenciador.getId(),                // ID do usuário logado
                gerenciador.getPerfil(),              // Nome do usuário
                "LOGIN",
                "Nome: " + gerenciador.getNome() + "; Email: " + gerenciador.getEmail() + "; Secretaria: " + gerenciador.getSecretaria().getNome()
        );

        return gerenciador;
    }

    // ➤ Atualizar guichê (Sistema ou Atendente)
    public Gerenciador atualizarGuiche(Long id, Integer novoGuiche) {

        Gerenciador g = gerenciadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gerenciador não encontrado"));

        Long usuarioLogadoId = UsuarioLogadoUtil.getUsuarioId();

        if (usuarioLogadoId == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        Gerenciador usuarioLogado = gerenciadorRepository.findById(usuarioLogadoId)
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado"));

        String perfil = usuarioLogado.getPerfil();

        // ✔ ADMIN pode alterar qualquer guichê
        // ✔ ATENDENTE só pode alterar o próprio
        if ("ATENDENTE".equalsIgnoreCase(perfil) && !usuarioLogado.getId().equals(id)) {
            throw new RuntimeException("Atendente só pode alterar o próprio guichê");
        }

        if (!"ADMIN".equalsIgnoreCase(perfil) && !"ATENDENTE".equalsIgnoreCase(perfil)) {
            throw new RuntimeException("Você não tem permissão para alterar o guichê");
        }

        // 🔴 Validação de guichê único
        if (novoGuiche != null &&
                gerenciadorRepository.existsBySecretariaIdAndGuicheAndIdNot(
                        g.getSecretaria().getId(),
                        novoGuiche,
                        g.getId()
                )) {
            throw new RuntimeException("Guichê já está sendo utilizado por outro atendente");
        }

        g.setGuiche(novoGuiche);
        Gerenciador salvo = gerenciadorRepository.save(g);

        logService.registrar(
                usuarioLogado.getId(),
                usuarioLogado.getPerfil(),
                "GUICHE_ALTERADO",
                "Atendente ID: " + salvo.getId() +
                        ", Nome: " + salvo.getNome() +
                        ", Novo Guichê: " + salvo.getGuiche()
        );

        return salvo;
    }
}
