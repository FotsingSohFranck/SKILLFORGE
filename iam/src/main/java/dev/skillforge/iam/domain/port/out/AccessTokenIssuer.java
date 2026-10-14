package dev.skillforge.iam.domain.port.out;

import dev.skillforge.iam.domain.model.AccessToken;
import dev.skillforge.iam.domain.model.User;

/** Port sortant : émission d'un jeton d'accès (JWT) pour un utilisateur. */
public interface AccessTokenIssuer {
    AccessToken issue(User user);
}
