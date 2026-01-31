package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.AgendamentoResponseDTO;
import com.gov.ma.saoluis.agendamento.DTO.UltimaChamadaDTO;
import com.gov.ma.saoluis.agendamento.model.*;
import com.gov.ma.saoluis.agendamento.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.gov.ma.saoluis.agendamento.DTO.AgendamentoDTO;
import com.gov.ma.saoluis.agendamento.DTO.AgendamentoAppRequest;
import com.gov.ma.saoluis.agendamento.service.SlotAtendimentoService;

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

    public AgendamentoService(GerenciadorRepository gerenciadorRepository, AgendamentoRepository agendamentoRepository, LogService logService, ConfiguracaoAtendimentoService configuracaoService, ChamadaAgendamentoRepository chamadaAgendamentoRepository, HorarioAtendimentoRepository horarioRepository, ServicoService servicoService, UsuarioService usuarioService, SlotAtendimentoService slotAtendimentoService, SlotAtendimentoRepository slotAtendimentoRepository) {
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
    }

    // 🔹 Listar todos COM DETALHES
    public List<AgendamentoDTO> listarPorSecretaria(Long secretariaId) {
        return agendamentoRepository.buscarAgendamentosPorSecretaria(secretariaId);
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

    private String gerarSenhaParaDiaEspontaneo(Long secretariaId, String tipo, LocalDate data) {
        String prefixo = gerarPrefixo(tipo);

        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay();

        String ultima = agendamentoRepository.findUltimaSenhaDoDiaParaEspontaneo(
                secretariaId, tipo, inicio, fim, PageRequest.of(0, 1)
        ).stream().findFirst().orElse(null);

        if (ultima == null) return prefixo + "001";

        return gerarProximaSenha(ultima); // N001 -> N002
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
    public Agendamento criarEspontaneo(Long secretariaId, Agendamento agendamento) {

        if (secretariaId == null) throw new RuntimeException("Secretaria é obrigatória");

        if (agendamento.getServico() == null || agendamento.getServico().getId() == null) {
            throw new RuntimeException("Serviço é obrigatório");
        }

        Servico servico = servicoService.buscarPorId(agendamento.getServico().getId());

        if (servico.getSecretaria() == null || servico.getSecretaria().getId() == null) {
            throw new RuntimeException("Serviço sem secretaria vinculada");
        }

        if (!servico.getSecretaria().getId().equals(secretariaId)) {
            throw new RuntimeException("Serviço não pertence à secretaria informada");
        }

        agendamento.setServico(servico);

        // ✅ salva secretaria no agendamento (isso que estava faltando)
        agendamento.setSecretaria(servico.getSecretaria());

        // 🔥 espontâneo não usa configuração
        agendamento.setConfiguracao(null);

        agendamento.setSituacao(SituacaoAgendamento.AGENDADO);
        agendamento.setHoraAgendamento(LocalDateTime.now());

        if (agendamento.getTipoAtendimento() == null || agendamento.getTipoAtendimento().isBlank()) {
            agendamento.setTipoAtendimento("NORMAL");
        } else {
            agendamento.setTipoAtendimento(agendamento.getTipoAtendimento().toUpperCase());
        }

        validarAgendamentoEspontaneo(agendamento);

        int tentativas = 0;
        LocalDateTime agora = LocalDateTime.now(ZoneId.of("America/Fortaleza"));
        agendamento.setHoraAgendamento(agora);
        LocalDate hoje = agora.toLocalDate();

        while (true) {
            tentativas++;

            agendamento.setSenha(
                    gerarSenhaParaDiaEspontaneo(secretariaId, agendamento.getTipoAtendimento(), hoje)
            );

            try {
                return agendamentoRepository.save(agendamento);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                if (tentativas >= 5) throw new RuntimeException("Falha ao gerar senha única");
            }
        }
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

    private String gerarSenhaEspontanea(Long secretariaId, String tipoAtendimento) {

        String prefixo = gerarPrefixo(tipoAtendimento);

        long totalHoje = agendamentoRepository.countBySecretariaAndTipoAndData(
                secretariaId.intValue(),
                tipoAtendimento,
                LocalDate.now()
        );

        return String.format("%s%03d", prefixo, totalHoje + 1);
    }

    public List<AgendamentoDTO> listarAgendamentosComDetalhes(Long agendamentoId) {
        return agendamentoRepository.buscarAgendamentosComDetalhes(agendamentoId);
    }
    
 // 🔹 Chamar próxima senha normal
     public Agendamento chamarProximaNormal(Long secretariaId, Long atendenteId) {

         Gerenciador gerenciador = atendenteRepository.findById(atendenteId)
                 .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

         LocalDate hoje = LocalDate.now();
         LocalDateTime inicio = hoje.atStartOfDay();
         LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

         var lista = agendamentoRepository.buscarProximoNormalHoje(
                 secretariaId, inicio, fim, PageRequest.of(0, 1)
         );

         Agendamento proximo = lista.isEmpty() ? null : lista.get(0);

         return processarChamada(proximo, gerenciador);
     }

    public Agendamento chamarProximaPrioridade(Long secretariaId, Long atendenteId) {

        Gerenciador gerenciador = atendenteRepository.findById(atendenteId)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

        var lista = agendamentoRepository.buscarProximoPrioridadeHoje(
                secretariaId, inicio, fim, PageRequest.of(0, 1)
        );

        Agendamento proximo = lista.isEmpty() ? null : lista.get(0);

        return processarChamada(proximo, gerenciador);
    }

    public AgendamentoResponseDTO chamarPorSenha(String senha, Long atendenteId) throws Exception {

        Gerenciador gerenciador = atendenteRepository.findById(atendenteId)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

        Long secretariaId = gerenciador.getSecretaria().getId();

        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

        Pageable pageable = PageRequest.of(0, 1);
        List<Agendamento> agendamentos = agendamentoRepository.buscarPorSenhaHoje(
                secretariaId, senha, inicio, fim, pageable
        );

        if (agendamentos.isEmpty()) {
            throw new Exception("Agendamento não encontrado para a senha " + senha + " (hoje)");
        }

        Agendamento agendamento = agendamentos.get(0);

        // 🔹 Agora chama passando o atendente
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
        agendamento.setAtendente(gerenciador); // 🔹 AQUI

        Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);
        // 🔹 REGISTRA HISTÓRICO DA CHAMADA
        ChamadaAgendamento chamada = new ChamadaAgendamento();
        chamada.setAgendamento(agendamentoSalvo);
        chamada.setGerenciador(gerenciador);
        chamada.setSecretaria(
                agendamentoSalvo.getServico() != null
                        ? agendamentoSalvo.getServico().getSecretaria()
                        : null
        );
        chamada.setSenha(agendamentoSalvo.getSenha());
        chamada.setTipoAtendimento(agendamentoSalvo.getTipoAtendimento());
        chamada.setGuiche(gerenciador.getGuiche());
        chamada.setDataChamada(LocalDateTime.now());

        chamadaAgendamentoRepository.save(chamada);

        return agendamentoSalvo;
    }

    public List<UltimaChamadaDTO> getUltimasChamadasPorSecretaria(String sigla) {
        return chamadaAgendamentoRepository.buscarUltimasChamadasPorSecretaria(sigla);
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
