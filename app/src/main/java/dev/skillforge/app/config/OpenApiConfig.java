package dev.skillforge.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Métadonnées OpenAPI exposées via springdoc (UI sur {@code /swagger-ui.html}). */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI skillForgeOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SkillForge API")
                .version("v1")
                .description("API de la plateforme e-learning locale SkillForge."));
    }
}
