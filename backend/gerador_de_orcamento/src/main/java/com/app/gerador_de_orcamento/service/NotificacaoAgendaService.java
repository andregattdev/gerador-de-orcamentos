package com.app.gerador_de_orcamento.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.app.gerador_de_orcamento.dto.AgendaItemDTO;
import com.app.gerador_de_orcamento.repository.OrcamentoRepository;

@Service
public class NotificacaoAgendaService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoAgendaService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OrcamentoRepository orcamentoRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.notificacao.email-destino:}")
    private String emailDestino;

    @Value("${app.notificacao.habilitada:false}")
    private boolean notificacaoHabilitada;

    public NotificacaoAgendaService(OrcamentoRepository orcamentoRepository) {
        this.orcamentoRepository = orcamentoRepository;
    }

    @Scheduled(cron = "${app.notificacao.cron:0 0 8 * * *}")
    public void enviarLembretesAgenda() {
        if (!notificacaoHabilitada || mailSender == null || emailDestino == null || emailDestino.isBlank()) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite = agora.plusHours(24);
        List<AgendaItemDTO> proximos = orcamentoRepository.buscarAgendamentosEntre(agora, limite);

        if (proximos.isEmpty()) {
            return;
        }

        StringBuilder corpo = new StringBuilder("Serviços agendados para as próximas 24 horas:\n\n");
        for (AgendaItemDTO item : proximos) {
            corpo.append("- Cliente: ").append(item.getClienteNome())
                    .append(" | ").append(item.getDescricaoCurta())
                    .append(" | ").append(item.getDataAgendamento() != null
                            ? item.getDataAgendamento().format(FORMATTER) : "Sem data")
                    .append(" | Status: ").append(item.getStatus())
                    .append("\n");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailDestino);
            message.setSubject("Lembrete: serviços agendados");
            message.setText(corpo.toString());
            mailSender.send(message);
            log.info("Lembrete de agenda enviado para {}", emailDestino);
        } catch (Exception e) {
            log.warn("Falha ao enviar lembrete de agenda: {}", e.getMessage());
        }
    }
}
