package dev.springmind.wallet.auth;

import dev.springmind.wallet.auth.dto.LoginRequest;
import dev.springmind.wallet.auth.dto.LoginResponse;
import dev.springmind.wallet.auth.dto.UserDto;
import dev.springmind.wallet.common.ApiException;
import dev.springmind.wallet.persistence.entity.UserEntity;
import dev.springmind.wallet.persistence.repository.UserRepository;
import dev.springmind.wallet.security.MockBearerTokenFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .filter(found -> found.getPasswordHash().equals(request.password()))
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email ou senha inválidos."));

        UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail());
        return new LoginResponse(MockBearerTokenFilter.MOCK_TOKEN, userDto);
    }
}
