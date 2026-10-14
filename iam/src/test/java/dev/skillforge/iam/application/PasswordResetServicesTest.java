package dev.skillforge.iam.application;

import dev.skillforge.common.error.UnauthorizedException;
import dev.skillforge.iam.domain.model.Email;
import dev.skillforge.iam.domain.model.PasswordHash;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;
import dev.skillforge.iam.domain.port.out.PasswordHasher;
import dev.skillforge.iam.domain.port.out.PasswordResetMailer;
import dev.skillforge.iam.domain.port.out.PasswordResetTokenRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests des cas d'usage : changement, demande de réinitialisation, réinitialisation. */
class PasswordResetServicesTest {

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

    // --- Changement de mot de passe (connecté) ---

    @Test
    void change_remplace_le_mot_de_passe_quand_l_actuel_est_bon() {
        var repo = new IamServicesTest.InMemoryUserRepository();
        User u = User.register(new Email("a@demo.dev"), hasher.hash("ancien12"), "A");
        repo.save(u).block();
        var service = new ChangePasswordService(repo, hasher);

        StepVerifier.create(service.change(u.id(), "ancien12", "nouveau34")).verifyComplete();
        assertEquals("H:nouveau34", repo.findById(u.id()).block().passwordHash().value());
    }

    @Test
    void change_echoue_401_si_mot_de_passe_actuel_incorrect() {
        var repo = new IamServicesTest.InMemoryUserRepository();
        User u = User.register(new Email("a@demo.dev"), hasher.hash("ancien12"), "A");
        repo.save(u).block();
        var service = new ChangePasswordService(repo, hasher);

        StepVerifier.create(service.change(u.id(), "mauvais", "nouveau34"))
                .expectErrorMatches(e -> e instanceof UnauthorizedException ex
                        && ex.code().equals("INVALID_CURRENT_PASSWORD"))
                .verify();
        assertEquals("H:ancien12", repo.findById(u.id()).block().passwordHash().value());
    }

    // --- Réinitialisation via jeton ---

    @Test
    void reset_definit_le_nouveau_mot_de_passe_avec_un_jeton_valide() {
        var repo = new IamServicesTest.InMemoryUserRepository();
        User u = User.register(new Email("b@demo.dev"), hasher.hash("vieux123"), "B");
        repo.save(u).block();
        var tokens = new InMemoryResetTokens();
        String raw = tokens.issue(u.id()).block();
        var service = new ResetPasswordService(tokens, repo, hasher);

        StepVerifier.create(service.reset(raw, "frais567")).verifyComplete();
        assertEquals("H:frais567", repo.findById(u.id()).block().passwordHash().value());
    }

    @Test
    void reset_echoue_401_si_jeton_invalide() {
        var repo = new IamServicesTest.InMemoryUserRepository();
        var service = new ResetPasswordService(new InMemoryResetTokens(), repo, hasher);

        StepVerifier.create(service.reset("jeton-bidon", "frais567"))
                .expectErrorMatches(e -> e instanceof UnauthorizedException ex
                        && ex.code().equals("INVALID_RESET_TOKEN"))
                .verify();
    }

    @Test
    void reset_echoue_si_jeton_deja_utilise() {
        var repo = new IamServicesTest.InMemoryUserRepository();
        User u = User.register(new Email("b@demo.dev"), hasher.hash("vieux123"), "B");
        repo.save(u).block();
        var tokens = new InMemoryResetTokens();
        String raw = tokens.issue(u.id()).block();
        var service = new ResetPasswordService(tokens, repo, hasher);

        service.reset(raw, "frais567").block();
        StepVerifier.create(service.reset(raw, "encore89"))
                .expectErrorMatches(e -> e instanceof UnauthorizedException)
                .verify();
    }

    // --- Demande de réinitialisation (réponse neutre) ---

    @Test
    void request_envoie_un_lien_pour_un_compte_existant() {
        var repo = new IamServicesTest.InMemoryUserRepository();
        User u = User.register(new Email("c@demo.dev"), hasher.hash("x"), "C");
        repo.save(u).block();
        var tokens = new InMemoryResetTokens();
        var mailer = new RecordingMailer();
        var service = new RequestPasswordResetService(repo, tokens, mailer, "http://localhost:8081");

        StepVerifier.create(service.request("c@demo.dev")).verifyComplete();
        assertEquals("c@demo.dev", mailer.lastRecipient);
        assertTrue(mailer.lastUrl.startsWith("http://localhost:8081/reset-password?token="));
    }

    @Test
    void request_ne_fait_rien_et_reste_neutre_pour_un_compte_inconnu() {
        var repo = new IamServicesTest.InMemoryUserRepository();
        var mailer = new RecordingMailer();
        var service = new RequestPasswordResetService(repo, new InMemoryResetTokens(), mailer, "http://localhost:8081");

        StepVerifier.create(service.request("inconnu@demo.dev")).verifyComplete();
        assertNull(mailer.lastRecipient);
    }

    // --- Mise à jour du profil ---

    @Test
    void update_display_name_normalise_et_enregistre() {
        var repo = new IamServicesTest.InMemoryUserRepository();
        User u = User.register(new Email("d@demo.dev"), hasher.hash("x"), "Ancien");
        repo.save(u).block();
        var service = new UpdateProfileService(repo);

        StepVerifier.create(service.updateDisplayName(u.id(), "  Nouveau Nom  "))
                .assertNext(updated -> assertEquals("Nouveau Nom", updated.displayName()))
                .verifyComplete();
    }

    @Test
    void update_display_name_refuse_un_nom_vide() {
        var repo = new IamServicesTest.InMemoryUserRepository();
        User u = User.register(new Email("d@demo.dev"), hasher.hash("x"), "Ancien");
        repo.save(u).block();
        var service = new UpdateProfileService(repo);

        StepVerifier.create(service.updateDisplayName(u.id(), "   "))
                .expectError()
                .verify();
    }

    // --- Fakes ---

    /** Faux store de jetons : usage unique, pas d'expiration simulée. */
    static final class InMemoryResetTokens implements PasswordResetTokenRepository {
        final Map<String, UserId> byToken = new HashMap<>();
        final Map<String, Boolean> used = new HashMap<>();

        @Override
        public Mono<String> issue(UserId userId) {
            String raw = UUID.randomUUID().toString();
            byToken.put(raw, userId);
            used.put(raw, false);
            return Mono.just(raw);
        }

        @Override
        public Mono<UserId> consume(String rawToken) {
            UserId id = byToken.get(rawToken);
            if (id == null || Boolean.TRUE.equals(used.get(rawToken))) {
                return Mono.empty();
            }
            used.put(rawToken, true);
            return Mono.just(id);
        }
    }

    /** Faux mailer : mémorise le dernier envoi. */
    static final class RecordingMailer implements PasswordResetMailer {
        String lastRecipient;
        String lastUrl;

        @Override
        public Mono<Void> sendResetLink(Email recipient, String resetUrl) {
            this.lastRecipient = recipient.value();
            this.lastUrl = resetUrl;
            return Mono.empty();
        }
    }
}
