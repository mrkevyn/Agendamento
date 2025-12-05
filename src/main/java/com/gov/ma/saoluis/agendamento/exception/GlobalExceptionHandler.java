package com.gov.ma.saoluis.agendamento.exception;

import com.gov.ma.saoluis.agendamento.DTO.ChamadaResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ChamadaResponseDTO> handleRuntimeException(RuntimeException ex) {
        // Retorna JSON consistente mesmo em erro
        return ResponseEntity.ok(new ChamadaResponseDTO(null, ex.getMessage()));
    }

}
