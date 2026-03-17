package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.model.Agendamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async // Envia em uma thread separada para não travar o salvamento
    public void enviarEmailConfirmacao(Agendamento ag) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("nao-responda-2@saoluis.ma.gov.br");
            message.setTo(ag.getEmail());
            message.setSubject("Confirmação de Agendamento - " + ag.getSecretaria().getNome());

            String corpo = String.format(
                    "Olá, %s!\n\n" +
                            "Seu agendamento foi realizado com sucesso.\n\n" +
                            "📍 Local: %s\n" +
                            "📅 Data: %s\n" +
                            "⏰ Hora: %s\n" +
                            "🎟️ Senha: %s\n\n" +
                            "Por favor, chegue com 15 minutos de antecedência.",
                    ag.getNomeCidadao(),
                    ag.getSetor().getNome(),
                    ag.getHoraAgendamento().toLocalDate(),
                    ag.getHoraAgendamento().toLocalTime(),
                    ag.getSenha()
            );

            message.setText(corpo);
            mailSender.send(message);
        } catch (Exception e) {
            // Apenas log de erro para não estornar a transação do banco caso o e-mail falhe
            System.err.println("Falha ao enviar e-mail: " + e.getMessage());
        }
    }
}
