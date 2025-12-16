package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.GerenciadorDTO;
import com.gov.ma.saoluis.agendamento.config.UsuarioLogadoUtil;
import com.gov.ma.saoluis.agendamento.model.Gerenciador;
import com.gov.ma.saoluis.agendamento.model.Secretaria;
import com.gov.ma.saoluis.agendamento.repository.GerenciadorRepository;
import com.gov.ma.saoluis.agendamento.repository.SecretariaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GerenciadorService {

    private final GerenciadorRepository gerenciadorRepository;
    private final SecretariaRepository secretariaRepository;
    private final LogService logService;

    public GerenciadorService(GerenciadorRepository gerenciadorRepository,
                            SecretariaRepository secretariaRepository,
                            LogService logService) {
        this.gerenciadorRepository = gerenciadorRepository;
        this.secretariaRepository = secretariaRepository;
        this.logService = logService;
    }

    // ➤ Criar gerenciador
    public Gerenciador criar(GerenciadorDTO dto) {

        Secretaria secretaria = secretariaRepository.findById(dto.secretariaId())
                .orElseThrow(() -> new RuntimeException("Secretaria não encontrada"));

        Gerenciador g = new Gerenciador();
        g.setNome(dto.nome());
        g.setCpf(dto.cpf());
        g.setEmail(dto.email());
        g.setSenha(dto.senha()); // simples, sem criptografia
        g.setPerfil(dto.perfil()); // ✅ OBRIGATÓRIO
        g.setGuiche(dto.guiche()); // pode ser null
        g.setSecretaria(secretaria);

        Gerenciador salvo = gerenciadorRepository.save(g);

        // 🔹 USUÁRIO LOGADO (vem do JWT)
        Long usuarioLogadoId = UsuarioLogadoUtil.getUsuarioId();

        // 🔹 Determinar ação do log baseado no perfil
        String acaoLog;
        if ("ATENDENTE".equalsIgnoreCase(salvo.getPerfil())) {
            acaoLog = "ADMIN_CRIACAO_ATENDENTE";
        } else {
            acaoLog = "GERENCIADOR_CRIADO";
        }

        // 🔹 Registrar log
        logService.registrar(
                usuarioLogadoId, // Sem usuário logado ainda, MÉTODO PENDENTE
                "SISTEMA",
                acaoLog,
                "Gerenciador ID: " + salvo.getId() +
                        ", Nome: " + salvo.getNome() +
                        ", CPF: " + salvo.getCpf() +
                        ", Secretaria ID: " + salvo.getSecretaria().getId()
        );

        return salvo;
    }

    // ➤ Atualizar atendente
    public Gerenciador editar(Long id, GerenciadorDTO dto) {

        Gerenciador g = gerenciadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gerenciador não encontrado"));

        Secretaria secretaria = secretariaRepository.findById(dto.secretariaId())
                .orElseThrow(() -> new RuntimeException("Secretaria não encontrada"));

        g.setNome(dto.nome());
        g.setCpf(dto.cpf());
        g.setEmail(dto.email());
        g.setSenha(dto.senha()); // simples
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
                gerenciador.getNome(),              // Nome do usuário
                "LOGIN",
                "Login realizado com sucesso | Perfil: " + gerenciador.getPerfil()
        );

        return gerenciador;
    }

    // ➤ Atualizar guichê (Sistema ou Atendente)
    public Gerenciador atualizarGuiche(Long id, Integer novoGuiche) {
        // Recupera o gerenciador com base no ID
        Gerenciador g = gerenciadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gerenciador não encontrado"));

        // Verifica se o usuário logado pode alterar o guichê
        Long usuarioLogadoId = UsuarioLogadoUtil.getUsuarioId();
        String perfilUsuarioLogado = UsuarioLogadoUtil.getPerfil();  // Pega o perfil do usuário logado

        // Permite alterar o guichê apenas se o usuário for ADMIN ou ATENDENTE
        if (!"ADMIN".equalsIgnoreCase(perfilUsuarioLogado) && !"ATENDENTE".equalsIgnoreCase(perfilUsuarioLogado)) {
            throw new RuntimeException("Você não tem permissão para alterar o guichê");
        }

        // Atualiza o guichê
        g.setGuiche(novoGuiche);
        Gerenciador salvo = gerenciadorRepository.save(g);

        // Registra o log da alteração
        String acaoLog = "GUICHE_ALTERADO";
        logService.registrar(
                usuarioLogadoId,
                "SISTEMA",
                acaoLog,
                "Gerenciador ID: " + salvo.getId() +
                        ", Nome: " + salvo.getNome() +
                        ", Novo Guichê: " + salvo.getGuiche()
        );

        return salvo;
    }
}
