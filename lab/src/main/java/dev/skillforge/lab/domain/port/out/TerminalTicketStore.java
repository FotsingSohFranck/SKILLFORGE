package dev.skillforge.lab.domain.port.out;

import dev.skillforge.lab.domain.model.TerminalTicket;

import java.util.Optional;
import java.util.UUID;

/** Port sortant : émission et consommation (usage unique) des tickets de terminal. */
public interface TerminalTicketStore {

    /** Émet un ticket à usage unique pour l'apprenant/nœud donnés, avec la géométrie initiale. */
    TerminalTicket issue(UUID learnerId, UUID nodeId, int cols, int rows);

    /** Consomme le ticket (le retire) s'il existe et n'est pas expiré, sinon {@link Optional#empty()}. */
    Optional<TerminalTicket> consume(String value);
}
