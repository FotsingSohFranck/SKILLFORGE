package dev.skillforge.iam.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.model.UserId;
import dev.skillforge.iam.domain.port.in.UpdateProfileUseCase;
import dev.skillforge.iam.domain.port.out.UserRepository;
import reactor.core.publisher.Mono;

/** Mise à jour du profil : normalise et valide le nom affiché, puis renvoie l'utilisateur à jour. */
public class UpdateProfileService implements UpdateProfileUseCase {

    private final UserRepository users;

    public UpdateProfileService(UserRepository users) {
        this.users = users;
    }

    @Override
    public Mono<User> updateDisplayName(UserId id, String displayName) {
        String name = displayName == null ? "" : displayName.trim();
        if (name.isBlank()) {
            return Mono.error(new BusinessRuleException("INVALID_DISPLAY_NAME", "Le nom affiché est requis."));
        }
        if (name.length() > 120) {
            name = name.substring(0, 120);
        }
        String finalName = name;
        return users.updateDisplayName(id, finalName)
                .then(users.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("USER_NOT_FOUND", "Utilisateur introuvable.")));
    }
}
