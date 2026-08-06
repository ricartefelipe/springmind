package dev.springmind.wallet;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.springmind.wallet.security.MockBearerTokenFilter;
import dev.springmind.wallet.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/reset-wallet.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PixTransferTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pix_comValorMaiorQueSaldoDisponivel_devolve409InsufficientFunds() throws Exception {
        mockMvc.perform(post("/api/v1/transfers/pix")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"beneficiaryId":"b1","amountCents":99999900}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("INSUFFICIENT_FUNDS")))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void pix_acimaDoLimiteDiario_devolve409DailyLimitExceeded() throws Exception {
        mockMvc.perform(post("/api/v1/transfers/pix")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"beneficiaryId":"b1","amountCents":100001}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("DAILY_LIMIT_EXCEEDED")))
                .andExpect(jsonPath("$.correlationId").exists());
    }
}
