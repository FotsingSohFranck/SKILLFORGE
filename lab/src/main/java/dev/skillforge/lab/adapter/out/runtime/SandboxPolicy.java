package dev.skillforge.lab.adapter.out.runtime;

import dev.skillforge.lab.domain.model.LabRuntimeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Politique de bac à sable : construit les arguments {@code docker} en imposant TOUS les garde-fous
 * d'isolement de l'ADR-0008, sans qu'aucun appelant ne puisse les affaiblir.
 *
 * <p>Classe pure (sans effet de bord) afin d'être testable : les « tests d'isolation » vérifient
 * que chaque drapeau de durcissement est présent, et qu'aucun drapeau dangereux n'est émis.
 */
public final class SandboxPolicy {

    /** Utilisateur non privilégié par défaut dans le conteneur. */
    public static final String NON_ROOT_USER = "1000:1000";

    private SandboxPolicy() {
    }

    /**
     * Arguments de {@code docker run} pour démarrer un conteneur jetable et durci (détaché).
     * N'inclut JAMAIS {@code --privileged}, {@code --network host}, ni un montage du socket Docker.
     */
    public static List<String> runArgs(LabRuntimeSpec spec) {
        List<String> a = new ArrayList<>();
        a.add("run");
        a.add("--detach");
        a.add("--name");
        a.add(spec.containerName());

        // Identité pour l'inventaire / le reaping.
        a.add("--label");
        a.add(LabRuntimeSpec.LABEL + "=" + spec.sessionId());

        // Isolement réseau total par défaut (aucune sortie Internet).
        a.add("--network");
        a.add("none");

        // Quotas : CPU, mémoire (swap désactivé), nombre de processus.
        a.add("--cpus");
        a.add(formatCpus(spec.cpus()));
        a.add("--memory");
        a.add(spec.memoryMb() + "m");
        a.add("--memory-swap");
        a.add(spec.memoryMb() + "m"); // égal à --memory => pas de swap
        a.add("--pids-limit");
        a.add(Integer.toString(spec.pidsLimit()));

        // Durcissement des privilèges.
        a.add("--security-opt");
        a.add("no-new-privileges");
        a.add("--cap-drop");
        a.add("ALL");
        a.add("--user");
        a.add(NON_ROOT_USER);

        // Système de fichiers : racine en lecture seule + zone de travail inscriptible en tmpfs.
        a.add("--read-only");
        a.add("--tmpfs");
        a.add(LabRuntimeSpec.WORKDIR + ":rw,nosuid,nodev,size=64m,mode=1777");
        a.add("--tmpfs");
        a.add("/tmp:rw,nosuid,nodev,size=32m,mode=1777");
        a.add("--workdir");
        a.add(LabRuntimeSpec.WORKDIR);

        a.add(spec.image());
        a.addAll(spec.keepAliveCommand());
        return List.copyOf(a);
    }

    /** Arguments de {@code docker exec} pour lancer une commande dans le conteneur. */
    public static List<String> execArgs(String containerId, List<String> command) {
        List<String> a = new ArrayList<>();
        a.add("exec");
        a.add("--user");
        a.add(NON_ROOT_USER);
        a.add("--workdir");
        a.add(LabRuntimeSpec.WORKDIR);
        a.add(containerId);
        a.addAll(command);
        return List.copyOf(a);
    }

    /**
     * Arguments de {@code docker exec -i} pour écrire un fichier (chemin relatif au workspace)
     * depuis l'entrée standard, en créant les dossiers parents. Tourne en non-root dans le workspace.
     */
    public static List<String> writeFileArgs(String containerId, String relativePath) {
        return List.of(
                "exec", "--interactive",
                "--user", NON_ROOT_USER,
                "--workdir", LabRuntimeSpec.WORKDIR,
                containerId,
                "sh", "-c", "mkdir -p \"$(dirname \"$1\")\" && cat > \"$1\"", "sh", relativePath);
    }

    /** Arguments de {@code docker rm -f} (arrêt + suppression, idempotent). */
    public static List<String> removeArgs(String containerIdOrName) {
        return List.of("rm", "--force", containerIdOrName);
    }

    /** Arguments listant les conteneurs de lab (par label), pour le reaper. */
    public static List<String> listLabContainersArgs() {
        return List.of("ps", "--all", "--quiet", "--filter", "label=" + LabRuntimeSpec.LABEL);
    }

    /**
     * Arguments listant les conteneurs de lab <em>expirés</em> (label + état « exited ») : un
     * conteneur dont le TTL est atteint s'éteint tout seul (sa commande {@code sleep} se termine)
     * et se retrouve « exited » ; le reaper les supprime.
     */
    public static List<String> listExpiredArgs() {
        return List.of("ps", "--all", "--quiet",
                "--filter", "label=" + LabRuntimeSpec.LABEL,
                "--filter", "status=exited");
    }

    private static String formatCpus(double cpus) {
        return String.format(Locale.ROOT, "%.2f", cpus);
    }
}
