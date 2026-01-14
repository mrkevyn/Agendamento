package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.AgendamentoResponseDTO;
import com.gov.ma.saoluis.agendamento.DTO.UltimaChamadaDTO;
import com.gov.ma.saoluis.agendamento.model.*;
import com.gov.ma.saoluis.agendamento.repository.ChamadaAgendamentoRepository;
import com.gov.ma.saoluis.agendamento.repository.GerenciadorRepository;
import com.gov.ma.saoluis.agendamento.repository.HorarioAtendimentoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.gov.ma.saoluis.agendamento.DTO.AgendamentoDTO;
import com.gov.ma.saoluis.agendamento.repository.AgendamentoRepository;
import com.gov.ma.saoluis.agendamento.DTO.AgendamentoAppRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public AgendamentoService(GerenciadorRepository gerenciadorRepository, AgendamentoRepository agendamentoRepository, LogService logService, ConfiguracaoAtendimentoService configuracaoService, ChamadaAgendamentoRepository chamadaAgendamentoRepository, HorarioAtendimentoRepository horarioRepository, ServicoService servicoService, UsuarioService usuarioService) {
        this.atendenteRepository = gerenciadorRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.logService = logService;
        this.configuracaoService = configuracaoService;
        this.chamadaAgendamentoRepository = chamadaAgendamentoRepository;
        this.horarioRepository = horarioRepository;
        this.servicoService = servicoService;
        this.usuarioService = usuarioService;
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
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
    }

    // 🔹 Criar novo agendamento
    public Agendamento salvarApp(AgendamentoAppRequest req) {

        Usuario usuario = usuarioService.buscarPorId(req.usuarioId());
        Servico servico = servicoService.buscarPorId(req.servicoId());

        HorarioAtendimento horario = horarioRepository.findById(req.horarioId())
                .orElseThrow(() -> new RuntimeException("Horário inválido"));

        if (horario.getOcupado()) {
            throw new RuntimeException("Horário já ocupado");
        }

        ConfiguracaoAtendimento configuracao = horario.getConfiguracao();

        // 🔒 trava o horário
        horario.setOcupado(true);
        horarioRepository.save(horario);

        Agendamento agendamento = new Agendamento();
        agendamento.setUsuario(usuario);
        agendamento.setServico(servico);
        agendamento.setConfiguracao(configuracao);
        agendamento.setHoraAgendamento(
                LocalDateTime.of(LocalDate.now(), horario.getHora())
        );
        agendamento.setSituacao(SituacaoAgendamento.AGENDADO);
        agendamento.setTipoAtendimento(
                req.tipoAtendimento() == null ? "NORMAL" : req.tipoAtendimento()
        );

        // 🔢 senha
        long totalHoje = agendamentoRepository.countBySecretariaAndTipoAndData(
                configuracao.getSecretaria().getId().intValue(),
                agendamento.getTipoAtendimento(),
                LocalDate.now()
        );

        agendamento.setSenha(
                String.format("%s%03d", gerarPrefixo(agendamento.getTipoAtendimento()), totalHoje + 1)
        );

        return agendamentoRepository.save(agendamento);
    }

    private void validarAgendamentoEspontaneo(Agendamento agendamento) {

        if (agendamento.getConfiguracao() == null) {
            throw new RuntimeException("Configuração de atendimento é obrigatória");
        }

        if (!agendamento.getConfiguracao().getAtivo()) {
            throw new RuntimeException("Configuração de atendimento está inativa");
        }

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

    public Agendamento criarEspontaneo(Agendamento agendamento) {

        if (agendamento.getServico() == null || agendamento.getServico().getId() == null) {
            throw new RuntimeException("Serviço é obrigatório");
        }

        if (agendamento.getConfiguracao() == null || agendamento.getConfiguracao().getId() == null) {
            throw new RuntimeException("Configuração é obrigatória");
        }

        ConfiguracaoAtendimento cfg =
                configuracaoService.buscarPorId(agendamento.getConfiguracao().getId());

        agendamento.setConfiguracao(cfg);
        agendamento.setSituacao(SituacaoAgendamento.AGENDADO);
        agendamento.setHoraAgendamento(LocalDateTime.now());

        if (agendamento.getTipoAtendimento() == null || agendamento.getTipoAtendimento().isBlank()) {
            agendamento.setTipoAtendimento("NORMAL");
        }

        // 🔥 Gera senha correta (sem depender de horário)
        agendamento.setSenha(
                gerarSenhaEspontanea(
                        cfg.getSecretaria().getId(),
                        agendamento.getTipoAtendimento()
                )
        );

        validarAgendamentoEspontaneo(agendamento);

        return agendamentoRepository.save(agendamento);
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

         var lista = agendamentoRepository.buscarProximoNormal(
                 secretariaId,
                 PageRequest.of(0, 1)
         );

         Agendamento proximo = lista.isEmpty() ? null : lista.get(0);

         return processarChamada(proximo, gerenciador);
     }

    public Agendamento chamarProximaPrioridade(Long secretariaId, Long atendenteId) {

        Gerenciador gerenciador = atendenteRepository.findById(atendenteId)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

        var lista = agendamentoRepository.buscarProximoPrioridade(
                secretariaId,
                PageRequest.of(0, 1)
        );

        Agendamento proximo = lista.isEmpty() ? null : lista.get(0);

        return processarChamada(proximo, gerenciador);
    }

    public AgendamentoResponseDTO chamarPorSenha(String senha, Long atendenteId) throws Exception {

        Pageable pageable = PageRequest.of(0, 1);
        List<Agendamento> agendamentos = agendamentoRepository.buscarPorSenha(senha, pageable);

        if (agendamentos.isEmpty()) {
            throw new Exception("Agendamento não encontrado para a senha " + senha);
        }

        Gerenciador gerenciador = atendenteRepository.findById(atendenteId)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado"));

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

    public UltimaChamadaDTO getUltimaChamada() {
        return chamadaAgendamentoRepository.buscarUltimaChamada();
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
