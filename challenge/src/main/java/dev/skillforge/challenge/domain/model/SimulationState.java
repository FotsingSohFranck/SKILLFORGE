package dev.skillforge.challenge.domain.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * État mutable de la simulation réduit à des variables entières (ex. {@code latency}, {@code staleRisk}).
 * Immuable : {@link #apply(List)} produit un nouvel état. Une variable absente vaut 0.
 */
public record SimulationState(Map<String, Integer> variables) {

    public SimulationState {
        variables = variables == null ? Map.of() : Map.copyOf(variables);
    }

    public static SimulationState empty() {
        return new SimulationState(Map.of());
    }

    public int get(String variable) {
        return variables.getOrDefault(variable, 0);
    }

    /** Applique en séquence les effets d'un choix et renvoie le nouvel état. */
    public SimulationState apply(List<Effect> effects) {
        if (effects == null || effects.isEmpty()) {
            return this;
        }
        Map<String, Integer> next = new LinkedHashMap<>(variables);
        for (Effect e : effects) {
            int current = next.getOrDefault(e.variable(), 0);
            int value = switch (e.op()) {
                case SET -> e.value();
                case INCREASE -> current + e.value();
                case DECREASE -> current - e.value();
            };
            next.put(e.variable(), value);
        }
        return new SimulationState(next);
    }
}
