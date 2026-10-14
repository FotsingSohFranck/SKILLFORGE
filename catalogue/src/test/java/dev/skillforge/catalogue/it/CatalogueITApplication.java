package dev.skillforge.catalogue.it;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * Contexte d'application minimal pour les tests d'intégration du contexte Catalogue :
 * autoconfiguration réactive (WebFlux + R2DBC) + scan des composants du contexte.
 * Aucune datasource JDBC applicative (module JDBC absent du classpath) ; le schéma de
 * test est chargé par Testcontainers via son propre JDBC.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "dev.skillforge") // catalogue + common (handler d'erreurs global)
@EnableR2dbcRepositories(basePackages = "dev.skillforge.catalogue")
public class CatalogueITApplication {
}
