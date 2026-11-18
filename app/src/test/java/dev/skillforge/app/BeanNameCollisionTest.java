package dev.skillforge.app;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Garde-fou d'assemblage : rejoue le <strong>component-scan</strong> de l'application complète
 * ({@code dev.skillforge}, comme {@link SkillForgeApplication}) et échoue si deux composants de
 * contextes bornés différents produisent le même nom de bean par défaut
 * ({@code ConflictingBeanDefinitionException}).
 *
 * <p>Ce conflit est invisible aux tests par module (chacun ne scanne que son propre contexte) :
 * il n'apparaît qu'au démarrage du monolithe. Le scan seul suffit à le détecter — aucune base ni
 * Docker n'est nécessaire, puisque l'erreur survient à l'enregistrement des définitions de beans,
 * avant toute instanciation.
 */
class BeanNameCollisionTest {

    @Test
    void aucun_conflit_de_nom_de_bean_entre_contextes_bornes() {
        assertDoesNotThrow(() -> {
            try (var ctx = new AnnotationConfigApplicationContext()) {
                new ClassPathBeanDefinitionScanner(ctx).scan("dev.skillforge");
            }
        });
    }
}
