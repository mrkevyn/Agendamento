package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.*;
import com.gov.ma.saoluis.agendamento.model.*;
import com.gov.ma.saoluis.agendamento.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.gov.ma.saoluis.agendamento.service.SlotAtendimentoService;
import com.gov.ma.saoluis.agendamento.repository.EnderecoRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@Service
public class AgendamentoService {

    private final GerenciadorRepository atendenteRepository;

    private final AgendamentoRepository agendamentoRepository;

    private final LogService logService;

    private final ConfiguracaoAtendimentoService configuracaoService;

    private final ChamadaAgendamentoRepository chamadaAgendamentoRepository;

    private final HorarioAtendimentoRepository horarioRepository;

    private final ServicoService servicoService;

    private final UsuarioService usuarioService;

    private final SlotAtendimentoService slotAtendimentoService;

    private final SlotAtendimentoRepository slotAtendimentoRepository;

    private final SetorRepository setorRepository;

    private final EnderecoRepository enderecoRepository;

    private final ServicoRepository servicoRepository;

    private TipoAtendimentoRepository tipoAtendimentoRepository;
    private ServicoSaudeRepository servicoSaudeRepository;

    private static final ZoneId ZONE_SLZ = ZoneId.of("America/Fortaleza");

    public AgendamentoService(GerenciadorRepository gerenciadorRepository, AgendamentoRepository agendamentoRepository, LogService logService, ConfiguracaoAtendimentoService configuracaoService, ChamadaAgendamentoRepository chamadaAgendamentoRepository, HorarioAtendimentoRepository horarioRepository, ServicoService servicoService, UsuarioService usuarioService, SlotAtendimentoService slotAtendimentoService, SlotAtendimentoRepository slotAtendimentoRepository, EnderecoRepository enderecoRepository, SetorRepository setorRepository, ServicoRepository servicoRepository, TipoAtendimentoRepository tipoAtendimentoRepository, ServicoSaudeRepository servicoSaudeRepository) {
        this.atendenteRepository = gerenciadorRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.logService = logService;
        this.configuracaoService = configuracaoService;
        this.chamadaAgendamentoRepository = chamadaAgendamentoRepository;
        this.horarioRepository = horarioRepository;
        this.servicoService = servicoService;
        this.usuarioService = usuarioService;
        this.slotAtendimentoService = slotAtendimentoService;
        this.slotAtendimentoRepository = slotAtendimentoRepository;
        this.enderecoRepository = enderecoRepository;
        this.setorRepository = setorRepository;
        this.servicoRepository = servicoRepository;
        this.tipoAtendimentoRepository = tipoAtendimentoRepository;
        this.servicoSaudeRepository = servicoSaudeRepository;
    }

    public List<AgendamentoDTO> listarPorSetorGerenciador(Long setorId) {
        // 1. Busca o setor para verificar a secretaria
        Setor setor = setorRepository.findById(setorId)
                .orElseThrow(() -> new RuntimeException("Setor não encontrado"));

        // 2. Define se é Hospital (Regra 24h e Serviços de Saúde)
        // Usamos o nome da secretaria para decidir
        String nomeSec = setor.getSecretaria().getNome().toUpperCase();
        boolean isHospital = nomeSec.contains("SAÚDE") || nomeSec.contains("SAUDE");

        // 3. Pega a hora local para a regra administrativa de "passou do horário"
        LocalDateTime horaLocal = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
        Timestamp agoraSql = Timestamp.valueOf(horaLocal);

        // 4. Chama a Query com os 3 parâmetros
        return agendamentoRepository.buscarAgendamentosPorSetor(setorId, agoraSql, isHospital);
    }

    // 🔹 Buscar todos os agendamentos com detalhes
    public List<AgendamentoDTO> listarTodosComDetalhes() {
        return agendamentoRepository.buscarTodosAgendamentosComDetalhes();
    }

