package dev.skillforge.lab.adapter.out.runtime;

import dev.skillforge.lab.domain.model.ExecResult;
import dev.skillforge.lab.domain.model.LabRuntimeSpec;
import dev.skillforge.lab.domain.model.RunningLab;
import dev.skillforge.lab.domain.port.out.LabRuntimePort;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Adapter sortant : orchestre des conteneurs jetables via la CLI {@code docker}.
 *
 * <p>Tout le durcissement provient de {@link SandboxPolicy} (classe pure, testée). Cet adapter ne
 * fait qu'exécuter les commandes construites, de façon réactive (hors du thread événementiel).
 *
 * <p>Non câblé en bean tant que l'accès au démon Docker n'est pas explicitement accordé (voir
 * ADR-0008 / SF-91) : on n'active pas la capacité quasi-root par défaut.
 */
public class DockerCliLabRuntime implements LabRuntimePort {

    private final String dockerBin;
    private final Duration commandTimeout;

    public DockerCliLabRuntime() {
        this("docker", Duration.ofSeconds(120));
    }

    public DockerCliLabRuntime(String dockerBin, Duration commandTimeout) {
        this.dockerBin = dockerBin;
        this.commandTimeout = commandTimeout;
    }

    @Override
    public Mono<RunningLab> start(LabRuntimeSpec spec) {
        return Mono.fromCallable(() -> {
            CommandOutcome out = run(SandboxPolicy.runArgs(spec));
            if (out.exitCode() != 0) {
                throw new LabRuntimeException("Echec du demarrage du conteneur de lab: " + out.stderr().strip());
            }
            String containerId = out.stdout().strip();
            return new RunningLab(containerId, spec);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> putFile(RunningLab lab, String relativePath, String content) {
        return Mono.fromRunnable(() -> {
            CommandOutcome out = runWithInput(SandboxPolicy.writeFileArgs(lab.containerId(), relativePath), content);
            if (out.exitCode() != 0) {
                throw new LabRuntimeException("Echec d'ecriture du fichier " + relativePath + ": " + out.stderr().strip());
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<ExecResult> exec(RunningLab lab, List<String> command) {
        return Mono.fromCallable(() -> {
            CommandOutcome out = run(SandboxPolicy.execArgs(lab.containerId(), command));
            return new ExecResult(out.exitCode(), out.stdout(), out.stderr());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> stop(RunningLab lab) {
        return Mono.fromRunnable(() -> run(SandboxPolicy.removeArgs(lab.containerId())))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<Integer> reapExpired() {
        return Mono.fromCallable(() -> {
            CommandOutcome listed = run(SandboxPolicy.listExpiredArgs());
            if (listed.exitCode() != 0) {
                return 0;
            }
            int removed = 0;
            for (String id : listed.stdout().split("\\R")) {
                if (!id.isBlank()) {
                    run(SandboxPolicy.removeArgs(id.strip()));
                    removed++;
                }
            }
            return removed;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /** Exécute {@code docker <args>} et récupère code de sortie, stdout, stderr (sans lever sur code != 0). */
    private CommandOutcome run(List<String> dockerArgs) {
        List<String> command = new ArrayList<>(dockerArgs.size() + 1);
        command.add(dockerBin);
        command.addAll(dockerArgs);
        try {
            Process process = new ProcessBuilder(command).start();
            String stdout = read(process, false);
            String stderr = read(process, true);
            boolean finished = process.waitFor(commandTimeout.toSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new LabRuntimeException("Commande docker expiree: " + String.join(" ", dockerArgs));
            }
            return new CommandOutcome(process.exitValue(), stdout, stderr);
        } catch (LabRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LabRuntimeException("Erreur d'execution docker: " + e.getMessage(), e);
        }
    }

    /** Comme {@link #run(List)}, mais alimente l'entrée standard avec {@code input}. */
    private CommandOutcome runWithInput(List<String> dockerArgs, String input) {
        List<String> command = new ArrayList<>(dockerArgs.size() + 1);
        command.add(dockerBin);
        command.addAll(dockerArgs);
        try {
            Process process = new ProcessBuilder(command).start();
            try (var stdin = process.getOutputStream()) {
                stdin.write(input.getBytes(StandardCharsets.UTF_8));
                stdin.flush();
            }
            String stdout = read(process, false);
            String stderr = read(process, true);
            boolean finished = process.waitFor(commandTimeout.toSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new LabRuntimeException("Commande docker expiree: " + String.join(" ", dockerArgs));
            }
            return new CommandOutcome(process.exitValue(), stdout, stderr);
        } catch (LabRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LabRuntimeException("Erreur d'execution docker: " + e.getMessage(), e);
        }
    }

    private static String read(Process process, boolean error) {
        var stream = error ? process.getErrorStream() : process.getInputStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (Exception ignored) {
            // flux fermé : on renvoie ce qui a été lu
        }
        return sb.toString();
    }

    private record CommandOutcome(int exitCode, String stdout, String stderr) {
    }

    /** Erreur d'orchestration (démarrage/suppression impossibles). */
    public static final class LabRuntimeException extends RuntimeException {
        public LabRuntimeException(String message) {
            super(message);
        }

        public LabRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
