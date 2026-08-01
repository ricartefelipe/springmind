package dev.springmind.wallet.totalrecall;

import dev.springmind.wallet.common.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/totalrecall")
public class TotalRecallInternalController {

    private final TotalRecallProvisionService provisionService;
    private final byte[] provisionToken;

    public TotalRecallInternalController(
            TotalRecallProvisionService provisionService,
            @Value("${totalrecall.provision-token:}") String provisionToken) {
        this.provisionService = provisionService;
        this.provisionToken = provisionToken.getBytes(StandardCharsets.UTF_8);
    }

    @GetMapping("/health")
    public Map<String, String> health(
            @RequestHeader(value = "X-TotalRecall-Token", required = false) String token) {
        authorize(token);
        return Map.of("status", "UP");
    }

    @PostMapping("/users")
    public TotalRecallUserResponse provisionUser(
            @RequestHeader(value = "X-TotalRecall-Token", required = false) String token,
            @RequestBody TotalRecallUserRequest request) {
        authorize(token);
        return provisionService.provision(request);
    }

    private void authorize(String token) {
        byte[] receivedToken = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
        if (provisionToken.length == 0 || !MessageDigest.isEqual(provisionToken, receivedToken)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Token de provisionamento inválido.");
        }
    }
}