    // 🔹 Buscar por ID
    public Agendamento buscarPorId(Long id) {
        return agendamentoRepository.findByIdNativo(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
    }

    // 🔹 Criar novo agendamento
    @Transactional
    public Agendamento salvarApp(AgendamentoAppRequest req) {

        Usuario usuario = usuarioService.buscarPorId(req.usuarioId());
        Servico servico = servicoService.buscarPorId(req.servicoId());

        ConfiguracaoAtendimento cfg =
                configuracaoService.buscarPorSetorId(req.setorId());

        // 1) valida se dia/hora é permitido pra secretaria
        configuracaoService.validarDisponibilidade(
                cfg.getSetor().getId(),
                req.data(),
                req.hora()
        );

        // 2) garante slots do dia (cria se não existir)
        slotAtendimentoService.garantirSlotsDoDia(cfg, req.data());

        // 3) lock no slot específico
        SlotAtendimento slot = slotAtendimentoRepository.lockSlot(
                cfg.getId(),
                req.data(),
                req.hora()
        ).orElseThrow(() -> new RuntimeException("Horário indisponível"));

        // 4) verifica vagas
        if (!slot.temVaga()) {
            throw new RuntimeException("Horário lotado");
        }

        // 5) reserva (incrementa)
        slot.setReservados(slot.getReservados() + 1);
        slotAtendimentoRepository.save(slot);

        // opcional: lotou -> marca template ocupado
        if (slot.getReservados() >= slot.getCapacidade()) {
            cfg.getHorarios().stream()
                    .filter(h -> h.getHora().equals(req.hora()))
                    .findFirst()
                    .ifPresent(h -> h.setOcupado(true));
            // 💡 se quiser persistir isso, faça cfgRepo.save(cfg) ou garanta cascade/dirty checking
        }

        // 6) cria agendamento
        Agendamento agendamento = new Agendamento();
        agendamento.setUsuario(usuario);
        agendamento.setServico(servico);
        agendamento.setSetor(cfg.getSetor());

        // 🟢 NOVO: Pega a secretaria através do setor (ou serviço) para usar nas validações e já salva no agendamento
        Secretaria secretaria = cfg.getSetor().getSecretaria();
        agendamento.setSecretaria(secretaria);

        agendamento.setHoraAgendamento(
                java.time.LocalDateTime.of(req.data(), req.hora())
        );

        agendamento.setTipoAgendamento(TipoAgendamento.AGENDADO);

        agendamento.setSituacao(SituacaoAgendamento.AGENDADO);

        TipoAtendimento tipoAtendimento;
        if (req.tipoAtendimentoId() != null) {
            tipoAtendimento = tipoAtendimentoRepository.findById(req.tipoAtendimentoId())
                    .orElseThrow(() -> new RuntimeException("Tipo de atendimento não encontrado"));

            // 🟢 VALIDAÇÃO DE SEGURANÇA: Garante que o tipo escolhido pertence à secretaria do agendamento
            if (!tipoAtendimento.getSecretaria().getId().equals(secretaria.getId())) {
                throw new RuntimeException("O tipo de atendimento escolhido não pertence a esta secretaria.");
            }
        } else {
            // 🟢 MUDANÇA AQUI: Busca o tipo padrão "NORMAL" cadastrado APENAS para esta secretaria
            tipoAtendimento = tipoAtendimentoRepository.findByNomeAndSecretaria_Id("NORMAL", secretaria.getId())
                    .orElseThrow(() -> new RuntimeException("Tipo de atendimento padrão não configurado para esta secretaria"));
        }

        agendamento.setTipoAtendimento(tipoAtendimento);

        int tentativas = 0;
        while (true) {
            tentativas++;

            agendamento.setSenha(
                    gerarSenhaParaDia(req.setorId(), agendamento.getTipoAtendimento(), req.data())
            );

            try {
                return agendamentoRepository.save(agendamento);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                if (tentativas >= 5) {
                    throw new RuntimeException("Falha ao gerar senha única");
                }
                // tenta de novo
            }
        }
    }

    @Autowired
    private EmailService emailService;

    // 🔹 Criar novo agendamento pelo app externo (sem login)
    @Transactional
    public Agendamento salvarExterno(AgendamentoExternoRequest req) {

        if (req.email() == null || req.email().isBlank()) {
            throw new RuntimeException("Email é obrigatório");
        }

        Servico servico = servicoService.buscarPorId(req.servicoId());

        // 🔥 AGORA BUSCA CONFIGURAÇÃO PELO SETOR
        ConfiguracaoAtendimento cfg =
                configuracaoService.buscarPorSetorId(req.setorId());

        configuracaoService.validarDisponibilidade(
                req.setorId(),
                req.data(),
                req.hora()
        );

        slotAtendimentoService.garantirSlotsDoDia(cfg, req.data());

        SlotAtendimento slot = slotAtendimentoRepository.lockSlot(
                cfg.getId(),
                req.data(),
                req.hora()
        ).orElseThrow(() -> new RuntimeException("Horário indisponível"));

        if (!slot.temVaga()) {
            throw new RuntimeException("Horário lotado");
        }

        slot.setReservados(slot.getReservados() + 1);
        slotAtendimentoRepository.save(slot);

        Agendamento agendamento = new Agendamento();

        agendamento.setServico(servico);
        agendamento.setSetor(cfg.getSetor());

        agendamento.setNomeCidadao(req.nome());
        agendamento.setCpf(req.cpf());
        agendamento.setDataNascimento(req.dataNascimento());
        agendamento.setCelular(req.celular());
        agendamento.setEmail(req.email());

        // 🟢 NOVO: Pega a secretaria através do setor (ou serviço) para usar nas validações e já salva no agendamento
        Secretaria secretaria = cfg.getSetor().getSecretaria();
        agendamento.setSecretaria(secretaria);

        agendamento.setHoraAgendamento(
                LocalDateTime.of(req.data(), req.hora())
        );

        agendamento.setTipoAgendamento(TipoAgendamento.AGENDADO);
        agendamento.setSituacao(SituacaoAgendamento.AGENDADO);

        TipoAtendimento tipoAtendimento;
        if (req.tipoAtendimentoId() != null) {
            tipoAtendimento = tipoAtendimentoRepository.findById(req.tipoAtendimentoId())
                    .orElseThrow(() -> new RuntimeException("Tipo de atendimento não encontrado"));

            // 🟢 VALIDAÇÃO DE SEGURANÇA: Garante que o tipo escolhido pertence à secretaria do agendamento
            if (!tipoAtendimento.getSecretaria().getId().equals(secretaria.getId())) {
                throw new RuntimeException("O tipo de atendimento escolhido não pertence a esta secretaria.");
            }
        } else {
            // 🟢 MUDANÇA AQUI: Busca o tipo padrão "NORMAL" cadastrado APENAS para esta secretaria
            tipoAtendimento = tipoAtendimentoRepository.findByNomeAndSecretaria_Id("NORMAL", secretaria.getId())
                    .orElseThrow(() -> new RuntimeException("Tipo de atendimento padrão não configurado para esta secretaria"));
        }

        agendamento.setTipoAtendimento(tipoAtendimento);

        int tentativas = 0;
        while (true) {
            tentativas++;

            agendamento.setSenha(
                    gerarSenhaParaDia(
                            req.setorId(),
                            agendamento.getTipoAtendimento(),
                            req.data()
                    )
            );

            try {
                // 1. Salva o agendamento no banco
                Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);

                // 2. Dispara o e-mail (O método deve ser @Async para não travar a resposta)
                try {
                    emailService.enviarEmailConfirmacao(agendamentoSalvo);
                } catch (Exception e) {
                    // Logamos o erro do e-mail mas não impedimos o retorno do sucesso do agendamento
                    System.err.println("Erro ao disparar e-mail: " + e.getMessage());
                }

                return agendamentoSalvo;

            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // Se houver duplicidade de senha, ele tenta novamente até 5 vezes
                if (tentativas >= 5) {
                    throw new RuntimeException("Falha ao gerar senha única após 5 tentativas");
                }
            }
        }
    }

