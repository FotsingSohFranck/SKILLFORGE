package dev.skillforge.lab.adapter.out.runtime;

import dev.skillforge.lab.domain.model.LabRuntimeSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SandboxPolicyTest {

    private final LabRuntimeSpec spec =
            LabRuntimeSpec.backendPilot("s-123", "eclipse-temurin:21-jdk");

    /** Index de la valeur qui suit un drapeau donné dans la liste d'arguments. */
    private static String valueAfter(List<String> args, String flag) {
        int i = args.indexOf(flag);
        assertTrue(i >= 0 && i + 1 < args.size(), "drapeau absent: " + flag);
        return args.get(i + 1);
    }

    @Test
    void run_impose_tous_les_garde_fous_d_isolement() {
        List<String> args = SandboxPolicy.runArgs(spec);

        // Réseau coupé
        assertEquals("none", valueAfter(args, "--network"));
        // Quotas
        assertEquals("1.00", valueAfter(args, "--cpus"));
        assertEquals("768m", valueAfter(args, "--memory"));
        assertEquals("768m", valueAfter(args, "--memory-swap")); // == memory => pas de swap
        assertEquals("256", valueAfter(args, "--pids-limit"));
        // Privilèges
        assertEquals("no-new-privileges", valueAfter(args, "--security-opt"));
        assertEquals("ALL", valueAfter(args, "--cap-drop"));
        assertEquals(SandboxPolicy.NON_ROOT_USER, valueAfter(args, "--user"));
        // Système de fichiers
        assertTrue(args.contains("--read-only"));
        assertTrue(args.stream().anyMatch(a -> a.startsWith(LabRuntimeSpec.WORKDIR + ":rw")),
                "tmpfs du workspace manquant");
        // Le tmpfs doit être inscriptible par l'utilisateur non-root (mode 1777) tout en restant
        // durci (nosuid), sinon le dépôt des fichiers de l'apprenant échoue en « permission denied ».
        assertTrue(args.stream().anyMatch(a -> a.startsWith(LabRuntimeSpec.WORKDIR + ":")
                        && a.contains("mode=1777") && a.contains("nosuid")),
                "tmpfs du workspace doit être en mode=1777,nosuid");
        assertEquals(LabRuntimeSpec.WORKDIR, valueAfter(args, "--workdir"));
        // Détaché + label + image + keep-alive
        assertTrue(args.contains("--detach"));
        assertTrue(valueAfter(args, "--label").startsWith(LabRuntimeSpec.LABEL + "="));
        assertTrue(args.contains("eclipse-temurin:21-jdk"));
        assertTrue(args.contains("sleep"));
    }

    @Test
    void run_n_emet_jamais_de_drapeau_dangereux() {
        String flat = String.join(" ", SandboxPolicy.runArgs(spec));
        assertFalse(flat.contains("--privileged"), "privileged interdit");
        assertFalse(flat.contains("network host"), "network host interdit");
        assertFalse(flat.contains("docker.sock"), "montage du socket Docker interdit");
        assertFalse(flat.contains("/var/run"), "montage de /var/run interdit");
        assertFalse(flat.contains("--cap-add"), "cap-add interdit");
        assertFalse(flat.contains("--pid host"), "pid host interdit");
    }

    @Test
    void exec_tourne_en_non_root_dans_le_workspace() {
        List<String> args = SandboxPolicy.execArgs("cid", List.of("bash", "grader/check.sh"));
        assertEquals("exec", args.get(0));
        assertEquals(SandboxPolicy.NON_ROOT_USER, valueAfter(args, "--user"));
        assertEquals(LabRuntimeSpec.WORKDIR, valueAfter(args, "--workdir"));
        assertTrue(args.contains("cid"));
        assertTrue(args.contains("grader/check.sh"));
    }

    @Test
    void remove_force_la_suppression() {
        assertEquals(List.of("rm", "--force", "cid"), SandboxPolicy.removeArgs("cid"));
    }

    @Test
    void reap_cible_les_conteneurs_de_lab_expires() {
        List<String> args = SandboxPolicy.listExpiredArgs();
        String flat = String.join(" ", args);
        assertTrue(flat.contains("label=" + LabRuntimeSpec.LABEL));
        assertTrue(flat.contains("status=exited"));
    }
}
