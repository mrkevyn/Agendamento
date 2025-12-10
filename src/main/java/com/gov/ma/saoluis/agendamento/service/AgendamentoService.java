package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.AgendamentoResponseDTO;
import com.gov.ma.saoluis.agendamento.DTO.UltimaChamadaDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    // 🔹 Listar todos COM DETALHES
    public List<AgendamentoDTO> listarPorSecretaria(Long secretariaId) {
        return agendamentoRepository.buscarAgendamentosPorSecretaria(secretariaId);
    }

    // 🔹 Buscar por ID
    public Agendamento buscarPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
    }

    // 🔹 Criar novo agendamento
    public Agendamento salvar(Agendamento agendamento) {

        agendamento.setSituacao("AGENDADO");

        if (agendamento.getTipoAtendimento() == null || agendamento.getTipoAtendimento().isEmpty()) {
            agendamento.setTipoAtendimento("NORMAL");
        }

        if (agendamento.getHoraAgendamento() == null) {
            agendamento.setHoraAgendamento(LocalDateTime.now());
        }

        // PREFIXO
        String prefixo = gerarPrefixo(agendamento.getTipoAtendimento());

        // 🟢 Agora buscamos a secretaria do serviço diretamente no banco!
        Long secretariaId = agendamentoRepository.findSecretariaIdByServicoId(
                agendamento.getServico().getId()
        );

        if (secretariaId == null) {
            throw new RuntimeException("O serviço informado não possui secretaria vinculada.");
        }

        LocalDate data = agendamento.getHoraAgendamento().toLocalDate();

        long totalHoje = agendamentoRepository.countBySecretariaAndTipoAndData(
                secretariaId.intValue(),
                agendamento.getTipoAtendimento(),
                data
        );

        String senha = String.format("%s%03d", prefixo, totalHoje + 1);
        agendamento.setSenha(senha);

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

        return String.format("%s%03d", prefixo, numero + 1);
    }

    public List<AgendamentoDTO> listarAgendamentosComDetalhes() {
        return agendamentoRepository.buscarAgendamentosComDetalhes();
    }
    
 // 🔹 Chamar próxima senha normal
 public Agendamento chamarProximaNormal(Long secretariaId) {
     var lista = agendamentoRepository.buscarProximoNormal(
             secretariaId,
             PageRequest.of(0, 1)
     );

     Agendamento proximo = lista.isEmpty() ? null : lista.get(0);
     return processarChamada(proximo);
 }

    public Agendamento chamarProximaPrioridade(Long secretariaId) {
        var lista = agendamentoRepository.buscarProximoPrioridade(
                secretariaId,
                PageRequest.of(0, 1)
        );

        Agendamento proximo = lista.isEmpty() ? null : lista.get(0);
        return processarChamada(proximo);
    }

    public AgendamentoResponseDTO chamarPorSenha(String senha) throws Exception {
        Pageable pageable = PageRequest.of(0, 1);
        List<Agendamento> agendamentos = agendamentoRepository.buscarPorSenha(senha, pageable);

        if (agendamentos.isEmpty()) {
            throw new Exception("Agendamento não encontrado para a senha " + senha);
        }

        Agendamento agendamento = agendamentos.get(0);
        agendamento.setHoraChamada(LocalDateTime.now());
        agendamentoRepository.save(agendamento);

        // Criar o DTO manualmente
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
    private Agendamento processarChamada(Agendamento agendamento) {
        if (agendamento == null) {
            throw new RuntimeException("Nenhuma senha disponível para chamar.");
        }

        agendamento.setSituacao("EM_ATENDIMENTO");
        agendamento.setHoraChamada(LocalDateTime.now());

        return agendamentoRepository.save(agendamento);
    }

    public UltimaChamadaDTO getUltimaChamada() {
        return agendamentoRepository.buscarUltimaChamada();
    }

    // 🔹 Finalizar atendimento
    public Agendamento finalizarAtendimento(Long id) {
        Agendamento agendamento = buscarPorId(id);

        if (!"EM_ATENDIMENTO".equals(agendamento.getSituacao())) {
            throw new RuntimeException("Este agendamento não está em atendimento.");
        }

        agendamento.setSituacao("FINALIZADO");
        agendamento.setHoraChamada(LocalDateTime.now());

        return agendamentoRepository.save(agendamento);
    }

    // 🔹 Cancelar atendimento (não compareceu)
    public Agendamento cancelarAtendimento(Long id) {
        Agendamento agendamento = buscarPorId(id);

        if (!"EM_ATENDIMENTO".equals(agendamento.getSituacao())) {
            throw new RuntimeException("Este agendamento não está em atendimento.");
        }

        agendamento.setSituacao("NAO_COMPARECEU");
        agendamento.setHoraChamada(LocalDateTime.now());

        return agendamentoRepository.save(agendamento);
    }
}
