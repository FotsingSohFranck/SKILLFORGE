package dev.skillforge.iam.adapter.out.persistence.mapper;

import dev.skillforge.iam.adapter.out.persistence.entity.UserEntity;
import dev.skillforge.iam.domain.model.Email;
import dev.skillforge.iam.domain.model.PasswordHash;
import dev.skillforge.iam.domain.model.RoleCode;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;

import java.util.Set;

/** Traduction entité de persistance ↔ modèle de domaine (hors du domaine). */
public final class IamPersistenceMapper {

    private IamPersistenceMapper() {
    }

    public static User toDomain(UserEntity e, Set<RoleCode> roles) {
        return new User(
                new UserId(e.id()),
                new Email(e.email()),
                new PasswordHash(e.passwordHash()),
                e.displayName(),
                Boolean.TRUE.equals(e.enabled()),
                roles);
    }

    public static UserEntity toEntity(User u) {
        return new UserEntity(
                u.id().value(),
                u.email().value(),
                u.passwordHash().value(),
                u.displayName(),
                u.enabled());
    }
}
