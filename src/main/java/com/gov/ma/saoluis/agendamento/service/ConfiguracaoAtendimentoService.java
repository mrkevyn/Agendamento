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

        // 🔹 Valida os dados da configuração (horários, dias, número de guichês, regra, etc)
        validarConfiguracao(configuracao);

        // 🔹 Gera os horários de atendimento de acordo com a regra
        gerarHorarios(configuracao);

        // 🔹 Marca como ativa
        configuracao.setAtivo(true);

        // 🔹 Salva no banco
        ConfiguracaoAtendimento salva = repository.save(configuracao);

        // 🔹 Opcional: já criar agendamentos “espontâneos” ou pré-reservas se quiser
        //    ou simplesmente garantir que qualquer agendamento posterior terá configuracao_atendimento_id preenchido
        //    nada precisa mudar no serviço, pois a configuração pertence à secretaria

        return salva;
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

        cfg.getHorarios().clear(); // 🔥 remove antigos (orphanRemoval cuida do delete)

        LocalTime atual = cfg.getHoraInicio();

        while (!atual.isAfter(cfg.getHoraFim())) {
            HorarioAtendimento h = new HorarioAtendimento();
            h.setConfiguracao(cfg);
            h.setHora(atual);
            h.setOcupado(false);

            cfg.getHorarios().add(h);

            atual = atual.plusMinutes(cfg.getIntervaloMinutos());
        }

        cfg.setQuantidadeAtendimentos(cfg.getHorarios().size());
    }

    private void gerarHorariosPorQuantidade(ConfiguracaoAtendimento cfg) {

        cfg.getHorarios().clear();

        long minutosTotais = Duration.between(
                cfg.getHoraInicio(),
                cfg.getHoraFim()
        ).toMinutes();

        int quantidade = cfg.getQuantidadeAtendimentos();

        long intervalo = minutosTotais / quantidade;

        LocalTime atual = cfg.getHoraInicio();

        for (int i = 0; i < quantidade; i++) {

            HorarioAtendimento h = new HorarioAtendimento();
            h.setConfiguracao(cfg);
            h.setHora(atual);
            h.setOcupado(false);

            cfg.getHorarios().add(h);

            atual = atual.plusMinutes(intervalo);
        }

        // ⏱ sistema calcula
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

    // 🔹 Converte DayOfWeek → DiaSemana
    private DiaSemana converterDia(DayOfWeek dayOfWeek) {
        return DiaSemana.valueOf(dayOfWeek.name());
    }
}
