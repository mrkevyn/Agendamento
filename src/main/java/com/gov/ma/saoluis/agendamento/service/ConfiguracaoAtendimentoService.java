package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.DatasResponse;
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
import java.util.Set;

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
        validarConfiguracao(configuracao); // valida só a base (hora/regra/guiches)

        gerarHorarios(configuracao);
        configuracao.setAtivo(true);

        // garante que não salva null
        if (configuracao.getDatasAtendimento() == null) {
            configuracao.setDatasAtendimento(new java.util.HashSet<>());
        }

        return repository.save(configuracao);
    }

    @Transactional
    public ConfiguracaoAtendimento adicionarDatas(Long configuracaoId, Set<LocalDate> datas) {

        if (datas == null || datas.isEmpty()) {
            throw new RuntimeException("Informe ao menos uma data de atendimento");
        }

        ConfiguracaoAtendimento cfg = buscarPorId(configuracaoId);

        cfg.getDatasAtendimento().addAll(datas);

        ConfiguracaoAtendimento salvo = repository.save(cfg);

        for (LocalDate data : datas) {
            slotService.garantirSlotsDoDia(salvo, data);
        }

        return salvo;
    }

    @Transactional
    public ConfiguracaoAtendimento removerDatas(Long configuracaoId, Set<LocalDate> datas) {

        if (datas == null || datas.isEmpty()) {
            throw new RuntimeException("Informe ao menos uma data para remover");
        }

        ConfiguracaoAtendimento cfg = buscarPorId(configuracaoId);

        for (LocalDate data : datas) {

            // se já tem alguém reservado, bloqueia
            boolean temReserva = slotAtendimentoRepository.existsByConfiguracaoIdAndDataAndReservadosGreaterThan(
                    configuracaoId, data, 0
            );

            if (temReserva) {
                throw new RuntimeException("Não é possível desvincular a data " + data +
                        " porque existem reservas nesse dia.");
            }

            // remove do conjunto vinculado
            cfg.getDatasAtendimento().remove(data);

            // (opcional) apaga slots do dia pra não ficar lixo no banco
            slotAtendimentoRepository.deleteByConfiguracaoIdAndData(configuracaoId, data);
        }

        return repository.save(cfg);
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
        existente.setTipoRegra(novosDados.getTipoRegra());
        existente.setAtivo(novosDados.getAtivo());

        // ✅ Atualiza datas sem quebrar a coleção (@ElementCollection)
        existente.getDatasAtendimento().clear();
        if (novosDados.getDatasAtendimento() != null) {
            existente.getDatasAtendimento().addAll(novosDados.getDatasAtendimento());
        }

        // 🔥 gera sem quebrar a coleção de horários
        gerarHorarios(existente);

        ConfiguracaoAtendimento salvo = repository.save(existente);

        // (opcional, mas recomendado) garantir slots para as novas datas
        for (LocalDate data : salvo.getDatasAtendimento()) {
            slotService.garantirSlotsDoDia(salvo, data);
        }

        return salvo;
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

    public DatasResponse listarDatas(Long id){
        ConfiguracaoAtendimento cfg = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuração não encontrada"));

        Set<LocalDate> set = cfg.getDatasAtendimento(); // pode ser null
        List<LocalDate> datas = (set == null) ? List.of() : set.stream().sorted().toList();

        return new DatasResponse(datas);
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
        List<ConfiguracaoAtendimento> configuracoes =
                repository.findAtivasPorSecretaria(secretariaId);

        return configuracoes.stream()
                // 📆 valida por data específica
                .filter(cfg -> cfg.getDatasAtendimento() != null
                        && cfg.getDatasAtendimento().contains(data))

                // ⏰ valida horário dentro do bloco
                .filter(cfg -> !hora.isBefore(cfg.getHoraInicio())
                        && hora.isBefore(cfg.getHoraFim()))

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

    public List<LocalDate> listarDatasVinculadas(Long secretariaId, Long configuracaoId) {
        ConfiguracaoAtendimento cfg = buscarPorId(configuracaoId);

        if (!cfg.getSecretaria().getId().equals(secretariaId)) {
            throw new RuntimeException("Configuração não pertence a esta secretaria");
        }

        if (cfg.getDatasAtendimento() == null) return List.of();

        LocalDate hoje = LocalDate.now();

        return cfg.getDatasAtendimento().stream()
                .filter(d -> !d.isBefore(hoje))
                .sorted()
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
