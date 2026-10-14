package dev.skillforge.iam.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.iam.adapter.in.web.dto.ChangePasswordRequest;
import dev.skillforge.iam.adapter.in.web.dto.ForgotPasswordRequest;
import dev.skillforge.iam.adapter.in.web.dto.LoginRequest;
import dev.skillforge.iam.adapter.in.web.dto.RefreshRequest;
import dev.skillforge.iam.adapter.in.web.dto.RegisterRequest;
import dev.skillforge.iam.adapter.in.web.dto.ResetPasswordRequest;
import dev.skillforge.iam.adapter.in.web.dto.TokensResponse;
import dev.skillforge.iam.adapter.in.web.dto.UpdateProfileRequest;
import dev.skillforge.iam.adapter.in.web.dto.UserProfileResponse;
import dev.skillforge.iam.adapter.in.web.mapper.IamWebMapper;
import dev.skillforge.iam.domain.model.UserId;
import dev.skillforge.iam.domain.port.in.ChangePasswordUseCase;
import dev.skillforge.iam.domain.port.in.GetProfileUseCase;
import dev.skillforge.iam.domain.port.in.LoginUseCase;
import dev.skillforge.iam.domain.port.in.LogoutUseCase;
import dev.skillforge.iam.domain.port.in.RefreshTokenUseCase;
import dev.skillforge.iam.domain.port.in.RegisterUserCommand;
import dev.skillforge.iam.domain.port.in.RegisterUserUseCase;
import dev.skillforge.iam.domain.port.in.RequestPasswordResetUseCase;
import dev.skillforge.iam.domain.port.in.ResetPasswordUseCase;
import dev.skillforge.iam.domain.port.in.UpdateProfileUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Adaptateur entrant WebFlux du contexte IAM : inscription, connexion, session, profil. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUser;
    private final LoginUseCase login;
    private final RefreshTokenUseCase refreshToken;
    private final LogoutUseCase logout;
    private final GetProfileUseCase getProfile;
    private final ChangePasswordUseCase changePassword;
    private final RequestPasswordResetUseCase requestPasswordReset;
    private final ResetPasswordUseCase resetPassword;
    private final UpdateProfileUseCase updateProfile;

    public AuthController(RegisterUserUseCase registerUser, LoginUseCase login,
                          RefreshTokenUseCase refreshToken, LogoutUseCase logout,
                          GetProfileUseCase getProfile, ChangePasswordUseCase changePassword,
                          RequestPasswordResetUseCase requestPasswordReset,
                          ResetPasswordUseCase resetPassword, UpdateProfileUseCase updateProfile) {
        this.registerUser = registerUser;
        this.login = login;
        this.refreshToken = refreshToken;
        this.logout = logout;
        this.getProfile = getProfile;
        this.changePassword = changePassword;
        this.requestPasswordReset = requestPasswordReset;
        this.resetPassword = resetPassword;
        this.updateProfile = updateProfile;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResponse<UserProfileResponse>>> register(
            @Valid @RequestBody RegisterRequest request) {
        return registerUser.register(
                        new RegisterUserCommand(request.email(), request.password(), request.displayName()))
                .map(IamWebMapper::toProfile)
                .map(profile -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(profile)));
    }

    @PostMapping("/login")
    public Mono<ApiResponse<TokensResponse>> login(@Valid @RequestBody LoginRequest request) {
        return login.login(request.email(), request.password())
                .map(IamWebMapper::toTokens)
                .map(ApiResponse::ok);
    }

    @PostMapping("/refresh")
    public Mono<ApiResponse<TokensResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        return refreshToken.refresh(request.refreshToken())
                .map(IamWebMapper::toTokens)
                .map(ApiResponse::ok);
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@Valid @RequestBody RefreshRequest request) {
        return logout.logout(request.refreshToken())
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/me")
    public Mono<ApiResponse<UserProfileResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        UserId userId = new UserId(UUID.fromString(jwt.getSubject()));
        return getProfile.getById(userId)
                .map(IamWebMapper::toProfile)
                .map(ApiResponse::ok);
    }

    /** Met à jour le profil de l'utilisateur connecté (nom affiché). */
    @PatchMapping("/me")
    public Mono<ApiResponse<UserProfileResponse>> updateMe(@AuthenticationPrincipal Jwt jwt,
                                                           @Valid @RequestBody UpdateProfileRequest request) {
        UserId userId = new UserId(UUID.fromString(jwt.getSubject()));
        return updateProfile.updateDisplayName(userId, request.displayName())
                .map(IamWebMapper::toProfile)
                .map(ApiResponse::ok);
    }

    /** Change le mot de passe de l'utilisateur connecté (vérifie l'actuel). */
    @PatchMapping("/password")
    public Mono<ResponseEntity<Void>> changePassword(@AuthenticationPrincipal Jwt jwt,
                                                     @Valid @RequestBody ChangePasswordRequest request) {
        UserId userId = new UserId(UUID.fromString(jwt.getSubject()));
        return changePassword.change(userId, request.currentPassword(), request.newPassword())
                .thenReturn(ResponseEntity.noContent().build());
    }

    /** Demande un lien de réinitialisation (réponse neutre, sans corps). */
    @PostMapping("/password/forgot")
    public Mono<ResponseEntity<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return requestPasswordReset.request(request.email())
                .thenReturn(ResponseEntity.noContent().build());
    }

    /** Définit un nouveau mot de passe à partir d'un jeton de réinitialisation. */
    @PostMapping("/password/reset")
    public Mono<ResponseEntity<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return resetPassword.reset(request.token(), request.newPassword())
                .thenReturn(ResponseEntity.noContent().build());
    }
}