    private String gerarSenhaParaDia(Long setorId, TipoAtendimento tipoAtendimento, LocalDate data) {
        // Pega a sigla (Ex: "P") e o nome (Ex: "PRIORIDADE") direto do banco
        String sigla = tipoAtendimento.getSigla();
        String nomeTipo = tipoAtendimento.getNome();

        // Busca no banco a última senha gerada para esse nome de tipo hoje
        String ultima = agendamentoRepository.findUltimaSenhaDoDia(setorId, sigla, data);

        // Se não houver nenhuma, cria a número 001 usando a sigla do banco
        if (ultima == null || ultima.isBlank()) {
            return sigla + "001";
        }

        // Se já tiver, passa para a função de incremento
        return gerarProximaSenha(ultima, sigla);
    }

    @Transactional
    public Agendamento criarEspontaneo(Long secretariaId, AgendamentoEspontaneoDTO dto) {
        if (secretariaId == null) throw new RuntimeException("Secretaria é obrigatória");

        // 1. Validar e Buscar Setor
        Setor setor = setorRepository.findById(dto.setorId())
                .orElseThrow(() -> new RuntimeException("Setor não encontrado"));

        Secretaria secretariaDoSetor = setor.getSecretaria();

        // 🟢 IDENTIFICAÇÃO DE REGRA: É Hospital/Saúde?
        boolean isHospital = secretariaDoSetor.getNome().toUpperCase().contains("SAÚDE")
                || secretariaDoSetor.getNome().toUpperCase().contains("SAUDE");

        // 2. Validar Serviço
        Long sId = dto.servicoId();
        if (sId == null) throw new RuntimeException("Serviço é obrigatório");

        // 3. INSTANCIAR a Entidade Agendamento
        Agendamento agendamento = new Agendamento();
        agendamento.setNomeCidadao(dto.nomeCidadao());
        agendamento.setSetor(setor);
        agendamento.setSecretaria(secretariaDoSetor);
        agendamento.setObservacao(dto.observacao());
        agendamento.setSituacao(SituacaoAgendamento.AGENDADO);
        agendamento.setTipoAgendamento(TipoAgendamento.ESPONTANEO);
        agendamento.setHoraAgendamento(LocalDateTime.now(ZONE_SLZ));

        // 🟢 LÓGICA DE SALVAMENTO HÍBRIDA (Resolve o erro de servico_id nulo)
        if (isHospital) {
            // Se for Saúde, valida e salva na NOVA coluna servicoSaude
            ServicoSaude ss = servicoSaudeRepository.findById(sId)
                    .orElseThrow(() -> new RuntimeException("Serviço hospitalar não encontrado"));

            if (!servicoSaudeRepository.existsByIdAndSetores_Id(sId, setor.getId())) {
                throw new RuntimeException("Este serviço de saúde não pertence a este setor");
            }

            agendamento.setServicoSaude(ss); // Salva na coluna servico_saude_id
            agendamento.setServico(null);     // Deixa a coluna servico_id (administrativa) vazia
        } else {
            // Se for Administrativo, valida e salva na coluna ANTIGA servico
            Servico s = servicoRepository.findById(sId)
                    .orElseThrow(() -> new RuntimeException("Serviço administrativo não encontrado"));

            if (!servicoRepository.existsByIdAndSetores_Id(sId, setor.getId())) {
                throw new RuntimeException("Este serviço não pertence ao setor informado");
            }

            agendamento.setServico(s);       // Salva na coluna servico_id
            agendamento.setServicoSaude(null); // Deixa a coluna de saúde vazia
        }

        // 4. Tipo de Atendimento (Mantendo sua regra de segurança)
        TipoAtendimento tipoAtendimento;
        if (dto.tipoAtendimentoId() != null) {
            tipoAtendimento = tipoAtendimentoRepository.findById(dto.tipoAtendimentoId())
                    .orElseThrow(() -> new RuntimeException("Tipo de atendimento não encontrado"));

            if (!tipoAtendimento.getSecretaria().getId().equals(secretariaDoSetor.getId())) {
                throw new RuntimeException("O tipo de atendimento não pertence à secretaria deste setor.");
            }
        } else {
            tipoAtendimento = tipoAtendimentoRepository.findByNomeAndSecretaria_Id("NORMAL", secretariaDoSetor.getId())
                    .orElseThrow(() -> new RuntimeException("Tipo 'NORMAL' não configurado para esta secretaria"));
        }
        agendamento.setTipoAtendimento(tipoAtendimento);

        // 4. Geração de Senha
        LocalDate hoje = LocalDate.now(ZONE_SLZ);
        Long setorId = setor.getId();

        int tentativas = 0;
        while (true) {
            tentativas++;
            agendamento.setSenha(
                    gerarSenhaParaDia(
                            setor.getId(),
                            agendamento.getTipoAtendimento(),
                            hoje
                    )
            );
            try {
                return agendamentoRepository.save(agendamento);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                if (tentativas >= 5) throw new RuntimeException("Falha ao gerar senha única");
            }
        }
    }

