package dev.skillforge.lab.adapter.in.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.skillforge.lab.application.LabTerminalService;
import dev.skillforge.lab.domain.port.out.TerminalChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Pont WebSocket ⇄ shell interactif (SF-91f).
 *
 * <p>Authentifié par un <em>ticket</em> court passé en paramètre de requête (le JWT ne circule pas
 * dans l'URL). Les trames binaires sont les frappes clavier (vers le shell) ; les trames texte sont
 * des contrôles JSON (redimensionnement). La sortie du shell est renvoyée en trames binaires.
 */
public class LabTerminalWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(LabTerminalWebSocketHandler.class);

    private final LabTerminalService terminals;
    private final ObjectMapper json;

    public LabTerminalWebSocketHandler(LabTerminalService terminals, ObjectMapper json) {
        this.terminals = terminals;
        this.json = json;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String ticket = queryParam(session.getHandshakeInfo().getUri(), "ticket");

        Sinks.Many<byte[]> output = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.EmitFailureHandler emitHandler = Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(20));

        return terminals.open(
                        ticket,
                        bytes -> output.emitNext(bytes, emitHandler),
                        () -> output.emitComplete(emitHandler))
                .flatMap(opened -> {
                    Mono<Void> send = session.send(
                            output.asFlux().map(b -> session.binaryMessage(f -> f.wrap(b))));

                    Mono<Void> receive = session.receive()
                            // Décodage synchrone sur le thread d'émission : le DataBuffer Netty y est
                            // encore valide (libéré juste après l'onNext). On copie les octets ici avant
                            // de basculer sur boundedElastic pour l'écriture bloquante (sinon refCnt=0).
                            .map(this::decode)
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(in -> apply(in, opened.channel()))
                            .then();

                    return Mono.firstWithSignal(send, receive)
                            .doFinally(sig -> {
                                opened.channel().close();
                                opened.release().run();
                            })
                            .then();
                })
                .onErrorResume(err -> {
                    log.warn("Ouverture du terminal echouee (ticket={})", ticket, err);
                    return session.close(CloseStatus.POLICY_VIOLATION.withReason(shortReason(err)));
                });
    }

    /**
     * Décode une trame entrante <em>sur le thread d'émission</em> (le {@link DataBuffer} y est encore
     * valide). Binaire = frappes clavier ; texte = contrôle JSON (resize). Les octets sont copiés ici
     * pour pouvoir appliquer l'action plus tard sur un autre scheduler sans toucher au buffer libéré.
     */
    private Inbound decode(WebSocketMessage message) {
        if (message.getType() == WebSocketMessage.Type.BINARY) {
            DataBuffer buffer = message.getPayload();
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            return Inbound.keys(bytes);
        }
        if (message.getType() == WebSocketMessage.Type.TEXT) {
            return parseResize(message.getPayloadAsText());
        }
        return Inbound.NONE;
    }

    /** Applique l'action décodée (écriture bloquante / resize), hors du thread d'émission. */
    private void apply(Inbound in, TerminalChannel channel) {
        if (in.keystrokes() != null) {
            channel.write(in.keystrokes());
        } else if (in.cols() > 0 && in.rows() > 0) {
            channel.resize(in.cols(), in.rows());
        }
    }

    private Inbound parseResize(String text) {
        try {
            JsonNode node = json.readTree(text);
            JsonNode resize = node.get("resize");
            if (resize != null) {
                int cols = resize.path("cols").asInt(0);
                int rows = resize.path("rows").asInt(0);
                if (cols > 0 && rows > 0) {
                    return Inbound.resize(cols, rows);
                }
            }
        } catch (Exception ignored) {
            // trame de contrôle invalide : ignorée
        }
        return Inbound.NONE;
    }

    /** Trame entrante décodée : soit des frappes clavier, soit un resize, soit rien. */
    private record Inbound(byte[] keystrokes, int cols, int rows) {
        private static final Inbound NONE = new Inbound(null, 0, 0);

        private static Inbound keys(byte[] bytes) {
            return new Inbound(bytes, 0, 0);
        }

        private static Inbound resize(int cols, int rows) {
            return new Inbound(null, cols, rows);
        }
    }

    private static String queryParam(URI uri, String name) {
        String query = uri.getQuery();
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) {
                return pair.substring(eq + 1);
            }
        }
        return null;
    }

    private static String shortReason(Throwable err) {
        String msg = err.getMessage() == null ? "Terminal indisponible" : err.getMessage();
        byte[] utf8 = msg.getBytes(StandardCharsets.UTF_8);
        // CloseStatus reason limité à 123 octets.
        if (utf8.length <= 120) {
            return msg;
        }
        return msg.substring(0, Math.min(msg.length(), 100));
    }
}
