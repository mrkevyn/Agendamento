package com.gov.ma.saoluis.agendamento.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class UsuarioLogadoUtil {

    public static Long getUsuarioId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }

        if (auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }

        return null;
    }

    // Método para pegar o perfil (authorities) do usuário logado
    public static String getPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            return null;
        }

        // O perfil está na primeira authority (role) do usuário
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)  // Obtém a string de authority (por exemplo, ROLE_ADMIN)
                .findFirst()
                .orElse("USER");  // Se não tiver nenhuma authority, retorna USER por padrão
    }
}