    @Transactional
    public AgendamentoUpdateResponseDTO atualizarEspontaneo(Long id, AgendamentoUpdateDTO dto) {

        Agendamento ag = agendamentoRepository.findByIdNativo(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (dto.nomeCidadao() != null && !dto.nomeCidadao().isBlank()) {
            ag.setNomeCidadao(dto.nomeCidadao().trim());
        }

        if (dto.servicoId() != null) {
            Servico servico = servicoService.buscarPorId(dto.servicoId());
            ag.setServico(servico);

            if (servico.getSecretaria() != null) {
                ag.setSecretaria(servico.getSecretaria());
            }
        }

        agendamentoRepository.save(ag);

        // ✅ monta resposta sem proxy
        return new AgendamentoUpdateResponseDTO(
                ag.getId(),
                ag.getNomeCidadao(),
                ag.getServico() != null ? ag.getServico().getId() : null,
                ag.getServico() != null ? ag.getServico().getNome() : null,
                ag.getSetor() != null ? ag.getSetor().getId() : null,
                ag.getSenha(),
                ag.getSituacao() != null ? ag.getSituacao().name() : null,
                ag.getTipoAtendimento() != null ? ag.getTipoAtendimento().getNome() : null
        );
    }

    // 🔹 Atualizar (reagendar)
    public Agendamento atualizar(Long id, Agendamento novosDados) {
        Agendamento existente = buscarPorId(id);

        boolean precisaNovaSenha = false;

        // 1. Verifica se a data do agendamento mudou
        LocalDate dataAntiga = existente.getHoraAgendamento().toLocalDate();
        LocalDate dataNova = novosDados.getHoraAgendamento().toLocalDate();

        if (!dataAntiga.equals(dataNova)) {
            precisaNovaSenha = true;
        }

        existente.setHoraAgendamento(novosDados.getHoraAgendamento());

        if (novosDados.getUsuario() != null) {
            existente.setUsuario(novosDados.getUsuario());
        }

        if (novosDados.getServico() != null) {
            existente.setServico(novosDados.getServico());
        }

        // 2. Verifica se o tipo de atendimento mudou
        if (novosDados.getTipoAtendimento() != null) {
            // Compara os IDs para saber se é um tipo diferente do atual
            if (!existente.getTipoAtendimento().getId().equals(novosDados.getTipoAtendimento().getId())) {
                precisaNovaSenha = true;
            }
            existente.setTipoAtendimento(novosDados.getTipoAtendimento());
        }

        // 3. Se mudou a fila (data ou tipo), gera a senha correta buscando no banco
        if (precisaNovaSenha) {
            String novaSenha = gerarSenhaParaDia(
                    existente.getSetor().getId(),
                    existente.getTipoAtendimento(),
                    dataNova
            );
            existente.setSenha(novaSenha);
        }

        existente.setSituacao(SituacaoAgendamento.REAGENDADO);

        return agendamentoRepository.save(existente);
    }

    // 🔹 Deletar
    public void deletar(Long id) {
        agendamentoRepository.deleteById(id);
    }

    // 🔹 Gerar prefixo da senha com base no tipo
    private String gerarPrefixo(String tipo) {
        tipo = tipo.toUpperCase();
        return switch (tipo) {
            case "PRIORIDADE" -> "P";
            case "PREFERENCIAL" -> "F";
            default -> "N"; // NORMAL
        };
    }

    // 🔹 Incrementar senha anterior (ex: N001 → N002)
    private String gerarProximaSenha(String senhaAntiga, String siglaFallback) {
        if (senhaAntiga == null || senhaAntiga.length() < 2) {
            return siglaFallback + "001";
        }

        String prefixo = senhaAntiga.substring(0, 1);
        String numeroStr = senhaAntiga.substring(1);

        try {
            int numero = Integer.parseInt(numeroStr);
            return String.format("%s%03d", prefixo, numero + 1);
        } catch (NumberFormatException e) {
            // Se por acaso alguém salvou "TESTE" na coluna de senha no banco,
            // ele recomeça do zero com a sigla correta em vez de quebrar a API.
            return siglaFallback + "001";
        }
    }

    public List<AgendamentoDTO> listarAgendamentosComDetalhes(Long agendamentoId) {
        return agendamentoRepository.buscarAgendamentosComDetalhes(agendamentoId);
    }

    private void verificarAtendenteOcupado(Long gerenciadorId) {
        List<String> statusOcupados = Arrays.asList("EM_ATENDIMENTO");
        boolean ocupado = agendamentoRepository.existsByGerenciadorIdAndSituacaoIn(gerenciadorId, statusOcupados);

        if (ocupado) {
            throw new RuntimeException("Atendente ocupado");
        }
    }

    @Transactional
    public Agendamento chamarProximaNormal(Long setorId, Long atendenteId) {

        verificarAtendenteOcupado(atendenteId);

        Gerenciador gerenciador = atendenteRepository.findById(atendenteId)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

        // Validação de vínculo...

        LocalDateTime agora = LocalDateTime.now(ZONE_SLZ);
        LocalDateTime inicio = agora.toLocalDate().atStartOfDay();
        LocalDateTime fim = inicio.plusDays(1); // Fim do dia (Meia-noite do dia seguinte)

        var lista = agendamentoRepository.buscarProximoNormalHoje(
                setorId,
                inicio,
                agora,
                fim,
                PageRequest.of(0, 1)
        );

        if (lista.isEmpty()) {
            throw new RuntimeException("Fila vazia");
        }

        return processarChamada(lista.get(0), gerenciador);
    }

    @Transactional
    public Agendamento chamarProximaPrioridade(Long setorId, Long atendenteId) {

        verificarAtendenteOcupado(atendenteId);

        Gerenciador gerenciador = atendenteRepository.findById(atendenteId)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

        boolean vinculado = gerenciador.getSetores().stream().anyMatch(s -> s.getId().equals(setorId));
        if (!vinculado) {
            throw new RuntimeException("Gerenciador não pertence a este setor");
        }

        // 🟢 Pegamos os 3 tempos (Início do dia, Agora, e Fim do dia)
        LocalDateTime agora = LocalDateTime.now(ZONE_SLZ);
        LocalDateTime inicio = agora.toLocalDate().atStartOfDay();
        LocalDateTime fim = inicio.plusDays(1);

        var lista = agendamentoRepository.buscarProximoPrioridadeHoje(
                setorId, inicio, agora, fim, PageRequest.of(0, 1)
        );

        if (lista.isEmpty()) {
            throw new RuntimeException("Fila vazia");
        }

        return processarChamada(lista.get(0), gerenciador);
    }

    public AgendamentoResponseDTO chamarPorSenha(String senha, Long atendenteId, Long setorId) throws Exception {

        // Vamos checar isso SÓ DEPOIS de saber qual senha ele está chamando.

        Gerenciador gerenciador = atendenteRepository.findById(atendenteId)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

        // ✅ Valida se o atendente pode atuar no setor informado
        boolean vinculado = gerenciador.getSetores().stream()
                .anyMatch(s -> s.getId().equals(setorId));

        if (!vinculado) {
            throw new RuntimeException("Atendente não possui vínculo com o setor informado");
        }

        LocalDate hoje = LocalDate.now(ZONE_SLZ);
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

        Pageable pageable = PageRequest.of(0, 1);

        // ✅ Busca por senha dentro do setor específico
        List<Agendamento> agendamentos = agendamentoRepository.buscarPorSenhaHoje(
                setorId, senha, inicio, fim, pageable
        );

        if (agendamentos.isEmpty()) {
            throw new Exception("Agendamento não encontrado para a senha " + senha + " neste setor (hoje)");
        }

        Agendamento agendamento = agendamentos.get(0);

        // 🟢 NOVA LÓGICA: Permite "Re-chamar" a mesma senha!
        // Descobre se a senha clicada é a que o atendente já está atendendo agora.
        boolean ehAMinhaSenhaAtual = (agendamento.getSituacao() == SituacaoAgendamento.EM_ATENDIMENTO) &&
                (agendamento.getAtendente() != null) &&
                (agendamento.getAtendente().getId().equals(atendenteId));

        // Só barra o atendente se ele tentar puxar um paciente NOVO (AGENDADO)
        // ou a senha de outra pessoa, enquanto ele ainda tem um atendimento em aberto.
        if (!ehAMinhaSenhaAtual) {
            verificarAtendenteOcupado(atendenteId);
        }

        // 🚩 TRAVA DE SEGURANÇA: Impede que OUTRO guichê "roube" o atendimento de um colega
        if (agendamento.getSituacao() == SituacaoAgendamento.EM_ATENDIMENTO) {
            if (agendamento.getAtendente() != null && !agendamento.getAtendente().getId().equals(atendenteId)) {
                throw new RuntimeException("Esta senha já está sendo atendida no Guichê "
                        + agendamento.getAtendente().getGuiche());
            }
        }

        // 🔹 Processa a chamada
        agendamento = processarChamada(agendamento, gerenciador);

        return new AgendamentoResponseDTO(
                agendamento.getId(),
                agendamento.getHoraAgendamento(),
                agendamento.getSituacao(),
                agendamento.getSenha(),
                agendamento.getTipoAtendimento().getNome(),
                agendamento.getUsuario() != null ? agendamento.getUsuario().getId() : null,
                agendamento.getUsuario() != null ? agendamento.getUsuario().getNome() : null,
                agendamento.getServico() != null ? agendamento.getServico().getId() : null,
                agendamento.getServico() != null ? agendamento.getServico().getNome() : null
        );
    }

    // 🔹 Lógica comum para registrar chamada
    private Agendamento processarChamada(Agendamento agendamento, Gerenciador gerenciador) {

        if (agendamento == null) {
            throw new RuntimeException("Nenhuma senha disponível para chamar.");
        }

        agendamento.setSituacao(SituacaoAgendamento.EM_ATENDIMENTO);
        agendamento.setHoraChamada(LocalDateTime.now());
        agendamento.setAtendente(gerenciador);

        Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);

        LocalDate hoje = LocalDate.now(ZONE_SLZ);
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

        Integer numeroGuiche = (gerenciador.getGuiche() != null)
                ? gerenciador.getGuiche().getNumero()
                : null;

        // 🔹 Verifica se já existe chamada para essa senha hoje
        List<ChamadaAgendamento> chamadasExistentes = chamadaAgendamentoRepository
                .findByAgendamentoAndDataChamadaBetween(agendamentoSalvo, inicio, fim);

        ChamadaAgendamento chamada;
        if (!chamadasExistentes.isEmpty()) {
            // ✅ já existe → atualiza apenas data/hora
            chamada = chamadasExistentes.get(0);
            chamada.setDataChamada(LocalDateTime.now());
            chamada.setGerenciador(gerenciador);
            chamada.setGuiche(numeroGuiche);
            chamada.setSetor(agendamentoSalvo.getSetor());
        } else {
            // ✅ não existe → cria nova chamada
            chamada = new ChamadaAgendamento();
            chamada.setAgendamento(agendamentoSalvo);
            chamada.setGerenciador(gerenciador);
            chamada.setSetor(agendamentoSalvo.getSetor());
            chamada.setSecretaria(
                    agendamentoSalvo.getServico() != null
                            ? agendamentoSalvo.getServico().getSecretaria()
                            : null
            );
            chamada.setSenha(agendamentoSalvo.getSenha());
            chamada.setTipoAtendimento(agendamentoSalvo.getTipoAtendimento().getNome());
            chamada.setGuiche(numeroGuiche);
            chamada.setDataChamada(LocalDateTime.now());
        }

        chamadaAgendamentoRepository.save(chamada);

        return agendamentoSalvo;
    }

