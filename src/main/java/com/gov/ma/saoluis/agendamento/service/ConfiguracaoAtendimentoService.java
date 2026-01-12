package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.model.ConfiguracaoAtendimento;
import com.gov.ma.saoluis.agendamento.model.DiaSemana;
import com.gov.ma.saoluis.agendamento.model.HorarioAtendimento;
import com.gov.ma.saoluis.agendamento.model.TipoRegraAtendimento;
import com.gov.ma.saoluis.agendamento.repository.ConfiguracaoAtendimentoRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ConfiguracaoAtendimentoService {

    private final ConfiguracaoAtendimentoRepository repository;

    public ConfiguracaoAtendimentoService(ConfiguracaoAtendimentoRepository repository) {
        this.repository = repository;
    }

    // 🔹 Criar configuração
    public ConfiguracaoAtendimento salvar(ConfiguracaoAtendimento configuracao) {

        validarConfiguracao(configuracao);

        if (configuracao.getTipoRegra() == TipoRegraAtendimento.POR_INTERVALO) {
            gerarHorariosPorIntervalo(configuracao);
        }

        if (configuracao.getTipoRegra() == TipoRegraAtendimento.POR_QUANTIDADE) {
            gerarHorariosPorQuantidade(configuracao);
        }

        configuracao.setAtivo(true);

        return repository.save(configuracao);
    }

    // 🔹 Atualizar configuração
    public ConfiguracaoAtendimento atualizar(Long id, ConfiguracaoAtendimento novosDados) {

        ConfiguracaoAtendimento existente = buscarPorId(id);

        validarConfiguracao(novosDados);

        existente.setHoraInicio(novosDados.getHoraInicio());
        existente.setHoraFim(novosDados.getHoraFim());
        existente.setQuantidadeAtendimentos(novosDados.getQuantidadeAtendimentos());
        existente.setNumeroGuiches(novosDados.getNumeroGuiches());
        existente.setDiasAtendimento(novosDados.getDiasAtendimento());
        existente.setAtivo(novosDados.getAtivo());

        return repository.save(existente);
    }

    // 🔹 Buscar por ID
    public ConfiguracaoAtendimento buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuração não encontrada"));
    }

    // 🔹 Listar por secretaria
    public List<ConfiguracaoAtendimento> listarPorSecretaria(Long secretariaId) {
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

        if (cfg.getSecretaria() == null) {
            throw new RuntimeException("Secretaria é obrigatória");
        }

        if (cfg.getHoraInicio() == null || cfg.getHoraFim() == null) {
            throw new RuntimeException("Horário inicial e final são obrigatórios");
        }

        if (cfg.getHoraFim().isBefore(cfg.getHoraInicio())) {
            throw new RuntimeException("Hora fim deve ser após hora início");
        }

        if (cfg.getTipoRegra() == null) {
            throw new RuntimeException("Tipo de regra é obrigatório");
        }

        if (cfg.getTipoRegra() == TipoRegraAtendimento.POR_QUANTIDADE) {
            if (cfg.getQuantidadeAtendimentos() == null || cfg.getQuantidadeAtendimentos() <= 0) {
                throw new RuntimeException("Quantidade de atendimentos inválida");
            }
            cfg.setIntervaloMinutos(null); // limpa o que não usa
        }

        if (cfg.getTipoRegra() == TipoRegraAtendimento.POR_INTERVALO) {
            if (cfg.getIntervaloMinutos() == null || cfg.getIntervaloMinutos() <= 0) {
                throw new RuntimeException("Intervalo inválido");
            }

            long minutosTotais = java.time.Duration.between(
                    cfg.getHoraInicio(),
                    cfg.getHoraFim()
            ).toMinutes();

            int quantidade = (int) (minutosTotais / cfg.getIntervaloMinutos());
            cfg.setQuantidadeAtendimentos(quantidade);
        }

        if (cfg.getNumeroGuiches() == null || cfg.getNumeroGuiches() <= 0) {
            throw new RuntimeException("Número de guichês inválido");
        }

        if (cfg.getDiasAtendimento() == null || cfg.getDiasAtendimento().isEmpty()) {
            throw new RuntimeException("Informe ao menos um dia de atendimento");
        }
    }

    private void gerarHorariosPorIntervalo(ConfiguracaoAtendimento cfg) {

        Set<HorarioAtendimento> horarios = new HashSet<>();

        LocalTime atual = cfg.getHoraInicio();

        while (!atual.isAfter(cfg.getHoraFim())) {
            HorarioAtendimento h = new HorarioAtendimento();
            h.setConfiguracao(cfg);
            h.setHora(atual);
            h.setOcupado(false);
            horarios.add(h);

            atual = atual.plusMinutes(cfg.getIntervaloMinutos());
        }

        cfg.setQuantidadeAtendimentos(horarios.size());
        cfg.setHorarios(horarios);
    }

    private void gerarHorariosPorQuantidade(ConfiguracaoAtendimento cfg) {

        long minutosTotais = Duration.between(
                cfg.getHoraInicio(),
                cfg.getHoraFim()
        ).toMinutes();

        long intervalo = minutosTotais / (cfg.getQuantidadeAtendimentos() - 1);

        Set<HorarioAtendimento> horarios = new HashSet<>();

        LocalTime atual = cfg.getHoraInicio();

        for (int i = 0; i < cfg.getQuantidadeAtendimentos(); i++) {
            HorarioAtendimento h = new HorarioAtendimento();
            h.setConfiguracao(cfg);
            h.setHora(atual);
            h.setOcupado(false);
            horarios.add(h);

            atual = atual.plusMinutes(intervalo);
        }

        cfg.setIntervaloMinutos((int) intervalo);
        cfg.setHorarios(horarios);
    }

    // 🔹 Converte DayOfWeek → DiaSemana
    private DiaSemana converterDia(DayOfWeek dayOfWeek) {
        return DiaSemana.valueOf(dayOfWeek.name());
    }
}
