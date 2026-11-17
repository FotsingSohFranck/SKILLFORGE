package dev.skillforge.lab.domain.port.in;

import dev.skillforge.lab.domain.model.TerminalTicket;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Émet un ticket d'accès court pour ouvrir un terminal sur le nœud (si le nœud a une épreuve). */
public interface IssueTerminalTicketUseCase {

    Mono<TerminalTicket> issue(UUID learnerId, UUID nodeId, int cols, int rows);
}
