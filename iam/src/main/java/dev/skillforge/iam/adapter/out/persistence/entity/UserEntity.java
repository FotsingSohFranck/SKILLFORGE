package dev.skillforge.iam.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/** Entité de persistance R2DBC de la table {@code users} (colonnes utiles à l'IAM). */
@Table("users")
public record UserEntity(
        @Id UUID id,
        String email,
        @Column("password_hash") String passwordHash,
        @Column("display_name") String displayName,
        Boolean enabled) {
}
