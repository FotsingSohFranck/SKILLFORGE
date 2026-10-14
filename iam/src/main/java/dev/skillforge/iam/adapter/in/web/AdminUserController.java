package dev.skillforge.iam.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.iam.adapter.in.web.dto.ChangeRolesRequest;
import dev.skillforge.iam.adapter.in.web.dto.SetStatusRequest;
import dev.skillforge.iam.adapter.in.web.dto.UserProfileResponse;
import dev.skillforge.iam.adapter.in.web.mapper.IamWebMapper;
import dev.skillforge.iam.domain.model.RoleCode;
import dev.skillforge.iam.domain.model.UserId;
import dev.skillforge.iam.domain.port.in.ChangeUserRolesUseCase;
import dev.skillforge.iam.domain.port.in.ListUsersUseCase;
import dev.skillforge.iam.domain.port.in.SetUserEnabledUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Administration des utilisateurs — réservé au rôle ADMIN (autorisation appliquée par la
 * {@code SecurityWebFilterChain} sur {@code /api/v1/admin/**}).
 */
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final ListUsersUseCase listUsers;
    private final ChangeUserRolesUseCase changeUserRoles;
    private final SetUserEnabledUseCase setUserEnabled;

    public AdminUserController(ListUsersUseCase listUsers,
                               ChangeUserRolesUseCase changeUserRoles,
                               SetUserEnabledUseCase setUserEnabled) {
        this.listUsers = listUsers;
        this.changeUserRoles = changeUserRoles;
        this.setUserEnabled = setUserEnabled;
    }

    @GetMapping
    public Mono<ApiResponse<List<UserProfileResponse>>> list() {
        return listUsers.list()
                .map(IamWebMapper::toProfile)
                .collectList()
                .map(ApiResponse::ok);
    }

    @PutMapping("/{id}/roles")
    public Mono<ApiResponse<UserProfileResponse>> changeRoles(
            @PathVariable UUID id, @Valid @RequestBody ChangeRolesRequest request) {
        return changeUserRoles.changeRoles(new UserId(id), parseRoles(request.roles()))
                .map(IamWebMapper::toProfile)
                .map(ApiResponse::ok);
    }

    @PatchMapping("/{id}/status")
    public Mono<ApiResponse<UserProfileResponse>> setStatus(
            @PathVariable UUID id, @Valid @RequestBody SetStatusRequest request) {
        return setUserEnabled.setEnabled(new UserId(id), request.enabled())
                .map(IamWebMapper::toProfile)
                .map(ApiResponse::ok);
    }

    private Set<RoleCode> parseRoles(List<String> codes) {
        Set<RoleCode> roles = EnumSet.noneOf(RoleCode.class);
        for (String code : codes) {
            try {
                roles.add(RoleCode.valueOf(code.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new BusinessRuleException("INVALID_ROLE", "Rôle inconnu : " + code);
            }
        }
        return roles;
    }
}
