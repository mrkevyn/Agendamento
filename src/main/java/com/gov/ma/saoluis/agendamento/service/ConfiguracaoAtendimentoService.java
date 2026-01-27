package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.model.*;
import com.gov.ma.saoluis.agendamento.repository.ConfiguracaoAtendimentoRepository;
import com.gov.ma.saoluis.agendamento.repository.SlotAtendimentoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.List;

@Service
public class ConfiguracaoAtendimentoService {

    private final ConfiguracaoAtendimentoRepository repository;

    private final SlotAtendimentoService slotService;

    private final SlotAtendimentoRepository slotAtendimentoRepository;

    public ConfiguracaoAtendimentoService(ConfiguracaoAtendimentoRepository repository, SlotAtendimentoService slotService, SlotAtendimentoRepository slotAtendimentoRepository) {
        this.repository = repository;
        this.slotService = slotService;
        this.slotAtendimentoRepository = slotAtendimentoRepository;
    }

    // 🔹 Criar configuração
    @Transactional
    public ConfiguracaoAtendimento salvar(ConfiguracaoAtendimento configuracao) {
        validarConfiguracao(configuracao);
        gerarHorarios(configuracao);
        configuracao.setAtivo(true);

        ConfiguracaoAtendimento salva = repository.save(configuracao);

        // pré-gerar próximos 30 dias
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i <= 30; i++) {
            slotService.garantirSlotsDoDia(salva, hoje.plusDays(i));
        }

