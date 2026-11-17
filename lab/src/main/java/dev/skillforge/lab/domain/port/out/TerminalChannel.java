package dev.skillforge.lab.domain.port.out;

/**
 * Canal bidirectionnel vers un shell interactif tournant dans un conteneur de lab (SF-91f).
 *
 * <p>Abstraction infra-neutre : l'adaptateur (API Docker) relaie les octets vers/depuis le PTY ;
 * la sortie est pousssée via le consommateur fourni à l'ouverture. Fermeture idempotente.
 */
public interface TerminalChannel {

    /** Envoie des octets (frappes clavier) vers l'entrée du shell. */
    void write(byte[] data);

    /** Redimensionne le pseudo-terminal (propagé au shell, déclenche SIGWINCH). */
    void resize(int cols, int rows);

    /** Ferme le shell (l'exec se termine). Idempotent. */
    void close();
}
