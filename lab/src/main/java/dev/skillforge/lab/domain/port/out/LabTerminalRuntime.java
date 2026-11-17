package dev.skillforge.lab.domain.port.out;

import java.util.function.Consumer;

/** Port sortant : ouverture d'un shell interactif (PTY) dans un conteneur de lab existant. */
public interface LabTerminalRuntime {

    /**
     * Ouvre un shell interactif ({@code bash}) dans le conteneur, en non-root, avec un pseudo-terminal.
     *
     * @param containerId conteneur cible (déjà démarré et durci)
     * @param cols        colonnes initiales
     * @param rows        lignes initiales
     * @param onOutput    reçoit les octets produits par le shell (sortie à relayer au client)
     * @param onExit      invoqué quand le shell se termine (fin de l'exec)
     * @return canal pour écrire l'entrée, redimensionner et fermer
     */
    TerminalChannel openShell(String containerId, int cols, int rows,
                              Consumer<byte[]> onOutput, Runnable onExit);
}
