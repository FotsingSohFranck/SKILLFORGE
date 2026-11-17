package dev.skillforge.lab.application;

import dev.skillforge.lab.domain.port.out.TerminalChannel;

/**
 * Terminal ouvert : canal d'E/S vers le shell + libération des ressources (arrêt du conteneur,
 * relâchement du permis de concurrence). {@code release} est idempotent.
 */
public record OpenedTerminal(TerminalChannel channel, Runnable release) {
}
