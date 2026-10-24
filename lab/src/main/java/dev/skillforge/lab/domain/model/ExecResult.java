package dev.skillforge.lab.domain.model;

/**
 * Résultat de l'exécution d'une commande (par exemple le script de validation) dans le conteneur.
 * Par convention, un {@code exitCode} de 0 signifie « réussi ».
 */
public record ExecResult(int exitCode, String stdout, String stderr) {

    public boolean passed() {
        return exitCode == 0;
    }
}
