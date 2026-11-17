package dev.skillforge.lab.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.common.id.UuidV7;
import dev.skillforge.lab.domain.model.LabRuntimeSpec;
import dev.skillforge.lab.domain.model.TerminalTicket;
import dev.skillforge.lab.domain.port.in.IssueTerminalTicketUseCase;
import dev.skillforge.lab.domain.port.out.LabRuntimePort;
import dev.skillforge.lab.domain.port.out.LabTerminalRuntime;
import dev.skillforge.lab.domain.port.out.PracticalTaskStore;
import dev.skillforge.lab.domain.port.out.TerminalChannel;
import dev.skillforge.lab.domain.port.out.TerminalTicketStore;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Orchestration du terminal interactif (SF-91f) : émission de ticket, puis ouverture d'une
 * session durcie (conteneur éphémère + shell PTY) après consommation du ticket.
 *
 * <p>Plafond de concurrence strict (machine locale) : au-delà, l'ouverture est refusée. Le
 * conteneur est démarré via {@link LabRuntimePort} (durci par {@code SandboxPolicy}, auto-extinction
 * au TTL) et le shell ouvert via {@link LabTerminalRuntime}. À la fermeture (déconnexion ou fin du
 * shell), le conteneur est arrêté et le permis relâché.
 */
public class LabTerminalService implements IssueTerminalTicketUseCase {

    private final TerminalTicketStore tickets;
    private final PracticalTaskStore tasks;
    private final LabRuntimePort runtime;
    private final LabTerminalRuntime terminalRuntime;
    private final Semaphore permits;

    public LabTerminalService(TerminalTicketStore tickets, PracticalTaskStore tasks,
                              LabRuntimePort runtime, LabTerminalRuntime terminalRuntime,
                              int maxConcurrent) {
        this.tickets = tickets;
        this.tasks = tasks;
        this.runtime = runtime;
        this.terminalRuntime = terminalRuntime;
        this.permits = new Semaphore(Math.max(1, maxConcurrent));
    }

    @Override
    public Mono<TerminalTicket> issue(UUID learnerId, UUID nodeId, int cols, int rows) {
        return tasks.findByNode(nodeId)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "PRACTICAL_TASK_NOT_FOUND", "Ce nœud n'a pas d'épreuve pratique.")))
                .map(task -> tickets.issue(learnerId, nodeId, cols, rows));
    }

    /**
     * Consomme le ticket et ouvre un terminal. {@code onOutput} reçoit la sortie du shell,
     * {@code onExit} est invoqué si le shell se termine de lui-même.
     */
    public Mono<OpenedTerminal> open(String ticketValue, Consumer<byte[]> onOutput, Runnable onExit) {
        return Mono.defer(() -> tickets.consume(ticketValue)
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new TerminalAccessException(
                                "Ticket de terminal invalide ou expiré."))))
                .flatMap(ticket -> tasks.findByNode(ticket.nodeId())
                        .switchIfEmpty(Mono.error(new NotFoundException(
                                "PRACTICAL_TASK_NOT_FOUND", "Ce nœud n'a pas d'épreuve pratique.")))
                        .flatMap(task -> startAndAttach(ticket, task.image(), onOutput, onExit)));
    }

    private Mono<OpenedTerminal> startAndAttach(TerminalTicket ticket, String image,
                                               Consumer<byte[]> onOutput, Runnable onExit) {
        if (!permits.tryAcquire()) {
            return Mono.error(new TerminalBusyException(
                    "Trop de terminaux ouverts actuellement, réessaie dans un instant."));
        }
        AtomicBoolean released = new AtomicBoolean(false);
        Runnable releasePermit = () -> {
            if (released.compareAndSet(false, true)) {
                permits.release();
            }
        };
        LabRuntimeSpec spec = LabRuntimeSpec.backendPilot(UuidV7.generate().toString(), image);
        return runtime.start(spec)
                .map(lab -> {
                    AtomicBoolean cleaned = new AtomicBoolean(false);
                    Runnable release = () -> {
                        if (cleaned.compareAndSet(false, true)) {
                            runtime.stop(lab).subscribe();
                            releasePermit.run();
                        }
                    };
                    try {
                        TerminalChannel channel = terminalRuntime.openShell(
                                lab.containerId(), ticket.cols(), ticket.rows(), onOutput,
                                () -> {
                                    onExit.run();
                                    release.run();
                                });
                        return new OpenedTerminal(channel, release);
                    } catch (RuntimeException e) {
                        release.run();
                        throw e;
                    }
                })
                .onErrorResume(err -> {
                    releasePermit.run();
                    return Mono.error(err);
                });
    }

    /** Ticket invalide/expiré. */
    public static final class TerminalAccessException extends RuntimeException {
        public TerminalAccessException(String message) {
            super(message);
        }
    }

    /** Capacité de terminaux saturée. */
    public static final class TerminalBusyException extends RuntimeException {
        public TerminalBusyException(String message) {
            super(message);
        }
    }
}
