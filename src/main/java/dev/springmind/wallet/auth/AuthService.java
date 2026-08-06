package dev.springmind.wallet.auth;

import dev.springmind.wallet.auth.dto.LoginRequest;
import dev.springmind.wallet.auth.dto.LoginResponse;
import dev.springmind.wallet.auth.dto.UserDto;
import dev.springmind.wallet.common.ApiException;
import dev.springmind.wallet.onboarding.OnboardingService;
import dev.springmind.wallet.persistence.entity.OnboardingStepCode;
import dev.springmind.wallet.persistence.entity.UserEntity;
import dev.springmind.wallet.persistence.repository.UserRepository;
import dev.springmind.wallet.security.MockBearerTokenFilter;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OnboardingService onboardingService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            OnboardingService onboardingService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.onboardingService = onboardingService;
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .filter(UserEntity::isEnabled)
                .filter(found -> hasNotExpired(found, Instant.now()))
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email ou senha inválidos."));

        onboardingService.markDone(user.getId(), OnboardingStepCode.PROFILE_OK);

        UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail());
        return new LoginResponse(MockBearerTokenFilter.MOCK_TOKEN, userDto);
    }

    private boolean hasNotExpired(UserEntity user, Instant now) {
        return user.getExpiresAt() == null || user.getExpiresAt().isAfter(now);
    }
}
