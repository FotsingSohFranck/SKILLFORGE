package dev.skillforge.contenu.it;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/** Contexte d'application minimal pour les tests d'intégration du contexte Contenu. */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "dev.skillforge")
@EnableR2dbcRepositories(basePackages = "dev.skillforge.contenu")
public class ContentITApplication {
}
