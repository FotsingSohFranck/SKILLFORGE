package dev.skillforge.lab.adapter.out.ticket;

import dev.skillforge.lab.domain.model.TerminalTicket;
import dev.skillforge.lab.domain.port.out.TerminalTicketStore;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stockage en mémoire des tickets de terminal (monolithe mono-instance). Les tickets sont à usage
 * unique et expirent vite ; la consommation les retire. Les expirés sont purgés paresseusement.
 */
@Component
public class InMemoryTerminalTicketStore implements TerminalTicketStore {

    private static final Duration TTL = Duration.ofSeconds(30);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ConcurrentHashMap<String, TerminalTicket> tickets = new ConcurrentHashMap<>();

    @Override
    public TerminalTicket issue(UUID learnerId, UUID nodeId, int cols, int rows) {
        purgeExpired();
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        TerminalTicket ticket = new TerminalTicket(value, learnerId, nodeId,
                clamp(cols, 20, 300), clamp(rows, 5, 100), Instant.now().plus(TTL));
        tickets.put(value, ticket);
        return ticket;
    }

    @Override
    public Optional<TerminalTicket> consume(String value) {
        if (value == null) {
            return Optional.empty();
        }
        TerminalTicket ticket = tickets.remove(value);
        if (ticket == null || ticket.isExpired(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(ticket);
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        tickets.values().removeIf(t -> t.isExpired(now));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
