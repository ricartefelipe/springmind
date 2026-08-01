package dev.springmind.wallet.totalrecall;

import dev.springmind.wallet.common.ApiException;
import dev.springmind.wallet.persistence.entity.UserEntity;
import dev.springmind.wallet.persistence.repository.UserRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TotalRecallProvisionService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TotalRecallProvisionService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public TotalRecallUserResponse provision(TotalRecallUserRequest request) {
        ProvisionAction action = request.action() == null ? ProvisionAction.upsert : request.action();
        return switch (action) {
            case upsert -> upsert(request);
            case disable -> disable(request.email(), false);
            case revoke -> disable(request.email(), true);
        };
    }

    private TotalRecallUserResponse upsert(TotalRecallUserRequest request) {
        requireValue(request.email(), "email");
        requireValue(request.name(), "name");
        requireValue(request.password(), "password");

        UserEntity user = userRepository.findByEmail(request.email()).orElseGet(() -> new UserEntity(
                UUID.randomUUID().toString(),
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                true,
                request.expiresAt(),
                request.role()));

        if (userRepository.existsById(user.getId())) {
            user.update(
                    request.name(),
                    passwordEncoder.encode(request.password()),
                    request.expiresAt(),
                    request.role());
        }

        return toResponse(userRepository.save(user));
    }

    private TotalRecallUserResponse disable(String email, boolean revoked) {
        requireValue(email, "email");
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuário não encontrado."));
        if (revoked) {
            user.revoke();
        } else {
            user.disable();
        }
        return toResponse(userRepository.save(user));
    }

    private void requireValue(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PROVISION_REQUEST",
                    "O campo %s é obrigatório.".formatted(field));
        }
    }

    private TotalRecallUserResponse toResponse(UserEntity user) {
        return new TotalRecallUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.isEnabled(),
                user.getRole(),
                user.getExpiresAt());
    }
}
