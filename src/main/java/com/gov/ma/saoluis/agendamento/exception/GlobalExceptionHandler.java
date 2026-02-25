package com.gov.ma.saoluis.agendamento.exception;

import com.gov.ma.saoluis.agendamento.DTO.ChamadaResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        // Cria um mapa simples para retornar como JSON: { "mensagem": "texto do erro" }
        Map<String, String> response = new HashMap<>();
        response.put("mensagem", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}