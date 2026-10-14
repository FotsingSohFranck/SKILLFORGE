package dev.skillforge.iam.domain.port.in;

/** Commande d'inscription (données brutes du bord, non encore validées comme VO). */
public record RegisterUserCommand(String email, String rawPassword, String displayName) {
}
