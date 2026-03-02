package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.DTO.ServicoResponseDTO;
import com.gov.ma.saoluis.agendamento.DTO.VincularEnderecosDTO;
import com.gov.ma.saoluis.agendamento.DTO.VincularSetoresDTO;
import com.gov.ma.saoluis.agendamento.model.Servico;
import com.gov.ma.saoluis.agendamento.service.ServicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/agendamento/api/servico")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    // Listar todos os serviços
    @GetMapping("/listar-todos")
    public ResponseEntity<List<Servico>> listarTodos() {
        try {
            List<Servico> servicos = servicoService.listarTodos();
            return ResponseEntity.ok(servicos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    // Buscar serviço por ID
    @GetMapping("/buscar/{id}")
    public ResponseEntity<Servico> buscarPorId(@PathVariable Long id) {
        try {
            Servico servico = servicoService.buscarPorId(id);
            if (servico != null) {
                return ResponseEntity.ok(servico);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/servicos/{id}/setores")
    public ResponseEntity<ServicoResponseDTO> vincularSetores(
            @PathVariable Long id,
            @RequestBody VincularSetoresDTO dto
    ) {
        Servico servico = servicoService.vincularSetores(id, dto.setorIds());

        return ResponseEntity.ok(
                new ServicoResponseDTO(
                        servico.getId(),
                        servico.getNome(),
                        servico.getDescricao()
                )
        );
    }

    @PutMapping("/servicos/{id}/setores/remover")
    public ResponseEntity<ServicoResponseDTO> desvincularSetores(
            @PathVariable Long id,
            @RequestBody VincularSetoresDTO dto
    ) {
        Servico servico = servicoService.desvincularSetores(id, dto.setorIds());

        return ResponseEntity.ok(
                new ServicoResponseDTO(
                        servico.getId(),
                        servico.getNome(),
                        servico.getDescricao()
                )
        );
    }

    @GetMapping("/setor/{setorId}")
    public ResponseEntity<List<ServicoResponseDTO>> listarPorSetor(@PathVariable Long setorId) {
        return ResponseEntity.ok(servicoService.listarPorSetor(setorId));
    }
}
