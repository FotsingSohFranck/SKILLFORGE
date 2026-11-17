package dev.skillforge.challenge.it;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Contexte d'application minimal pour les tests d'intégration du contexte {@code challenge} :
 * autoconfiguration réactive (R2DBC) + scan des composants {@code challenge} et {@code common}.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"dev.skillforge.challenge", "dev.skillforge.common"})
public class ChallengeITApplication {
}
