package dev.skillforge.lab.adapter.in.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.skillforge.lab.application.LabTerminalService;
import dev.skillforge.lab.domain.port.out.TerminalChannel;
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
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(msg -> handleInbound(msg, opened.channel()))
                            .then();

                    return Mono.firstWithSignal(send, receive)
                            .doFinally(sig -> {
                                opened.channel().close();
                                opened.release().run();
                            })
                            .then();
                })
                .onErrorResume(err -> session
                        .close(CloseStatus.POLICY_VIOLATION.withReason(shortReason(err))));
    }

    private void handleInbound(WebSocketMessage message, TerminalChannel channel) {
        if (message.getType() == WebSocketMessage.Type.TEXT) {
            tryResize(message.getPayloadAsText(), channel);
            return;
        }
        if (message.getType() == WebSocketMessage.Type.BINARY) {
            DataBuffer buffer = message.getPayload();
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            channel.write(bytes);
        }
    }

    private void tryResize(String text, TerminalChannel channel) {
        try {
            JsonNode node = json.readTree(text);
            JsonNode resize = node.get("resize");
            if (resize != null) {
                int cols = resize.path("cols").asInt(0);
                int rows = resize.path("rows").asInt(0);
                if (cols > 0 && rows > 0) {
                    channel.resize(cols, rows);
                }
            }
        } catch (Exception ignored) {
            // trame de contrôle invalide : ignorée
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
