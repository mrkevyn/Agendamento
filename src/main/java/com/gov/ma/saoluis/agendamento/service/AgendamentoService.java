package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.*;
import com.gov.ma.saoluis.agendamento.model.*;
import com.gov.ma.saoluis.agendamento.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.gov.ma.saoluis.agendamento.service.SlotAtendimentoService;
import com.gov.ma.saoluis.agendamento.repository.EnderecoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

    public AgendamentoService(GerenciadorRepository gerenciadorRepository, AgendamentoRepository agendamentoRepository, LogService logService, ConfiguracaoAtendimentoService configuracaoService, ChamadaAgendamentoRepository chamadaAgendamentoRepository, HorarioAtendimentoRepository horarioRepository, ServicoService servicoService, UsuarioService usuarioService, SlotAtendimentoService slotAtendimentoService, SlotAtendimentoRepository slotAtendimentoRepository, EnderecoRepository enderecoRepository, SetorRepository setorRepository) {
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
    }

    // 🔹 Listar todos COM DETALHES
    public List<AgendamentoDTO> listarPorSetorGerenciador(Long setorId) {
        return agendamentoRepository.buscarAgendamentosPorSetor(setorId);
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

        ConfiguracaoAtendimento cfg = configuracaoService.buscarPorId(req.configuracaoId());

        // 1) valida se dia/hora é permitido pra secretaria
        configuracaoService.validarDisponibilidade(
                cfg.getSecretaria().getId(),
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
        agendamento.setConfiguracao(cfg);

        agendamento.setHoraAgendamento(
                java.time.LocalDateTime.of(req.data(), req.hora())
        );

        agendamento.setTipoAgendamento(TipoAgendamento.AGENDADO);

        agendamento.setSituacao(SituacaoAgendamento.AGENDADO);

        agendamento.setTipoAtendimento(
                req.tipoAtendimento() == null ? "NORMAL" : req.tipoAtendimento()
        );

        int tentativas = 0;
        while (true) {
            tentativas++;

            agendamento.setSenha(
                    gerarSenhaParaDia(cfg.getId(), agendamento.getTipoAtendimento(), req.data())
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

    private String gerarSenhaParaDiaEspontaneo(Long setorId, String tipo, LocalDate data) {
        String prefixo = gerarPrefixo(tipo);

        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay();

        String ultima = agendamentoRepository.findUltimaSenhaDoDiaParaEspontaneoPorEndereco(
                setorId, tipo, inicio, fim, PageRequest.of(0, 1)
        ).stream().findFirst().orElse(null);

        if (ultima == null) return prefixo + "001";

        return gerarProximaSenha(ultima);
    }

    private String gerarSenhaParaDia(Long configId, String tipo, LocalDate data) {
        String prefixo = gerarPrefixo(tipo);

        String ultima = agendamentoRepository.findUltimaSenhaDoDia(configId, tipo, data);

        if (ultima == null) return prefixo + "001";

        return gerarProximaSenha(ultima);
    }

    private void validarAgendamentoEspontaneo(Agendamento agendamento) {

        if (agendamento.getServico() == null) {
            throw new RuntimeException("Serviço é obrigatório");
        }

        if (agendamento.getNomeCidadao() == null || agendamento.getNomeCidadao().isBlank()) {
            throw new RuntimeException("Nome do cidadão é obrigatório para atendimento espontâneo");
        }

        if (agendamento.getTipoAtendimento() == null || agendamento.getTipoAtendimento().isBlank()) {
            throw new RuntimeException("Tipo de atendimento é obrigatório");
        }
    }

    @Transactional
    public Agendamento criarEspontaneo(Long secretariaId, AgendamentoEspontaneoDTO dto) { // 🟢 Alterado de Agendamento para o DTO
        if (secretariaId == null) throw new RuntimeException("Secretaria é obrigatória");

        // 1. Validar e Buscar Setor usando o ID que vem do DTO
        if (dto.setorId() == null) {
            throw new RuntimeException("Setor é obrigatório");
        }

        Setor setor = setorRepository.findById(dto.setorId())
                .orElseThrow(() -> new RuntimeException("Setor não encontrado"));

        // 2. Validar Serviço usando o ID que vem do DTO
        if (dto.servicoId() == null) {
            throw new RuntimeException("Serviço é obrigatório");
        }

        Servico servico = servicoService.buscarPorId(dto.servicoId());
        if (!servico.getSecretaria().getId().equals(secretariaId)) {
            throw new RuntimeException("Serviço não pertence à secretaria informada");
        }

        // 3. INSTANCIAR a Entidade Agendamento (O banco só entende esta classe)
        Agendamento agendamento = new Agendamento();

        // Mapear dados do DTO para a Entidade
        agendamento.setNomeCidadao(dto.nomeCidadao()); // Record acessa como método: campo()
        agendamento.setSetor(setor);
        agendamento.setSecretaria(setor.getSecretaria());
        agendamento.setServico(servico);

        // Status e Horários
        agendamento.setSituacao(SituacaoAgendamento.AGENDADO);
        agendamento.setTipoAgendamento(TipoAgendamento.ESPONTANEO);

        LocalDateTime agora = LocalDateTime.now(ZoneId.of("America/Fortaleza"));
        agendamento.setHoraAgendamento(agora);

        // Normalizar Tipo Atendimento vindo do DTO
        String tipo = dto.tipoAtendimento();
        agendamento.setTipoAtendimento(tipo == null ? "NORMAL" : tipo.toUpperCase());

        // 4. Geração de Senha
        LocalDate hoje = agora.toLocalDate();
        Long setorId = setor.getId();

        int tentativas = 0;
        while (true) {
            tentativas++;
            agendamento.setSenha(gerarSenhaParaDiaEspontaneo(setorId, agendamento.getTipoAtendimento(), hoje));
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
                ag.getTipoAtendimento()
        );
    }

    // 🔹 Atualizar (reagendar)
    public Agendamento atualizar(Long id, Agendamento novosDados) {
        Agendamento existente = buscarPorId(id);

        existente.setHoraAgendamento(novosDados.getHoraAgendamento());

        if (novosDados.getUsuario() != null) {
            existente.setUsuario(novosDados.getUsuario());
        }

        if (novosDados.getServico() != null) {
            existente.setServico(novosDados.getServico());
        }

        if (novosDados.getTipoAtendimento() != null && !novosDados.getTipoAtendimento().isEmpty()) {
            existente.setTipoAtendimento(novosDados.getTipoAtendimento());
        }

        existente.setSenha(gerarProximaSenha(existente.getSenha()));
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
    private String gerarProximaSenha(String senhaAntiga) {

        if (senhaAntiga == null || senhaAntiga.length() < 2) {
            return "N001";
        }

        String prefixo = senhaAntiga.substring(0, 1);
        String numeroStr = senhaAntiga.substring(1);
        int numero = Integer.parseInt(numeroStr);

        return String.format("%s%03d", prefixo, numero + 1);
    }

    public List<AgendamentoDTO> listarAgendamentosComDetalhes(Long agendamentoId) {
        return agendamentoRepository.buscarAgendamentosComDetalhes(agendamentoId);
    }

 // 🔹 Chamar próxima senha normal
     public Agendamento chamarProximaNormal(Long setorId, Long atendenteId) {

         Gerenciador gerenciador = atendenteRepository.findById(atendenteId)
                 .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

         // 🔒 Segurança: valida se o atendente está vinculado ao setor informado
         boolean vinculado = gerenciador.getSetores().stream()
                 .anyMatch(s -> s.getId().equals(setorId));

         if (!vinculado) {
             throw new RuntimeException("Gerenciador não pertence a este setor");
         }

         LocalDate hoje = LocalDate.now();
         LocalDateTime inicio = hoje.atStartOfDay();
         LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

         // Busca no repository usando setorId
         var lista = agendamentoRepository.buscarProximoNormalHoje(
                 setorId, inicio, fim, PageRequest.of(0, 1)
         );

         Agendamento proximo = lista.isEmpty() ? null : lista.get(0);

         return processarChamada(proximo, gerenciador);
     }

    public Agendamento chamarProximaPrioridade(Long setorId, Long atendenteId) {

        Gerenciador gerenciador = atendenteRepository.findById(atendenteId)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

        // 🔒 Segurança: valida vínculo com o setor
        boolean vinculado = gerenciador.getSetores().stream()
                .anyMatch(s -> s.getId().equals(setorId));

        if (!vinculado) {
            throw new RuntimeException("Gerenciador não pertence a este setor ou não possui setor vinculado");
        }

        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

        var lista = agendamentoRepository.buscarProximoPrioridadeHoje(
                setorId, inicio, fim, PageRequest.of(0, 1)
        );

        Agendamento proximo = lista.isEmpty() ? null : lista.get(0);

        return processarChamada(proximo, gerenciador);
    }

    public AgendamentoResponseDTO chamarPorSenha(String senha, Long atendenteId, Long setorId) throws Exception {

        Gerenciador gerenciador = atendenteRepository.findById(atendenteId)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

        // ✅ Valida se o atendente pode atuar no setor informado
        boolean vinculado = gerenciador.getSetores().stream()
                .anyMatch(s -> s.getId().equals(setorId));

        if (!vinculado) {
            throw new RuntimeException("Atendente não possui vínculo com o setor informado");
        }

        LocalDate hoje = LocalDate.now();
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

        // 🔹 Processa a chamada
        agendamento = processarChamada(agendamento, gerenciador);

        return new AgendamentoResponseDTO(
                agendamento.getId(),
                agendamento.getHoraAgendamento(),
                agendamento.getSituacao(),
                agendamento.getSenha(),
                agendamento.getTipoAtendimento(),
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

        LocalDate hoje = LocalDate.now();
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
            chamada.setTipoAtendimento(agendamentoSalvo.getTipoAtendimento());
            chamada.setGuiche(numeroGuiche);
            chamada.setDataChamada(LocalDateTime.now());
        }

        chamadaAgendamentoRepository.save(chamada);

        return agendamentoSalvo;
    }

    public List<UltimaChamadaDTO> getUltimasChamadasPorSetor(Long setorId) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

        return chamadaAgendamentoRepository
                .buscarUltimasChamadasPorSetorEHorario(setorId, inicio, fim);
    }

    // 🔹 Finalizar atendimento
    public Agendamento finalizarAtendimento(Long id) {

        Agendamento agendamento = buscarPorId(id);

        if (agendamento.getSituacao() != SituacaoAgendamento.EM_ATENDIMENTO) {
            throw new RuntimeException("Este agendamento não está em atendimento.");
        }

        agendamento.setSituacao(SituacaoAgendamento.ATENDIDO);

        return agendamentoRepository.save(agendamento);
    }

    // 🔹 Cancelar atendimento (não compareceu)
    public Agendamento cancelarAtendimento(Long id) {

        Agendamento agendamento = buscarPorId(id);

        if (agendamento.getSituacao() != SituacaoAgendamento.EM_ATENDIMENTO) {
            throw new RuntimeException("Este agendamento não está em atendimento.");
        }

        agendamento.setSituacao(SituacaoAgendamento.FALTOU);
        agendamento.setHoraChamada(LocalDateTime.now());

        return agendamentoRepository.save(agendamento);
    }
}
