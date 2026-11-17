package dev.skillforge.lab.adapter.out.runtime;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import dev.skillforge.lab.domain.port.out.LabTerminalRuntime;
import dev.skillforge.lab.domain.port.out.TerminalChannel;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Ouvre un shell interactif dans un conteneur via l'API Docker (exec avec {@code Tty:true}).
 *
 * <p>Le pseudo-terminal est alloué <em>côté démon Docker</em> : aucun PTY client nécessaire. La
 * sortie du shell arrive par le callback et est relayée via {@code onOutput} ; l'entrée est écrite
 * dans un flux relié à l'exec. L'exec tourne en non-root dans {@code /workspace}, à l'intérieur du
 * conteneur déjà durci par {@link SandboxPolicy} (réseau none, caps, read-only, quotas).
 */
public class DockerJavaTerminalRuntime implements LabTerminalRuntime {

    private final DockerClient docker;

    public DockerJavaTerminalRuntime(DockerClient docker) {
        this.docker = docker;
    }

    @Override
    public TerminalChannel openShell(String containerId, int cols, int rows,
                                     Consumer<byte[]> onOutput, Runnable onExit) {
        String execId = docker.execCreateCmd(containerId)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withTty(true)
                .withUser("1000:1000")
                .withWorkingDir("/workspace")
                .withCmd("bash", "-l")
                .exec()
                .getId();

        PipedOutputStream stdinWriter = new PipedOutputStream();
        PipedInputStream stdin;
        try {
            stdin = new PipedInputStream(stdinWriter, 64 * 1024);
        } catch (IOException e) {
            throw new DockerCliLabRuntime.LabRuntimeException("Impossible d'ouvrir l'entree du terminal", e);
        }

        AtomicBoolean exited = new AtomicBoolean(false);
        Runnable fireExit = () -> {
            if (exited.compareAndSet(false, true)) {
                onExit.run();
            }
        };

        ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Frame frame) {
                byte[] payload = frame.getPayload();
                if (payload != null && payload.length > 0) {
                    onOutput.accept(payload);
                }
            }

            @Override
            public void onComplete() {
                fireExit.run();
            }

            @Override
            public void onError(Throwable throwable) {
                fireExit.run();
            }
        };

        docker.execStartCmd(execId)
                .withDetach(false)
                .withTty(true)
                .withStdIn(stdin)
                .exec(callback);

        resizeQuietly(execId, cols, rows);

        return new DockerTerminalChannel(execId, stdinWriter, callback);
    }

    private void resizeQuietly(String execId, int cols, int rows) {
        try {
            docker.resizeExecCmd(execId).withSize(Math.max(1, rows), Math.max(1, cols)).exec();
        } catch (RuntimeException ignored) {
            // best-effort : l'exec peut ne pas encore etre pret ; la taille par defaut reste utilisable
        }
    }

    /** Canal docker-java : écrit l'entrée dans le flux relié à l'exec, redimensionne, ferme. */
    private final class DockerTerminalChannel implements TerminalChannel {

        private final String execId;
        private final PipedOutputStream stdinWriter;
        private final ResultCallback.Adapter<Frame> callback;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        DockerTerminalChannel(String execId, PipedOutputStream stdinWriter,
                              ResultCallback.Adapter<Frame> callback) {
            this.execId = execId;
            this.stdinWriter = stdinWriter;
            this.callback = callback;
        }

        @Override
        public void write(byte[] data) {
            if (closed.get() || data == null || data.length == 0) {
                return;
            }
            try {
                stdinWriter.write(data);
                stdinWriter.flush();
            } catch (IOException ignored) {
                // flux ferme : rien a faire, la fermeture suivra
            }
        }

        @Override
        public void resize(int cols, int rows) {
            if (closed.get()) {
                return;
            }
            resizeQuietly(execId, cols, rows);
        }

        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            try {
                stdinWriter.close();
            } catch (IOException ignored) {
                // deja ferme
            }
            try {
                callback.close();
            } catch (IOException ignored) {
                // deja ferme
            }
        }
    }
}
