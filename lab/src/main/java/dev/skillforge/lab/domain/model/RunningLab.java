package dev.skillforge.lab.domain.model;

import java.util.Objects;

/** Référence vers un environnement de pratique en cours d'exécution. */
public record RunningLab(String containerId, LabRuntimeSpec spec) {
    public RunningLab {
        Objects.requireNonNull(containerId, "RunningLab.containerId");
        Objects.requireNonNull(spec, "RunningLab.spec");
    }
}
