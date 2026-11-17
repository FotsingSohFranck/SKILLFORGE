package dev.skillforge.lab.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.skillforge.lab.adapter.in.ws.LabTerminalWebSocketHandler;
import dev.skillforge.lab.application.LabTerminalService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

/**
 * Câblage du terminal interactif par WebSocket (SF-91f) : enregistre le handler sur
 * {@code /api/v1/lab/terminal} et l'adaptateur WebFlux. L'authentification se fait par ticket
 * court au handshake (le chemin est en accès libre côté filtre de sécurité, cf. IamSecurityConfig).
 */
@Configuration
public class LabWebSocketConfig {

    public static final String TERMINAL_PATH = "/api/v1/lab/terminal";

    @Bean
    public WebSocketHandler labTerminalWebSocketHandler(LabTerminalService terminals) {
        // Mapper dédié (une seule instance, réutilisée par le handler singleton) : sert uniquement à
        // parser les petites trames de contrôle JSON (resize). On n'injecte pas le bean ObjectMapper
        // car Spring Boot 4 autoconfigure un mapper Jackson 3 (tools.jackson.*), pas le Jackson 2
        // (com.fasterxml.jackson.databind) utilisé ici.
        return new LabTerminalWebSocketHandler(terminals, new ObjectMapper());
    }

    @Bean
    public HandlerMapping labTerminalHandlerMapping(WebSocketHandler labTerminalWebSocketHandler) {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Map.of(TERMINAL_PATH, labTerminalWebSocketHandler));
        // Avant les controllers annotés, pour que le handshake WS soit pris en premier.
        mapping.setOrder(-1);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
