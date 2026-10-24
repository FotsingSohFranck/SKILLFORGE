package dev.skillforge.lab.domain.port.in;

/**
 * Un fichier soumis par l'apprenant, à déposer dans l'espace de travail.
 *
 * @param path    chemin relatif au workspace (jamais absolu, jamais de remontée {@code ..})
 * @param content contenu texte du fichier
 */
public record SubmittedFile(String path, String content) {
    public SubmittedFile {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("SubmittedFile.path est requis");
        }
        String normalized = path.replace('\\', '/');
        if (normalized.startsWith("/") || normalized.contains("..")) {
            throw new IllegalArgumentException("SubmittedFile.path doit etre relatif et sans '..': " + path);
        }
        if (content == null) {
            throw new IllegalArgumentException("SubmittedFile.content est requis");
        }
    }
}
