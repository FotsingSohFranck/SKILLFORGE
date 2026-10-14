package dev.skillforge.gamification.domain.port.out;

import dev.skillforge.gamification.domain.model.Level;
import reactor.core.publisher.Flux;

/** Port sortant : paliers de niveau (barème), ordonnés par seuil croissant. */
public interface LevelRepository {
    Flux<Level> findAllOrdered();
}
