package dev.skillforge.lab.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Ticket d'accès à usage unique et à durée de vie courte pour ouvrir un terminal (SF-91f).
 *
 * <p>Émis par un appel authentifié, puis présenté au handshake WebSocket (le JWT ne circule pas
 * dans l'URL). Lié à un apprenant + un nœud, avec la géométrie initiale du terminal.
 *
 * @param value     valeur opaque du ticket (aléatoire)
 * @param learnerId apprenant autorisé
 * @param nodeId    nœud concerné (détermine l'image de lab)
 * @param cols      colonnes initiales du terminal
 * @param rows      lignes initiales du terminal
 * @param expiresAt instant d'expiration
 */
public record TerminalTicket(
        String value,
        UUID learnerId,
        UUID nodeId,
        int cols,
        int rows,
        Instant expiresAt
) {
    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
