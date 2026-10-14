package dev.skillforge.iam.it;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * Contexte d'application minimal pour les tests d'intégration IAM :
 * autoconfiguration réactive (WebFlux + R2DBC + Security) + scan des composants.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "dev.skillforge") // iam + common (handler d'erreurs)
@EnableR2dbcRepositories(basePackages = "dev.skillforge.iam")
public class IamITApplication {
}
