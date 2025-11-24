package com.gov.ma.saoluis.agendamento.service;

import org.springframework.stereotype.Service;
import com.gov.ma.saoluis.agendamento.DTO.AgendamentoDTO;
import com.gov.ma.saoluis.agendamento.model.Agendamento;
import com.gov.ma.saoluis.agendamento.repository.AgendamentoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    // 🔹 Listar todos
    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    // 🔹 Buscar por ID
    public Agendamento buscarPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
    }

    // 🔹 Criar novo agendamento
    public Agendamento salvar(Agendamento agendamento) {
        agendamento.setSituacao("AGENDADO");

        // Define tipo de atendimento padrão
        if (agendamento.getTipoAtendimento() == null || agendamento.getTipoAtendimento().isEmpty()) {
            agendamento.setTipoAtendimento("NORMAL");
        }

        // Se horaAgendamento não estiver definida, usa agora
        if (agendamento.getHoraAgendamento() == null) {
            agendamento.setHoraAgendamento(LocalDateTime.now());
        }

        String prefixo = gerarPrefixo(agendamento.getTipoAtendimento());

        // Conta apenas agendamentos do mesmo tipo e do mesmo dia
        LocalDate dataDoAgendamento = agendamento.getHoraAgendamento().toLocalDate();
        long totalPorTipoHoje = agendamentoRepository.countByTipoAtendimentoAndData(
            agendamento.getTipoAtendimento(),
            dataDoAgendamento
        );

        String senha = String.format("%s%03d", prefixo, totalPorTipoHoje + 1);
        agendamento.setSenha(senha);

        return agendamentoRepository.save(agendamento);
    }

    // 🔹 Atualizar (reagendar)
    public Agendamento atualizar(Long id, Agendamento novosDados) {
        Agendamento existente = buscarPorId(id);

        existente.setHoraAgendamento(novosDados.getHoraAgendamento());
        existente.setServicoId(novosDados.getServicoId());
        existente.setUsuarioId(novosDados.getUsuarioId());

        String tipoAtendimento = (novosDados.getTipoAtendimento() != null && !novosDados.getTipoAtendimento().isEmpty())
                ? novosDados.getTipoAtendimento()
                : existente.getTipoAtendimento();
        existente.setTipoAtendimento(tipoAtendimento);

        existente.setSenha(gerarProximaSenha(existente.getSenha()));
        existente.setSituacao("REAGENDADO");

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
        int proximoNumero = numero + 1;

        return String.format("%s%03d", prefixo, proximoNumero);
    }

    public List<AgendamentoDTO> listarAgendamentosComDetalhes() {
        return agendamentoRepository.buscarAgendamentosComDetalhes();
    }
    
 // 🔹 Chamar próxima senha normal
    public Agendamento chamarProximaNormal() {
        Agendamento proximo = agendamentoRepository.buscarProximoNormal();
        return processarChamada(proximo);
    }

    // 🔹 Chamar próxima senha prioritária
    public Agendamento chamarProximaPrioridade() {
        Agendamento proximo = agendamentoRepository.buscarProximoPrioridade();
        return processarChamada(proximo);
    }

    // 🔹 Lógica comum para registrar chamada
    private Agendamento processarChamada(Agendamento agendamento) {
        if (agendamento == null) {
            throw new RuntimeException("Nenhuma senha disponível para chamar.");
        }

        agendamento.setSituacao("EM_ATENDIMENTO");
        agendamento.setHoraChamada(LocalDateTime.now());

        return agendamentoRepository.save(agendamento);
    }
    
    public Agendamento getUltimaChamada() {
        return agendamentoRepository.findTopByOrderByHoraChamadaDesc();
    }
}
