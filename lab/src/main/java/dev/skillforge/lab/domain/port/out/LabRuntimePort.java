package dev.skillforge.lab.domain.port.out;

import dev.skillforge.lab.domain.model.ExecResult;
import dev.skillforge.lab.domain.model.LabRuntimeSpec;
import dev.skillforge.lab.domain.model.RunningLab;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Port sortant : cycle de vie d'un environnement de pratique éphémère et durci.
 *
 * <p>L'implémentation garantit l'isolement (réseau coupé, quotas, non-root, cap-drop, racine en
 * lecture seule, TTL) : le domaine ne manipule jamais directement de primitive de conteneur.
 */
public interface LabRuntimePort {

    /** Démarre un conteneur jetable selon la spécification (durcissement appliqué d'office). */
    Mono<RunningLab> start(LabRuntimeSpec spec);

    /** Écrit un fichier (chemin relatif au workspace) dans le conteneur, en créant les dossiers. */
    Mono<Void> putFile(RunningLab lab, String relativePath, String content);

    /** Exécute une commande dans le conteneur et renvoie son code de sortie et sa sortie. */
    Mono<ExecResult> exec(RunningLab lab, List<String> command);

    /** Arrête et supprime le conteneur (idempotent). */
    Mono<Void> stop(RunningLab lab);

    /** Supprime les conteneurs de lab orphelins dont le TTL est dépassé (filet de sécurité). */
    Mono<Integer> reapExpired();
}
