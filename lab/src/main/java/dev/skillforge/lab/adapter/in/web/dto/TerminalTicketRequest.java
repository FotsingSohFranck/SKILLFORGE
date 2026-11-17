package dev.skillforge.lab.adapter.in.web.dto;

/** Demande d'ouverture de terminal : géométrie initiale souhaitée (optionnelle). */
public record TerminalTicketRequest(Integer cols, Integer rows) {
    public int colsOrDefault() {
        return cols == null || cols <= 0 ? 100 : cols;
    }

    public int rowsOrDefault() {
        return rows == null || rows <= 0 ? 30 : rows;
    }
}
