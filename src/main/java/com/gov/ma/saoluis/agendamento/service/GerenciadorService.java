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
    private final PontoAtendimentoRepository pontoAtendimentoRepository;

    public GerenciadorService(GerenciadorRepository gerenciadorRepository,
                            SecretariaRepository secretariaRepository,
                            LogService logService,
                              EnderecoRepository enderecoRepository,
                              PasswordEncoder passwordEncoder,
                              SetorRepository setorRepository,
                              PontoAtendimentoRepository pontoAtendimentoRepository) {
        this.gerenciadorRepository = gerenciadorRepository;
        this.secretariaRepository = secretariaRepository;
        this.logService = logService;
        this.enderecoRepository = enderecoRepository;
        this.passwordEncoder = passwordEncoder;
        this.setorRepository = setorRepository;
        this.pontoAtendimentoRepository = pontoAtendimentoRepository;

    }

    // Criar gerenciador (N Secretarias : N Setores)
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

        // 3. VALIDAR GUICHÊ ÚNICO
        // O guichê é validado por SETOR, que é o local físico do atendimento
        PontoAtendimento pontoAtendimentoEntidade = null;
        if (dto.guicheId() != null) {
            // Validação de ocupação usando a Query customizada que fizemos
            boolean ocupado = gerenciadorRepository.existsByPontoAtendimentoId(dto.guicheId());
            if (ocupado) {
                throw new RuntimeException("O guichê selecionado já está ocupado por outro atendente.");
            }

            pontoAtendimentoEntidade = pontoAtendimentoRepository.findById(dto.guicheId())
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
        // Sincronização de Guichê
        if (pontoAtendimentoEntidade != null) {
            g.setPontoAtendimento(pontoAtendimentoEntidade); // Define o Objeto (Sistema Novo)
            // Se o outro sistema exigir o número na coluna Integer 'guiche' da tabela 'gerenciador':
            // g.setGuicheNumero(guicheEntidade.getNumero());
        }

        // PREENCHE A COLUNA FÍSICA (Para o outro sistema)
        g.setSecretariaPrincipal(principal);

        // RELACIONAMENTOS N:N
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

    // Atualizar atendente (N Secretarias : N Setores)
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

        // 3. VALIDAR GUICHÊ ÚNICO (Entidade Guiche)
        if (dto.guicheId() != null) {
            // Valida se o guichê já está ocupado por OUTRO atendente (IdNot)
            boolean ocupado = gerenciadorRepository.existsByPontoAtendimentoIdAndIdNot(dto.guicheId(), id);
            if (ocupado) {
                throw new RuntimeException("O Guichê selecionado já está sendo usado por outro atendente.");
            }

            PontoAtendimento pontoEntidade = pontoAtendimentoRepository.findById(dto.guicheId())
                    .orElseThrow(() -> new RuntimeException("Guichê não encontrado no cadastro auxiliar."));

            // Atualiza tanto o objeto (FK) quanto o número (Integer legado)
            g.setPontoAtendimento(pontoEntidade);
        } else {
            g.setPontoAtendimento(null);
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

        // 5. ATUALIZAÇÃO DOS VÍNCULOS N:N
        g.getSecretarias().clear();
        g.getSecretarias().addAll(secretariasEncontradas);

        g.getSetores().clear();
        g.getSetores().addAll(setoresEncontrados);

        return gerenciadorRepository.save(g);
    }

    // Listar todos
    public List<Gerenciador> listarTodos() {
        return gerenciadorRepository.findAll();
    }

    // Listar por secretaria
    public List<Gerenciador> listarPorSecretaria(Long secretariaId) {
        // Mudou de findBySecretariaId para findBySecretarias_Id
        return gerenciadorRepository.findBySecretarias_Id(secretariaId);
    }

    // Buscar por ID
    public Gerenciador buscarPorId(Long id) {
        return gerenciadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));
    }

    // Remover
    public void remover(Long id) {
        Gerenciador at = gerenciadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));
        gerenciadorRepository.delete(at);
    }

    @Transactional
    public Gerenciador atualizarGuiche(Long id, Long pontoAtendimentoId) { // 🟢 Agora recebe Long guicheId

        Gerenciador g = gerenciadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gerenciador não encontrado"));

        Long usuarioLogadoId = UsuarioLogadoUtil.getUsuarioId();
        if (usuarioLogadoId == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        Gerenciador usuarioLogado = gerenciadorRepository.findById(usuarioLogadoId)
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado"));

        String perfil = usuarioLogado.getPerfil();
        boolean ehOProprio = usuarioLogado.getId().equals(id);

        // ✔ Validação de Perfil
        if (!"ADMIN".equals(perfil)) {
            // Se não for o próprio, já barra aqui (segurança)
            if (!ehOProprio) {
                throw new RuntimeException("Você não tem permissão para alterar o local de outro usuário.");
            }

            // Verifica se o perfil está na lista dos permitidos para auto-alteração
            if (!"ATENDENTE".equals(perfil) && !"MEDICO".equals(perfil) && !"TRIAGEM".equals(perfil) && !"CADASTRO".equals(perfil)) {
                throw new RuntimeException("Seu perfil (" + perfil + ") não tem permissão para selecionar local de trabalho.");
            }
        }

        // Variável para guardar o nome da secretaria selecionada para o log
        String nomeSecretariaLog = "Nenhum";

        if (pontoAtendimentoId != null) {
            // 1. Busca a entidade do Guichê
            PontoAtendimento pontoEntidade = pontoAtendimentoRepository.findById(pontoAtendimentoId)
                    .orElseThrow(() -> new RuntimeException("Guichê não encontrado no cadastro."));

            // 2. VERIFICAÇÃO: Se algum gerenciador já possui este guichê (e não é o próprio usuário atual)
            gerenciadorRepository.findByPontoAtendimentoId(pontoAtendimentoId).ifPresent(ocupante -> {
                if (!ocupante.getId().equals(id)) {
                    // Se encontrou alguém e não é o próprio 'g', lança a exceção
                    throw new RuntimeException("Guichê sendo usado, escolha outro. (Ocupante: " + ocupante.getNome() + ")");
                }
            });

            // CAPTURA A SECRETARIA ESPECÍFICA DO GUICHÊ
            if (pontoEntidade.getSetor() != null && pontoEntidade.getSetor().getSecretaria() != null) {
                nomeSecretariaLog = pontoEntidade.getSetor().getSecretaria().getNome();
            }

            // 3. Se passou pela verificação, atribui o guichê
            g.setPontoAtendimento(pontoEntidade);
        } else {
            g.setPontoAtendimento(null);
        }

        Gerenciador salvo = gerenciadorRepository.save(g);

        logService.registrar(
                usuarioLogado.getId(),
                usuarioLogado.getPerfil(),
                "GUICHE_ALTERADO",
                "Atendente ID: " + salvo.getId() +
                        ", Nome: " + salvo.getNome() +
                        ", Secretaria: " + nomeSecretariaLog + // <--- Aqui entra a secretaria única
                        ", Novo Guichê: " + (salvo.getPontoAtendimento() != null ? salvo.getPontoAtendimento().getNumero() : "Nenhum")
        );

        return salvo;
    }

    @Transactional
    public void deslogarGuiche(Long id) {
        Gerenciador g = gerenciadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gerenciador não encontrado"));

        // Pegamos os dados ANTES de limpar a FK
        String infoAtendimento = "Sem guichê vinculado";

        if (g.getPontoAtendimento() != null) {
            PontoAtendimento pontoAtivo = g.getPontoAtendimento();
            String secretaria = (pontoAtivo.getSetor() != null && pontoAtivo.getSetor().getSecretaria() != null)
                    ? pontoAtivo.getSetor().getSecretaria().getNome()
                    : "N/A";

            infoAtendimento = String.format("Secretaria: %s | Setor: %s | Guichê: %s",
                    secretaria,
                    pontoAtivo.getSetor().getNome(),
                    pontoAtivo.getNumero());
        }

        gerenciadorRepository.desvincularPontoAtendimento(id);

        logService.registrar(
                id,
                g.getPerfil(),
                "GUICHE_LOGOUT",
                "Atendente: " + g.getNome() + " | " + infoAtendimento
        );
    }
}
