package dev.skillforge.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * Point d'entrée du monolithe modulaire SkillForge.
 * Scanne l'ensemble des contextes bornés sous {@code dev.skillforge} (composants et
 * repositories R2DBC), qui vivent hors du package de cette classe d'assemblage.
 *
 * <p>Le runtime est <b>R2DBC</b> (non bloquant). On exclut la datasource JDBC applicative :
 * seul Flyway utilise JDBC, via sa propre datasource construite depuis {@code spring.flyway.url}
 * (cf. docs/adr/0005-migrations-flyway.md).
 */
@SpringBootApplication(scanBasePackages = "dev.skillforge",
        exclude = DataSourceAutoConfiguration.class)
@EnableR2dbcRepositories(basePackages = "dev.skillforge")
public class SkillForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillForgeApplication.class, args);
    }
}
