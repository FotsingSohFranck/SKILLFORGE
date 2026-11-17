package dev.skillforge.lab.it;

import com.github.dockerjava.api.DockerClient;
import dev.skillforge.lab.adapter.in.ws.LabTerminalWebSocketHandler;
import dev.skillforge.lab.application.LabTerminalService;
import dev.skillforge.lab.config.LabWebSocketConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de fumée d'intégration du contexte {@code lab} (SF-91f, Tier A).
 *
 * <p>Démarre le <strong>contexte Spring complet du lab avec un vrai serveur WebFlux</strong> et
 * vérifie que tout le câblage du terminal interactif se construit : le handler WebSocket, le
 * {@link HandlerMapping} qui l'expose, l'adaptateur WebFlux, le client Docker (inerte) et le
 * service d'orchestration.
 *
 * <p>Ce test couvre précisément le trou qui a laissé passer le bug <em>« APPLICATION FAILED TO
 * START »</em> : {@code labTerminalWebSocketHandler} réclamait un bean {@code ObjectMapper}
 * (Jackson 2) absent sous Spring Boot 4. Un échec de câblage ici casse le build, au lieu d'être
 * découvert à chaud en navigateur.
 *
 * <p><strong>Volontairement sans Docker ni base réelle</strong> : c'est un test de câblage, pas
 * d'exécution. R2DBC pointe sur une URL inerte, pool désactivé — aucune connexion n'est ouverte
 * car aucun bean du lab ne requête la base au démarrage et le test n'exécute aucune requête. Il
 * tourne donc à chaque build, partout, sans flakiness. L'exécution réelle (round-trip WS + PTY
 * contre un vrai conteneur) relève du Tier B, Docker-gated.
 */
@SpringBootTest(classes = LabITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // URL R2DBC syntaxiquement valide mais jamais contactée (pool off => pas de connexion
                // au démarrage). Le port 1 garantit qu'aucune base locale n'est touchée par mégarde.
                "spring.r2dbc.url=r2dbc:postgresql://localhost:1/lab_boot_test",
                "spring.r2dbc.username=boot",
                "spring.r2dbc.password=boot",
                "spring.r2dbc.pool.enabled=false"
        })
class LabTerminalContextIntegrationTest {

    @Value("${local.server.port}")
    int port;

    private final ApplicationContext context;

    LabTerminalContextIntegrationTest(ApplicationContext context) {
        this.context = context;
    }

    @Test
    void le_contexte_demarre_et_cable_le_terminal() {
        // Le serveur WebFlux a bien démarré (port réel attribué).
        assertTrue(port > 0, "le serveur WebFlux doit avoir démarré sur un port réel");

        // Le handler WebSocket du terminal est câblé (c'est lui qui exigeait l'ObjectMapper absent).
        Object handler = context.getBean("labTerminalWebSocketHandler");
        assertInstanceOf(LabTerminalWebSocketHandler.class, handler,
                "le handler WebSocket du terminal doit être construit");
        assertInstanceOf(WebSocketHandler.class, handler);

        // Le mapping qui expose le terminal sur son chemin, avant les controllers annotés.
        HandlerMapping mapping = (HandlerMapping) context.getBean("labTerminalHandlerMapping");
        SimpleUrlHandlerMapping urlMapping = assertInstanceOf(SimpleUrlHandlerMapping.class, mapping);
        assertTrue(urlMapping.getUrlMap().containsKey(LabWebSocketConfig.TERMINAL_PATH),
                "le terminal doit être exposé sur " + LabWebSocketConfig.TERMINAL_PATH);

        // L'adaptateur WebFlux qui traite le handshake WebSocket (récupéré par son nom : Spring Boot
        // en autoconfigure déjà un, donc la résolution par type seule serait ambiguë).
        assertInstanceOf(WebSocketHandlerAdapter.class, context.getBean("webSocketHandlerAdapter"),
                "l'adaptateur WebSocket WebFlux doit être présent");

        // Le client Docker (inerte tant que le socket n'est pas sollicité) et le service d'orchestration.
        assertNotNull(context.getBean(DockerClient.class),
                "le client Docker doit être construit (sans ouvrir de connexion)");
        assertNotNull(context.getBean(LabTerminalService.class),
                "le service d'orchestration du terminal doit être câblé");
    }
}
