package dev.skillforge.lab.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Spécification d'un environnement de pratique éphémère à démarrer.
 *
 * <p>Ne contient que l'intention « métier » (image, limites, TTL, session). Le durcissement
 * (réseau coupé, non-root, cap-drop, read-only, seccomp, etc.) est appliqué de façon
 * systématique et non contournable par {@code SandboxPolicy} côté adapter : l'appelant ne peut
 * pas affaiblir l'isolement.
 *
 * @param sessionId      identifiant de session (sert au nom du conteneur et au label de reaping)
 * @param image          image de base (ex. image pilote backend JDK/Maven)
 * @param cpus           quota CPU (ex. 1.0)
 * @param memoryMb       quota mémoire en Mo
 * @param pidsLimit      nombre maximal de processus
 * @param ttlSeconds     durée de vie maximale avant auto-extinction / reaping
 */
public record LabRuntimeSpec(
        String sessionId,
        String image,
        double cpus,
        int memoryMb,
        int pidsLimit,
        int ttlSeconds
) {
    /** Dossier de travail inscriptible (monté en tmpfs), racine du conteneur en lecture seule. */
    public static final String WORKDIR = "/workspace";

    public LabRuntimeSpec {
        requireText(sessionId, "sessionId");
        requireText(image, "image");
        if (cpus <= 0) throw new IllegalArgumentException("cpus doit etre > 0");
        if (memoryMb <= 0) throw new IllegalArgumentException("memoryMb doit etre > 0");
        if (pidsLimit <= 0) throw new IllegalArgumentException("pidsLimit doit etre > 0");
        if (ttlSeconds <= 0) throw new IllegalArgumentException("ttlSeconds doit etre > 0");
    }

    /** Quotas conseillés pour le pilote backend (cf. docs/lab/backend.md). */
    public static LabRuntimeSpec backendPilot(String sessionId, String image) {
        return new LabRuntimeSpec(sessionId, image, 1.0, 768, 256, 900);
    }

    /** Nom de conteneur dérivé de la session (préfixe reconnaissable par le reaper). */
    public String containerName() {
        return "sf-lab-" + sessionId;
    }

    /** Commande maintenant le conteneur en vie au plus TTL secondes (auto-extinction). */
    public List<String> keepAliveCommand() {
        return List.of("sleep", Integer.toString(ttlSeconds));
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("LabRuntimeSpec." + field + " est requis");
        }
    }

    /** Label appliqué à tout conteneur de lab, pour l'inventaire et le reaping. */
    public static final String LABEL = "skillforge.lab";
}
