package dev.skillforge.lab.adapter.in.web.dto;

import dev.skillforge.lab.domain.model.TerminalTicket;

import java.time.Duration;
import java.time.Instant;

/** Réponse d'émission de ticket : valeur à présenter au handshake WebSocket + validité. */
public record TerminalTicketDto(String ticket, long expiresInSeconds) {
    public static TerminalTicketDto from(TerminalTicket t) {
        long seconds = Math.max(0, Duration.between(Instant.now(), t.expiresAt()).toSeconds());
        return new TerminalTicketDto(t.value(), seconds);
    }
}
