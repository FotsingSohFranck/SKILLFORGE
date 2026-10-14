package dev.skillforge.iam.domain.model;

import dev.skillforge.common.id.UuidV7;

import java.util.EnumSet;
import java.util.Set;

/**
 * Agrégat racine <b>User</b>. {@code id} est généré en Java (UUID v7) à l'inscription car
 * il est nécessaire pour insérer les rôles associés dans la même opération.
 */
public record User(
        UserId id,
        Email email,
        PasswordHash passwordHash,
        String displayName,
        boolean enabled,
        Set<RoleCode> roles) {

    public User {
        roles = roles == null ? EnumSet.noneOf(RoleCode.class) : EnumSet.copyOf(roles);
    }

    /** Crée un nouvel utilisateur inscrit (rôle LEARNER, actif), avec un identifiant UUID v7. */
    public static User register(Email email, PasswordHash passwordHash, String displayName) {
        return new User(
                new UserId(UuidV7.generate()),
                email,
                passwordHash,
                displayName,
                true,
                EnumSet.of(RoleCode.LEARNER));
    }

    public boolean hasRole(RoleCode role) {
        return roles.contains(role);
    }
}
