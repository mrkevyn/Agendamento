package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.AgendamentoResponseDTO;
import com.gov.ma.saoluis.agendamento.DTO.UltimaChamadaDTO;
import com.gov.ma.saoluis.agendamento.model.ConfiguracaoAtendimento;
import com.gov.ma.saoluis.agendamento.model.Gerenciador;
import com.gov.ma.saoluis.agendamento.model.SituacaoAgendamento;
import com.gov.ma.saoluis.agendamento.repository.GerenciadorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.gov.ma.saoluis.agendamento.DTO.AgendamentoDTO;
import com.gov.ma.saoluis.agendamento.model.Agendamento;
import com.gov.ma.saoluis.agendamento.repository.AgendamentoRepository;

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

    public AgendamentoService(GerenciadorRepository gerenciadorRepository, AgendamentoRepository agendamentoRepository, LogService logService, ConfiguracaoAtendimentoService configuracaoService) {
        this.atendenteRepository = gerenciadorRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.logService = logService;
        this.configuracaoService = configuracaoService;
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
    public Agendamento salvar(Agendamento agendamento) {

        // 🔹 Valida usuário
        if (agendamento.getUsuario() == null || agendamento.getUsuario().getId() == null) {
            throw new RuntimeException("Usuário é obrigatório");
        }

        // 🔹 Valida serviço
        if (agendamento.getServico() == null || agendamento.getServico().getId() == null) {
            throw new RuntimeException("Serviço é obrigatório");
        }

        agendamento.setSituacao(SituacaoAgendamento.AGENDADO);

        if (agendamento.getTipoAtendimento() == null || agendamento.getTipoAtendimento().isBlank()) {
            agendamento.setTipoAtendimento("NORMAL");
        }

        if (agendamento.getHoraAgendamento() == null) {
            throw new RuntimeException("Data e hora do agendamento são obrigatórias");
        }

        LocalDate data = agendamento.getHoraAgendamento().toLocalDate();
        LocalTime hora = agendamento.getHoraAgendamento().toLocalTime();

        // 🔹 Descobre a secretaria pelo serviço
        Long secretariaId = agendamentoRepository.findSecretariaIdByServicoId(
                agendamento.getServico().getId()
        );

        if (secretariaId == null) {
            throw new RuntimeException("O serviço não possui secretaria vinculada");
        }

        // 🔥 AQUI entra o ConfiguracaoAtendimentoService
        ConfiguracaoAtendimento configuracao =
                configuracaoService.validarDisponibilidade(secretariaId, data, hora);

        // 🔹 Vincula a configuração ao agendamento
        agendamento.setConfiguracao(configuracao);

        // 🔹 Geração da senha
        String prefixo = gerarPrefixo(agendamento.getTipoAtendimento());

        long totalHoje = agendamentoRepository.countBySecretariaAndTipoAndData(
                secretariaId.intValue(),
                agendamento.getTipoAtendimento(),
                data
        );

        String senha = String.format("%s%03d", prefixo, totalHoje + 1);
        agendamento.setSenha(senha);

        // 🔹 Salva
        Agendamento salvo = agendamentoRepository.save(agendamento);

        // 🔴 LOG
        logService.registrar(
                agendamento.getUsuario().getId(),
                "USUARIO",
                "AGENDAMENTO_CRIADO",
                "Agendamento ID: " + salvo.getId()
                        + ", Senha: " + salvo.getSenha()
                        + ", Configuração: " + configuracao.getId()
        );

        return salvo;
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

        return agendamentoRepository.save(agendamento);
    }

    public UltimaChamadaDTO getUltimaChamada() {
        return agendamentoRepository.buscarUltimaChamada();
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
