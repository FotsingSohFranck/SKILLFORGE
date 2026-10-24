package dev.skillforge.lab.adapter.in.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Corps de requête : les fichiers soumis par l'apprenant pour l'épreuve pratique d'un nœud. */
public record LabSubmitRequest(@NotNull @NotEmpty List<FileInput> files) {

    public record FileInput(@NotNull String path, @NotNull String content) {
    }
}