    public List<UltimaChamadaDTO> getUltimasChamadasPorSetor(Long setorId) {

        LocalDate hoje = LocalDate.now(ZONE_SLZ);
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

        return chamadaAgendamentoRepository
                .buscarUltimasChamadasPorSetorEHorario(setorId, inicio, fim);
    }

    // 🔹 Finalizar atendimento
    @Transactional
    public Agendamento finalizarAtendimento(Long id) {

        Agendamento agendamento = buscarPorId(id);

        // Validação de segurança
        if (agendamento.getSituacao() != SituacaoAgendamento.EM_ATENDIMENTO) {
            throw new RuntimeException("Este agendamento não está em atendimento.");
        }

        // 🟢 Define a situação e carimba o horário de término com o fuso correto
        agendamento.setSituacao(SituacaoAgendamento.ATENDIDO);
        agendamento.setHoraFinalizado(LocalDateTime.now(ZONE_SLZ));

        return agendamentoRepository.save(agendamento);
    }

    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void finalizarAtendimentosAbandonados() {
        // 1. Use o mesmo fuso horário (ZONE_SLZ) para o cálculo do limite
        LocalDateTime limite = LocalDateTime.now(ZONE_SLZ).minusSeconds(30);

        // 2. Tente fazer o update diretamente no banco (mais eficiente)
        // Se preferir manter a lógica atual para disparar eventos de JPA, corrija apenas o fuso:
        List<Agendamento> abandonados = agendamentoRepository.findBySituacaoInAndUltimoPingBefore(
                List.of(SituacaoAgendamento.EM_ATENDIMENTO), limite
        );

        if (!abandonados.isEmpty()) {
            for (Agendamento ag : abandonados) {
                ag.setSituacao(SituacaoAgendamento.ATENDIDO);
                ag.setHoraFinalizado(LocalDateTime.now(ZONE_SLZ));
                // No @Transactional, o save() dentro do loop é opcional se o objeto for 'managed'
                // mas ajuda na legibilidade.
                agendamentoRepository.save(ag);
            }
        }
    }

