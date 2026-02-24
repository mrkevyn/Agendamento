package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.GerenciadorDTO;
import com.gov.ma.saoluis.agendamento.config.UsuarioLogadoUtil;
import com.gov.ma.saoluis.agendamento.model.*;
import com.gov.ma.saoluis.agendamento.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GerenciadorService {

    private final GerenciadorRepository gerenciadorRepository;
    private final SecretariaRepository secretariaRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;
    private final EnderecoRepository enderecoRepository;
    private final SetorRepository setorRepository;
    private final GuicheRepository guicheRepository;

    public GerenciadorService(GerenciadorRepository gerenciadorRepository,
                            SecretariaRepository secretariaRepository,
                            LogService logService,
                              EnderecoRepository enderecoRepository,
                              PasswordEncoder passwordEncoder,
                              SetorRepository setorRepository,
                            GuicheRepository guicheRepository) {
        this.gerenciadorRepository = gerenciadorRepository;
        this.secretariaRepository = secretariaRepository;
        this.logService = logService;
        this.enderecoRepository = enderecoRepository;
        this.passwordEncoder = passwordEncoder;
        this.setorRepository = setorRepository;
        this.guicheRepository = guicheRepository;

    }

    // ➤ Criar gerenciador (N Secretarias : N Setores)
    public Gerenciador criar(GerenciadorDTO dto) {

        // 1. Busca a lista de Secretarias (Agora plural no DTO)
        List<Secretaria> secretariasEncontradas = secretariaRepository.findAllById(dto.secretariasIds());
        if (secretariasEncontradas.isEmpty()) {
            throw new RuntimeException("Nenhuma secretaria encontrada para os IDs fornecidos");
        }
        Set<Secretaria> secretarias = new HashSet<>(secretariasEncontradas);

        Long idPrincipal = dto.secretariasIds().get(0);
        Secretaria principal = secretariasEncontradas.stream()
                .filter(s -> s.getId().equals(idPrincipal))
                .findFirst()
                .orElse(secretariasEncontradas.get(0));

        // 2. Busca a lista de Setores
        List<Setor> setoresEncontrados = setorRepository.findAllById(dto.setoresIds());
        if (setoresEncontrados.isEmpty()) {
            throw new RuntimeException("Nenhum setor encontrado para os IDs fornecidos");
        }
        Set<Setor> setores = new HashSet<>(setoresEncontrados);

        // 🔒 3. VALIDAR GUICHÊ ÚNICO
        // O guichê é validado por SETOR, que é o local físico do atendimento
        Guiche guicheEntidade = null;
        if (dto.guicheId() != null) {
            // Validação de ocupação usando a Query customizada que fizemos
            boolean ocupado = gerenciadorRepository.existsByGuicheId(dto.guicheId());
            if (ocupado) {
                throw new RuntimeException("O guichê selecionado já está ocupado por outro atendente.");
            }

            guicheEntidade = guicheRepository.findById(dto.guicheId())
                    .orElseThrow(() -> new RuntimeException("Guichê não encontrado no cadastro auxiliar."));
        }

        // 4. Mapeamento da Entidade
        Gerenciador g = new Gerenciador();
        g.setNome(dto.nome());
        g.setCpf(dto.cpf());
        g.setContato(dto.contato());
        g.setEmail(dto.email());

        // Criptografia obrigatória na criação
        String senhaCriptografada = passwordEncoder.encode(dto.senha());
        g.setSenha(senhaCriptografada);

        g.setPerfil(dto.perfil());
        // 🟢 Sincronização de Guichê
        if (guicheEntidade != null) {
            g.setGuiche(guicheEntidade); // Define o Objeto (Sistema Novo)
            // Se o outro sistema exigir o número na coluna Integer 'guiche' da tabela 'gerenciador':
            // g.setGuicheNumero(guicheEntidade.getNumero());
        }

        // 🟢 PREENCHE A COLUNA FÍSICA (Para o outro sistema)
        g.setSecretariaPrincipal(principal);

        // 🔴 RELACIONAMENTOS N:N
        g.setSecretarias(secretarias);
        g.setSetores(setores);

        Gerenciador salvo = gerenciadorRepository.save(g);

        // 5. Logs e Auditoria
        Long usuarioLogadoId = UsuarioLogadoUtil.getUsuarioId();

        String acaoLog = "ATENDENTE".equalsIgnoreCase(salvo.getPerfil())
                ? "ADMIN_CRIACAO_ATENDENTE"
                : "GERENCIADOR_CRIADO";

        String nomesSecLog = salvo.getSecretarias().stream()
                .map(Secretaria::getNome).collect(Collectors.joining(", "));

        String nomesSetoresLog = salvo.getSetores().stream()
                .map(Setor::getNome).collect(Collectors.joining(", "));

        logService.registrar(
                usuarioLogadoId,
                "SISTEMA",
                acaoLog,
                "Gerenciador ID: " + salvo.getId() +
                        ", CPF: " + salvo.getCpf() +
                        ", Secretarias: [" + nomesSecLog + "]" +
                        ", Setores: [" + nomesSetoresLog + "]"
        );

        return salvo;
    }

    // ➤ Atualizar atendente (N Secretarias : N Setores)
    public Gerenciador editar(Long id, GerenciadorDTO dto) {

        Gerenciador g = gerenciadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gerenciador não encontrado"));

        // 1. Busca a lista de Secretarias (Plural no DTO)
        List<Secretaria> secretariasEncontradas = secretariaRepository.findAllById(dto.secretariasIds());
        if (secretariasEncontradas.isEmpty()) {
            throw new RuntimeException("Nenhuma secretaria encontrada para os IDs fornecidos");
        }
        Set<Secretaria> novasSecretarias = new java.util.HashSet<>(secretariasEncontradas);

        // 2. Busca a lista de Setores
        List<Setor> setoresEncontrados = setorRepository.findAllById(dto.setoresIds());
        if (setoresEncontrados.isEmpty()) {
            throw new RuntimeException("Nenhum setor encontrado para os IDs fornecidos");
        }
        Set<Setor> novosSetores = new java.util.HashSet<>(setoresEncontrados);

        // 🟢 3. VALIDAR GUICHÊ ÚNICO (Entidade Guiche)
        if (dto.guicheId() != null) {
            // Valida se o guichê já está ocupado por OUTRO atendente (IdNot)
            boolean ocupado = gerenciadorRepository.existsByGuicheIdAndIdNot(dto.guicheId(), id);
            if (ocupado) {
                throw new RuntimeException("O Guichê selecionado já está sendo usado por outro atendente.");
            }

            Guiche guicheEntidade = guicheRepository.findById(dto.guicheId())
                    .orElseThrow(() -> new RuntimeException("Guichê não encontrado no cadastro auxiliar."));

            // Atualiza tanto o objeto (FK) quanto o número (Integer legado)
            g.setGuiche(guicheEntidade);
            // g.setGuicheNumero(guicheEntidade.getNumero()); // Descomente se tiver a coluna Integer
        } else {
            g.setGuiche(null);
        }

        // 4. Atualização dos campos básicos
        g.setNome(dto.nome());
        g.setCpf(dto.cpf());
        g.setContato(dto.contato());
        g.setEmail(dto.email());

        // Atualiza senha apenas se fornecida
        if (dto.senha() != null && !dto.senha().isBlank()) {
            g.setSenha(passwordEncoder.encode(dto.senha()));
        }

        g.setPerfil(dto.perfil());

        // 🔴 5. ATUALIZAÇÃO DOS VÍNCULOS N:N
        g.getSecretarias().clear();
        g.getSecretarias().addAll(secretariasEncontradas);

        g.getSetores().clear();
        g.getSetores().addAll(setoresEncontrados);

        return gerenciadorRepository.save(g);
    }

    // ➤ Listar todos
    public List<Gerenciador> listarTodos() {
        return gerenciadorRepository.findAll();
    }

    // ➤ Listar por secretaria
    public List<Gerenciador> listarPorSecretaria(Long secretariaId) {
        // 🔴 Mudou de findBySecretariaId para findBySecretarias_Id
        return gerenciadorRepository.findBySecretarias_Id(secretariaId);
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

        // 🔐 COMPARAÇÃO SEGURA COM HASH
        if (!passwordEncoder.matches(senha, gerenciador.getSenha())) {
            throw new RuntimeException("Senha inválida");
        }

        // 🔴 Agora pegamos os nomes de TODAS as secretarias (N:N)
        String nomesSecretarias = gerenciador.getSecretarias().stream()
                .map(Secretaria::getNome)
                .collect(Collectors.joining(", "));

        // 🟢 Transforma a lista de SETORES em uma String para o log (N:N)
        String nomesSetores = gerenciador.getSetores().stream()
                .map(Setor::getNome)
                .collect(Collectors.joining(", "));

        // 🔹 REGISTRAR LOG DE LOGIN
        logService.registrar(
                gerenciador.getId(),
                gerenciador.getPerfil(),
                "LOGIN",
                "Nome: " + gerenciador.getNome() +
                        "; Email: " + gerenciador.getEmail() +
                        "; Secretarias: [" + nomesSecretarias + "]" +
                        "; Setores: [" + nomesSetores + "]"
        );

        return gerenciador;
    }

    @Transactional
    public Gerenciador atualizarGuiche(Long id, Long guicheId) { // 🟢 Agora recebe Long guicheId

        Gerenciador g = gerenciadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gerenciador não encontrado"));

        Long usuarioLogadoId = UsuarioLogadoUtil.getUsuarioId();
        if (usuarioLogadoId == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        Gerenciador usuarioLogado = gerenciadorRepository.findById(usuarioLogadoId)
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado"));

        String perfil = usuarioLogado.getPerfil();

        // ✔ Validação de Perfil
        if ("ATENDENTE".equalsIgnoreCase(perfil) && !usuarioLogado.getId().equals(id)) {
            throw new RuntimeException("Atendente só pode alterar o próprio guichê");
        }

        if (!"ADMIN".equalsIgnoreCase(perfil) && !"ATENDENTE".equalsIgnoreCase(perfil)) {
            throw new RuntimeException("Você não tem permissão para alterar o guichê");
        }

        // 🟢 Nova Lógica "Trava-Tudo"
        if (guicheId != null) {
            // 1. Primeiro buscamos a entidade para saber o número dela (para a mensagem)
            Guiche guicheEntidade = guicheRepository.findById(guicheId)
                    .orElseThrow(() -> new RuntimeException("Guichê não encontrado no cadastro auxiliar."));

            // 2. Verificamos se já está ocupado
            boolean ocupado = gerenciadorRepository.existsByGuicheIdAndIdNot(guicheId, g.getId());

            // 3. SE ESTIVER OCUPADO, LANÇAMOS A EXCEÇÃO IMEDIATAMENTE
            // Isso faz o Rollback da transação e nada é salvo no banco.
            if (ocupado) {
                throw new RuntimeException("O Guichê " + guicheEntidade.getNumero() + " já está sendo utilizado.");
            }

            // 4. SÓ CHEGA AQUI SE NÃO ESTIVER OCUPADO
            // Agora sim, alteramos o objeto com segurança
            g.setGuiche(guicheEntidade);
        } else {
            g.setGuiche(null);
        }

        Gerenciador salvo = gerenciadorRepository.save(g);

        logService.registrar(
                usuarioLogado.getId(),
                usuarioLogado.getPerfil(),
                "GUICHE_ALTERADO",
                "Atendente ID: " + salvo.getId() +
                        ", Nome: " + salvo.getNome() +
                        ", Novo Guichê: " + (salvo.getGuiche() != null ? salvo.getGuiche().getNumero() : "Nenhum")
        );

        return salvo;
    }
}
