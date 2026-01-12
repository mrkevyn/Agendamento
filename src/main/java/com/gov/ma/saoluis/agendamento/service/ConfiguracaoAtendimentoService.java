package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.model.ConfiguracaoAtendimento;
import com.gov.ma.saoluis.agendamento.model.DiaSemana;
import com.gov.ma.saoluis.agendamento.repository.ConfiguracaoAtendimentoRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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

        if (cfg.getQuantidadeAtendimentos() == null || cfg.getQuantidadeAtendimentos() <= 0) {
            throw new RuntimeException("Quantidade de atendimentos inválida");
        }

        if (cfg.getNumeroGuiches() == null || cfg.getNumeroGuiches() <= 0) {
            throw new RuntimeException("Número de guichês inválido");
        }

        Set<DiaSemana> dias = cfg.getDiasAtendimento();
        if (dias == null || dias.isEmpty()) {
            throw new RuntimeException("Informe ao menos um dia de atendimento");
        }
    }

    // 🔹 Converte DayOfWeek → DiaSemana
    private DiaSemana converterDia(DayOfWeek dayOfWeek) {
        return DiaSemana.valueOf(dayOfWeek.name());
    }
}
