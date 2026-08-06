package dev.springmind.wallet;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.springmind.wallet.security.MockBearerTokenFilter;
import dev.springmind.wallet.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/reset-wallet.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WalletBalanceTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void balance_semToken_devolve401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/wallet/balance"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void balance_comToken_devolveSaldoSeed() throws Exception {
        mockMvc.perform(get("/api/v1/wallet/balance")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCents", is(250_000)))
                .andExpect(jsonPath("$.blockedCents", is(10_000)))
                .andExpect(jsonPath("$.dailyLimitCents", is(100_000)))
                .andExpect(jsonPath("$.dailySpentCents", is(0)))
                .andExpect(jsonPath("$.currency", is("BRL")));
    }
}