        return salva;
    }

    private void gerarSlotsProximosDias(ConfiguracaoAtendimento cfg, int dias) {
        LocalDate hoje = LocalDate.now();

        for (int i = 0; i <= dias; i++) {
            LocalDate data = hoje.plusDays(i);
            slotService.garantirSlotsDoDia(cfg, data);
        }
    }

    // 🔹 Atualizar configuração
    public ConfiguracaoAtendimento atualizar(Long id, ConfiguracaoAtendimento novosDados) {

        ConfiguracaoAtendimento existente = buscarPorId(id);

        validarConfiguracao(novosDados);

        existente.setHoraInicio(novosDados.getHoraInicio());
        existente.setHoraFim(novosDados.getHoraFim());
        existente.setQuantidadeAtendimentos(novosDados.getQuantidadeAtendimentos());
        existente.setIntervaloMinutos(novosDados.getIntervaloMinutos());
        existente.setNumeroGuiches(novosDados.getNumeroGuiches());
        existente.setDiasAtendimento(novosDados.getDiasAtendimento());
        existente.setTipoRegra(novosDados.getTipoRegra());
        existente.setAtivo(novosDados.getAtivo());

        // 🔥 gera sem quebrar a coleção
        gerarHorarios(existente);

        return repository.save(existente);
    }

    // 🔹 Buscar por ID
    public ConfiguracaoAtendimento buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuração não encontrada"));
    }

    // 🔹 Listar por secretaria
    public List<ConfiguracaoAtendimento> listarPorSecretaria(Long secretariaId) {
        System.out.print(repository.findBySecretariaIdAndAtivoTrue(secretariaId));
        return repository.findBySecretariaIdAndAtivoTrue(secretariaId);
    }

    // 🔹 Desativar configuração
    public void desativar(Long id) {
        ConfiguracaoAtendimento configuracao = buscarPorId(id);
        configuracao.setAtivo(false);
        repository.save(configuracao);
    }

    // 🔹 Verifica se uma data/horário é permitido
    public ConfiguracaoAtendimento validarDisponibilidade(
            Long secretariaId,
            LocalDate data,
            LocalTime hora
    ) {

        DiaSemana diaSemana = converterDia(data.getDayOfWeek());

        List<ConfiguracaoAtendimento> configuracoes =
                repository.findAtivasPorSecretaria(secretariaId);

        return configuracoes.stream()
                .filter(cfg -> cfg.getDiasAtendimento().contains(diaSemana))
                .filter(cfg -> !hora.isBefore(cfg.getHoraInicio())
                        && !hora.isAfter(cfg.getHoraFim()))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Horário indisponível para esta secretaria")
                );
    }

    // 🔹 Validações de regra
    private void validarConfiguracao(ConfiguracaoAtendimento cfg) {

        if (cfg.getHoraInicio() == null || cfg.getHoraFim() == null) {
            throw new RuntimeException("Hora início e fim são obrigatórias");
        }

        if (cfg.getHoraFim().isBefore(cfg.getHoraInicio())) {
            throw new RuntimeException("Hora fim deve ser após hora início");
        }

        if (cfg.getTipoRegra() == null) {
            throw new RuntimeException("Tipo de regra é obrigatório");
        }

        // ✅ PAUSA (opcional) - ex: 13:00 até 14:00
        if (cfg.getPausaInicio() != null || cfg.getPausaFim() != null) {

            // se um veio, os dois precisam vir
            if (cfg.getPausaInicio() == null || cfg.getPausaFim() == null) {
                throw new RuntimeException("Pausa deve ter início e fim");
            }

            // início < fim
            if (!cfg.getPausaInicio().isBefore(cfg.getPausaFim())) {
                throw new RuntimeException("Pausa início deve ser antes da pausa fim");
            }

            // pausa dentro do expediente
            if (cfg.getPausaInicio().isBefore(cfg.getHoraInicio()) || cfg.getPausaFim().isAfter(cfg.getHoraFim())) {
                throw new RuntimeException("Pausa deve estar dentro do horário de atendimento");
            }

            // pausa não pode engolir tudo (tem que sobrar tempo)
            if (!cfg.getPausaInicio().isAfter(cfg.getHoraInicio()) && !cfg.getPausaFim().isBefore(cfg.getHoraFim())) {
                throw new RuntimeException("Pausa inválida: não pode cobrir todo o expediente");
            }
        }

        // 🔹 POR INTERVALO
        if (cfg.getTipoRegra() == TipoRegraAtendimento.POR_INTERVALO) {

            if (cfg.getIntervaloMinutos() == null || cfg.getIntervaloMinutos() <= 0) {
                throw new RuntimeException("Intervalo em minutos é obrigatório");
            }

            // 🔥 IGNORA quantidade vinda do front
            cfg.setQuantidadeAtendimentos(null);
        }

        // 🔹 POR QUANTIDADE
        if (cfg.getTipoRegra() == TipoRegraAtendimento.POR_QUANTIDADE) {

            if (cfg.getQuantidadeAtendimentos() == null || cfg.getQuantidadeAtendimentos() < 2) {
                throw new RuntimeException("Quantidade mínima de atendimentos é 2");
            }

            // 🔥 IGNORA intervalo vindo do front
            cfg.setIntervaloMinutos(null);
        }

        if (cfg.getNumeroGuiches() == null || cfg.getNumeroGuiches() <= 0) {
            throw new RuntimeException("Número de guichês inválido");
        }

        if (cfg.getDiasAtendimento() == null || cfg.getDiasAtendimento().isEmpty()) {
            throw new RuntimeException("Informe ao menos um dia de atendimento");
        }
    }

    private void gerarHorariosPorIntervalo(ConfiguracaoAtendimento cfg) {

        cfg.getHorarios().clear();

        LocalTime atual = cfg.getHoraInicio();

        while (!atual.isAfter(cfg.getHoraFim())) {

            // ✅ pula horários na pausa
            if (!estaNaPausa(cfg, atual)) {
                HorarioAtendimento h = new HorarioAtendimento();
                h.setConfiguracao(cfg);
                h.setHora(atual);
                h.setOcupado(false);
                cfg.getHorarios().add(h);
            }

            atual = atual.plusMinutes(cfg.getIntervaloMinutos());
        }

        cfg.setQuantidadeAtendimentos(cfg.getHorarios().size());
    }

    private void gerarHorariosPorQuantidade(ConfiguracaoAtendimento cfg) {

        cfg.getHorarios().clear();

        long totalMinutos = Duration.between(cfg.getHoraInicio(), cfg.getHoraFim()).toMinutes();

        long pausaMin = 0;
        if (cfg.getPausaInicio() != null && cfg.getPausaFim() != null) {
            pausaMin = Duration.between(cfg.getPausaInicio(), cfg.getPausaFim()).toMinutes();
        }

        long minutosUteis = totalMinutos - pausaMin;

        int quantidade = cfg.getQuantidadeAtendimentos();
        long intervalo = minutosUteis / quantidade;

        LocalTime atual = cfg.getHoraInicio();

        for (int i = 0; i < quantidade; i++) {

            // se cair na pausa, pula pro fim da pausa
            if (estaNaPausa(cfg, atual)) {
                atual = cfg.getPausaFim();
            }

            HorarioAtendimento h = new HorarioAtendimento();
            h.setConfiguracao(cfg);
            h.setHora(atual);
            h.setOcupado(false);
            cfg.getHorarios().add(h);

            atual = atual.plusMinutes(intervalo);

            // se depois de somar cair na pausa, ajusta também
            if (estaNaPausa(cfg, atual)) {
                atual = cfg.getPausaFim();
            }
        }

        cfg.setIntervaloMinutos((int) intervalo);
    }

    private void gerarHorarios(ConfiguracaoAtendimento cfg) {

        // 🔥 LIMPA HORÁRIOS ANTIGOS (funciona no update)
        if (cfg.getHorarios() != null) {
            cfg.getHorarios().clear();
        }

        if (cfg.getTipoRegra() == TipoRegraAtendimento.POR_INTERVALO) {
            gerarHorariosPorIntervalo(cfg);
        }

        if (cfg.getTipoRegra() == TipoRegraAtendimento.POR_QUANTIDADE) {
            gerarHorariosPorQuantidade(cfg);
        }
    }

    // 🔹 Converte DayOfWeek → DiaSemana (PT-BR)
    private DiaSemana converterDia(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.SEGUNDA;
            case TUESDAY -> DiaSemana.TERCA;
            case WEDNESDAY -> DiaSemana.QUARTA;
            case THURSDAY -> DiaSemana.QUINTA;
            case FRIDAY -> DiaSemana.SEXTA;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }

    public List<LocalDate> listarDatasDisponiveis(Long secretariaId, Long configuracaoId, int dias) {
        ConfiguracaoAtendimento cfg = buscarPorId(configuracaoId);

        LocalDate hoje = LocalDate.now();
        return java.util.stream.IntStream.rangeClosed(0, dias)
                .mapToObj(hoje::plusDays)
                .filter(data -> {
                    slotService.garantirSlotsDoDia(cfg, data);
                    return slotAtendimentoRepository.findByConfiguracaoIdAndDataOrderByHora(configuracaoId, data)
                            .stream()
                            .anyMatch(SlotAtendimento::temVaga);
                })
                .toList();
    }

    private boolean estaNaPausa(ConfiguracaoAtendimento cfg, LocalTime hora) {
        if (cfg.getPausaInicio() == null || cfg.getPausaFim() == null) return false;

        // dentro: [pausaInicio, pausaFim)
        return !hora.isBefore(cfg.getPausaInicio()) && hora.isBefore(cfg.getPausaFim());
    }

    public ConfiguracaoAtendimento buscarConfigAtivaPorSecretaria(Long secretariaId) {

        List<ConfiguracaoAtendimento> configs =
                repository.findBySecretariaIdAndAtivoTrue(secretariaId);

        if (configs.isEmpty()) {
            throw new RuntimeException("Não existe configuração ativa para esta secretaria");
        }

        return configs.get(0); // ⚠️ perigoso se tiver mais de uma
    }
}
