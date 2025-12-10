package com.gov.ma.saoluis.agendamento.controller;

import com.gov.ma.saoluis.agendamento.model.Secretaria;
import com.gov.ma.saoluis.agendamento.service.SecretariaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/secretarias")
@CrossOrigin(origins = "*") // Libera acesso para frontend
public class SecretariaController {

    private final SecretariaService secretariaService;

    public SecretariaController(SecretariaService secretariaService) {
        this.secretariaService = secretariaService;
    }

    // ✅ GET - Todas
    @GetMapping
    public ResponseEntity<List<Secretaria>> listarTodas() {
        return ResponseEntity.ok(secretariaService.buscarTodas());
    }

    // ✅ GET - Por ID
    @GetMapping("/{id}")
    public ResponseEntity<Secretaria> buscarPorId(@PathVariable Long id) {
        return secretariaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ GET - Somente ativas
    @GetMapping("/ativas")
    public ResponseEntity<List<Secretaria>> listarAtivas() {
        return ResponseEntity.ok(secretariaService.buscarAtivas());
    }

    // ✅ GET - Somente visíveis
    @GetMapping("/visiveis")
    public ResponseEntity<List<Secretaria>> listarVisiveis() {
        return ResponseEntity.ok(secretariaService.buscarVisiveis());
    }

    // ✅ GET - Ativas e visíveis (mais usado no frontend)
    @GetMapping("/ativas-visiveis")
    public ResponseEntity<List<Secretaria>> listarAtivasEVisiveis() {
        return ResponseEntity.ok(secretariaService.buscarAtivasEVisiveis());
    }

    // ✅ GET - Buscar por nome
    @GetMapping("/buscar")
    public ResponseEntity<List<Secretaria>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(secretariaService.buscarPorNome(nome));
    }

    @GetMapping("/sigla/{sigla}")
    public ResponseEntity<Secretaria> buscarPorSigla(@PathVariable String sigla) {
        Secretaria secretaria = secretariaService.buscarPorSigla(sigla);

        if (secretaria == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(secretaria);
    }
}