    // 🔹 Cancelar atendimento (não compareceu)
    public Agendamento cancelarAtendimento(Long id) {

        Agendamento agendamento = buscarPorId(id);

        if (agendamento.getSituacao() != SituacaoAgendamento.EM_ATENDIMENTO) {
            throw new RuntimeException("Este agendamento não está em atendimento.");
        }

        agendamento.setSituacao(SituacaoAgendamento.FALTOU);
        agendamento.setHoraChamada(LocalDateTime.now(ZONE_SLZ));

        return agendamentoRepository.save(agendamento);
    }

    public List<HistoricoDTO> buscarHistorico(String cpf) {
        String cpfLimpo = cpf.replaceAll("\\D", ""); // Tira a máscara

        List<Agendamento> agendamentos = agendamentoRepository.buscarHistoricoPorCpf(cpfLimpo);

        // Converte a entidade pesada para o DTO leve
        return agendamentos.stream().map(a -> new HistoricoDTO(
                a.getId(),
                a.getSenha(),
                a.getSituacao().name(), // Ex: "AGUARDANDO", "FINALIZADO"
                a.getHoraAgendamento(),
                a.getServico() != null ? a.getServico().getNome() : "Serviço não informado",
                a.getSetor() != null ? a.getSetor().getNome() : "Setor não informado"
        )).toList();
    }

    @Transactional
    public void atualizarPing(Long id) {
        Agendamento agendamento = buscarPorId(id);

        // 🚨 IMPORTANTE: Use o mesmo ZONE_SLZ que você usa no Scheduler
        agendamento.setUltimoPing(LocalDateTime.now(ZONE_SLZ));

        // O Spring Data JPA salva automaticamente ao final do @Transactional,
        // mas você pode forçar o save se preferir.
        agendamentoRepository.save(agendamento);
    }
}
