package dev.skillforge.lab.it;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * Contexte d'application minimal pour les tests d'intégration du contexte {@code lab} :
 * autoconfiguration réactive (WebFlux + R2DBC) + scan des composants {@code lab} et {@code common}.
 *
 * <p>On ne scanne volontairement <em>pas</em> tout {@code dev.skillforge} : inutile de tirer la
 * sécurité/JWT et le mailer de {@code iam} pour vérifier le câblage du terminal. Le but est de
 * démarrer le serveur et de prouver que les beans du terminal (WebSocket + client Docker) se
 * construisent — le garde-fou qui aurait bloqué au build l'absence du bean {@code ObjectMapper}.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"dev.skillforge.lab", "dev.skillforge.common"})
@EnableR2dbcRepositories(basePackages = "dev.skillforge.lab")
public class LabITApplication {
}
