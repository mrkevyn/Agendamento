package com.gov.ma.saoluis.agendamento.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET =
            "MINHA_CHAVE_SUPER_SECRETA_DEVE_TER_32_CARACTERES";

    private static final long EXPIRACAO = 1000 * 60 * 60 * 8; // 8 horas

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String gerarToken(Long usuarioId, String perfil, Long secretariaId) {

        return Jwts.builder()
                .setSubject(usuarioId.toString())
                .claim("perfil", perfil)
                .claim("secretariaId", secretariaId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRACAO))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validarToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUsuarioId(String token) {
        return Long.valueOf(validarToken(token).getSubject());
    }

    public String getPerfil(String token) {
        return validarToken(token).get("perfil", String.class);
    }

    public Long getSecretariaId(String token) {
        return validarToken(token).get("secretariaId", Long.class);
    }
}
