package dev.skillforge.iam.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.common.error.ConflictException;
import dev.skillforge.common.error.UnauthorizedException;
import dev.skillforge.iam.domain.model.Email;
import dev.skillforge.iam.domain.model.PasswordHash;
import dev.skillforge.iam.domain.model.RoleCode;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;
import dev.skillforge.iam.domain.port.in.RegisterUserCommand;
import dev.skillforge.iam.domain.port.out.PasswordHasher;
import dev.skillforge.iam.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IamServicesTest {

    /** Hasher factice : "hash" = "H:" + clair. */
    private final PasswordHasher hasher = new PasswordHasher() {
        @Override
        public PasswordHash hash(String rawPassword) {
            return new PasswordHash("H:" + rawPassword);
        }

        @Override
        public boolean matches(String rawPassword, PasswordHash hash) {
            return hash.value().equals("H:" + rawPassword);
        }
    };

    @Test
    void register_cree_un_LEARNER_et_normalise_email() {
        var repo = new InMemoryUserRepository();
        var service = new RegisterUserService(repo, hasher);

        StepVerifier.create(service.register(
                        new RegisterUserCommand("  LEA@Demo.DEV ", "secret123", "Léa")))
                .assertNext(user -> {
                    assertEquals("lea@demo.dev", user.email().value());
                    assertTrue(user.hasRole(RoleCode.LEARNER));
                    assertTrue(user.enabled());
                    assertEquals("H:secret123", user.passwordHash().value());
                })
                .verifyComplete();
        assertEquals(1, repo.byEmail.size());
    }

    @Test
    void register_refuse_un_email_deja_utilise() {
        var repo = new InMemoryUserRepository();
        repo.save(User.register(new Email("dup@demo.dev"), new PasswordHash("H:x"), "Dup")).block();
        var service = new RegisterUserService(repo, hasher);

        StepVerifier.create(service.register(new RegisterUserCommand("dup@demo.dev", "pw", "Dup2")))
                .expectErrorMatches(e -> e instanceof ConflictException c
                        && c.code().equals("EMAIL_ALREADY_USED"))
                .verify();
    }

    @Test
    void register_rejette_un_email_invalide() {
        var service = new RegisterUserService(new InMemoryUserRepository(), hasher);
        assertThrows(BusinessRuleException.class,
                () -> service.register(new RegisterUserCommand("pas-un-email", "pw", "X")));
    }

    @Test
    void authenticate_reussit_avec_le_bon_mot_de_passe() {
        var repo = new InMemoryUserRepository();
        repo.save(User.register(new Email("marc@demo.dev"), hasher.hash("topsecret"), "Marc")).block();
        var service = new AuthenticateUserService(repo, hasher);

        StepVerifier.create(service.authenticate("marc@demo.dev", "topsecret"))
                .assertNext(u -> assertEquals("marc@demo.dev", u.email().value()))
                .verifyComplete();
    }

    @Test
    void authenticate_echoue_401_avec_mauvais_mot_de_passe() {
        var repo = new InMemoryUserRepository();
        repo.save(User.register(new Email("marc@demo.dev"), hasher.hash("topsecret"), "Marc")).block();
        var service = new AuthenticateUserService(repo, hasher);

        StepVerifier.create(service.authenticate("marc@demo.dev", "wrong"))
                .expectErrorMatches(e -> e instanceof UnauthorizedException u
                        && u.code().equals("INVALID_CREDENTIALS"))
                .verify();
    }

    @Test
    void authenticate_echoue_401_pour_email_inconnu() {
        var service = new AuthenticateUserService(new InMemoryUserRepository(), hasher);
        StepVerifier.create(service.authenticate("nobody@demo.dev", "pw"))
                .expectErrorMatches(e -> e instanceof UnauthorizedException)
                .verify();
    }

    /** Fake in-memory du port UserRepository. */
    static final class InMemoryUserRepository implements UserRepository {
        final Map<String, User> byEmail = new HashMap<>();
        final Map<UserId, User> byId = new HashMap<>();

        @Override
        public Mono<Boolean> existsByEmail(Email email) {
            return Mono.just(byEmail.containsKey(email.value()));
        }

        @Override
        public Mono<User> findByEmail(Email email) {
            return Mono.justOrEmpty(byEmail.get(email.value()));
        }

        @Override
        public Mono<User> findById(UserId id) {
            return Mono.justOrEmpty(byId.get(id));
        }

        @Override
        public Mono<User> save(User user) {
            byEmail.put(user.email().value(), user);
            byId.put(user.id(), user);
            return Mono.just(user);
        }

        @Override
        public reactor.core.publisher.Flux<User> findAll() {
            return reactor.core.publisher.Flux.fromIterable(byId.values());
        }

        @Override
        public Mono<Void> replaceRoles(UserId id, java.util.Set<dev.skillforge.iam.domain.model.RoleCode> roles) {
            User u = byId.get(id);
            if (u != null) {
                User updated = new User(u.id(), u.email(), u.passwordHash(), u.displayName(), u.enabled(), roles);
                byId.put(id, updated);
                byEmail.put(u.email().value(), updated);
            }
            return Mono.empty();
        }

        @Override
        public Mono<Void> setEnabled(UserId id, boolean enabled) {
            User u = byId.get(id);
            if (u != null) {
                User updated = new User(u.id(), u.email(), u.passwordHash(), u.displayName(), enabled, u.roles());
                byId.put(id, updated);
                byEmail.put(u.email().value(), updated);
            }
            return Mono.empty();
        }

        @Override
        public Mono<Void> updatePassword(UserId id, PasswordHash passwordHash) {
            User u = byId.get(id);
            if (u != null) {
                User updated = new User(u.id(), u.email(), passwordHash, u.displayName(), u.enabled(), u.roles());
                byId.put(id, updated);
                byEmail.put(u.email().value(), updated);
            }
            return Mono.empty();
        }

        @Override
        public Mono<Void> updateDisplayName(UserId id, String displayName) {
            User u = byId.get(id);
            if (u != null) {
                User updated = new User(u.id(), u.email(), u.passwordHash(), displayName, u.enabled(), u.roles());
                byId.put(id, updated);
                byEmail.put(u.email().value(), updated);
            }
            return Mono.empty();
        }
    }
}
