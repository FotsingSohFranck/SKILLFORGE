package dev.skillforge.iam.config;

import dev.skillforge.iam.application.AuthenticateUserService;
import dev.skillforge.iam.application.ChangePasswordService;
import dev.skillforge.iam.application.ChangeUserRolesService;
import dev.skillforge.iam.application.GetProfileService;
import dev.skillforge.iam.application.ListUsersService;
import dev.skillforge.iam.application.LoginService;
import dev.skillforge.iam.application.LogoutService;
import dev.skillforge.iam.application.RefreshTokenService;
import dev.skillforge.iam.application.RegisterUserService;
import dev.skillforge.iam.application.RequestPasswordResetService;
import dev.skillforge.iam.application.ResetPasswordService;
import dev.skillforge.iam.application.SetUserEnabledService;
import dev.skillforge.iam.application.UpdateProfileService;
import dev.skillforge.iam.domain.port.in.AuthenticateUserUseCase;
import dev.skillforge.iam.domain.port.in.ChangePasswordUseCase;
import dev.skillforge.iam.domain.port.in.ChangeUserRolesUseCase;
import dev.skillforge.iam.domain.port.in.GetProfileUseCase;
import dev.skillforge.iam.domain.port.in.ListUsersUseCase;
import dev.skillforge.iam.domain.port.in.LoginUseCase;
import dev.skillforge.iam.domain.port.in.LogoutUseCase;
import dev.skillforge.iam.domain.port.in.RefreshTokenUseCase;
import dev.skillforge.iam.domain.port.in.RegisterUserUseCase;
import dev.skillforge.iam.domain.port.in.RequestPasswordResetUseCase;
import dev.skillforge.iam.domain.port.in.ResetPasswordUseCase;
import dev.skillforge.iam.domain.port.in.SetUserEnabledUseCase;
import dev.skillforge.iam.domain.port.in.UpdateProfileUseCase;
import dev.skillforge.iam.domain.port.out.AccessTokenIssuer;
import dev.skillforge.iam.domain.port.out.PasswordHasher;
import dev.skillforge.iam.domain.port.out.PasswordResetMailer;
import dev.skillforge.iam.domain.port.out.PasswordResetTokenRepository;
import dev.skillforge.iam.domain.port.out.RefreshTokenStore;
import dev.skillforge.iam.domain.port.out.UserRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Câblage du contexte IAM : services applicatifs purs exposés en beans. */
@Configuration
@EnableConfigurationProperties(PasswordResetProperties.class)
public class IamConfig {

    @Bean
    RegisterUserUseCase registerUserUseCase(UserRepository users, PasswordHasher passwordHasher) {
        return new RegisterUserService(users, passwordHasher);
    }

    @Bean
    AuthenticateUserUseCase authenticateUserUseCase(UserRepository users, PasswordHasher passwordHasher) {
        return new AuthenticateUserService(users, passwordHasher);
    }

    @Bean
    GetProfileUseCase getProfileUseCase(UserRepository users) {
        return new GetProfileService(users);
    }

    @Bean
    LoginUseCase loginUseCase(AuthenticateUserUseCase authenticate,
                              AccessTokenIssuer accessTokenIssuer,
                              RefreshTokenStore refreshTokenStore) {
        return new LoginService(authenticate, accessTokenIssuer, refreshTokenStore);
    }

    @Bean
    RefreshTokenUseCase refreshTokenUseCase(UserRepository users,
                                            AccessTokenIssuer accessTokenIssuer,
                                            RefreshTokenStore refreshTokenStore) {
        return new RefreshTokenService(users, accessTokenIssuer, refreshTokenStore);
    }

    @Bean
    LogoutUseCase logoutUseCase(RefreshTokenStore refreshTokenStore) {
        return new LogoutService(refreshTokenStore);
    }

    @Bean
    ListUsersUseCase listUsersUseCase(UserRepository users) {
        return new ListUsersService(users);
    }

    @Bean
    ChangeUserRolesUseCase changeUserRolesUseCase(UserRepository users) {
        return new ChangeUserRolesService(users);
    }

    @Bean
    SetUserEnabledUseCase setUserEnabledUseCase(UserRepository users) {
        return new SetUserEnabledService(users);
    }

    @Bean
    ChangePasswordUseCase changePasswordUseCase(UserRepository users, PasswordHasher passwordHasher) {
        return new ChangePasswordService(users, passwordHasher);
    }

    @Bean
    UpdateProfileUseCase updateProfileUseCase(UserRepository users) {
        return new UpdateProfileService(users);
    }

    @Bean
    RequestPasswordResetUseCase requestPasswordResetUseCase(UserRepository users,
                                                            PasswordResetTokenRepository tokens,
                                                            PasswordResetMailer mailer,
                                                            PasswordResetProperties props) {
        return new RequestPasswordResetService(users, tokens, mailer, props.frontendBaseUrl());
    }

    @Bean
    ResetPasswordUseCase resetPasswordUseCase(PasswordResetTokenRepository tokens,
                                              UserRepository users,
                                              PasswordHasher passwordHasher) {
        return new ResetPasswordService(tokens, users, passwordHasher);
    }
}
