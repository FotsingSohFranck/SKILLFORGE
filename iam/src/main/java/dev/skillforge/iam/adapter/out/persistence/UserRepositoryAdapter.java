package dev.skillforge.iam.adapter.out.persistence;

import dev.skillforge.iam.adapter.out.persistence.entity.UserEntity;
import dev.skillforge.iam.adapter.out.persistence.mapper.IamPersistenceMapper;
import dev.skillforge.iam.domain.model.Email;
import dev.skillforge.iam.domain.model.PasswordHash;
import dev.skillforge.iam.domain.model.RoleCode;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;
import dev.skillforge.iam.domain.port.out.UserRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur R2DBC du port {@link UserRepository}.
 * L'INSERT est forcé via {@link R2dbcEntityTemplate#insert} (l'id v7 est fourni par le
 * domaine), ce qui évite l'ambiguïté new/update de Spring Data quand l'id est présent.
 */
@Component
public class UserRepositoryAdapter implements UserRepository {

    private static final String ROLES_SQL =
            "SELECT r.code FROM user_roles ur JOIN roles r ON r.id = ur.role_id WHERE ur.user_id = :uid";
    private static final String INSERT_ROLE_SQL =
            "INSERT INTO user_roles(user_id, role_id) SELECT :uid, id FROM roles WHERE code = :code";

    private final R2dbcEntityTemplate template;
    private final DatabaseClient db;

    public UserRepositoryAdapter(R2dbcEntityTemplate template) {
        this.template = template;
        this.db = template.getDatabaseClient();
    }

    @Override
    public Mono<Boolean> existsByEmail(Email email) {
        return template.exists(
                Query.query(Criteria.where("email").is(email.value())), UserEntity.class);
    }

    @Override
    public Mono<User> findByEmail(Email email) {
        return template.selectOne(
                        Query.query(Criteria.where("email").is(email.value())), UserEntity.class)
                .flatMap(this::withRoles);
    }

    @Override
    public Mono<User> findById(UserId id) {
        return template.selectOne(
                        Query.query(Criteria.where("id").is(id.value())), UserEntity.class)
                .flatMap(this::withRoles);
    }

    @Override
    public Mono<User> save(User user) {
        return template.insert(IamPersistenceMapper.toEntity(user))
                .thenMany(Flux.fromIterable(user.roles()))
                .flatMap(role -> db.sql(INSERT_ROLE_SQL)
                        .bind("uid", user.id().value())
                        .bind("code", role.name())
                        .fetch().rowsUpdated())
                .then(Mono.just(user));
    }

    @Override
    public Flux<User> findAll() {
        return template.select(UserEntity.class).all()
                .concatMap(this::withRoles);
    }

    @Override
    public Mono<Void> replaceRoles(UserId id, Set<RoleCode> roles) {
        return db.sql("DELETE FROM user_roles WHERE user_id = :uid")
                .bind("uid", id.value())
                .fetch().rowsUpdated()
                .thenMany(Flux.fromIterable(roles))
                .flatMap(role -> db.sql(INSERT_ROLE_SQL)
                        .bind("uid", id.value())
                        .bind("code", role.name())
                        .fetch().rowsUpdated())
                .then();
    }

    @Override
    public Mono<Void> setEnabled(UserId id, boolean enabled) {
        return db.sql("UPDATE users SET enabled = :enabled WHERE id = :uid")
                .bind("enabled", enabled)
                .bind("uid", id.value())
                .fetch().rowsUpdated()
                .then();
    }

    @Override
    public Mono<Void> updatePassword(UserId id, PasswordHash passwordHash) {
        return db.sql("UPDATE users SET password_hash = :hash WHERE id = :uid")
                .bind("hash", passwordHash.value())
                .bind("uid", id.value())
                .fetch().rowsUpdated()
                .then();
    }

    @Override
    public Mono<Void> updateDisplayName(UserId id, String displayName) {
        return db.sql("UPDATE users SET display_name = :name WHERE id = :uid")
                .bind("name", displayName)
                .bind("uid", id.value())
                .fetch().rowsUpdated()
                .then();
    }

    private Mono<User> withRoles(UserEntity entity) {
        return loadRoles(entity.id())
                .map(roles -> IamPersistenceMapper.toDomain(entity, roles));
    }

    private Mono<Set<RoleCode>> loadRoles(UUID userId) {
        return db.sql(ROLES_SQL)
                .bind("uid", userId)
                .map((row, meta) -> RoleCode.valueOf(row.get("code", String.class)))
                .all()
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RoleCode.class)));
    }
}
