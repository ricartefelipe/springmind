package dev.springmind.wallet;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import dev.springmind.wallet.security.MockBearerTokenFilter;
import dev.springmind.wallet.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/reset-wallet.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PixTransferIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fluxoCompleto_loginPixEConsultaSaldo() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"demo@vuemind.dev","password":"demo123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("mock-jwt-demo")));

        MvcResult firstPix = mockMvc.perform(post("/api/v1/transfers/pix")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN)
                        .header("Idempotency-Key", "it-pix-1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"beneficiaryId":"b1","amountCents":1000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amountCents", is(1000)))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andReturn();

        mockMvc.perform(get("/api/v1/wallet/balance")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCents", is(249_000)));

        String firstId = JsonPath.read(firstPix.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/v1/transfers/pix")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN)
                        .header("Idempotency-Key", "it-pix-1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"beneficiaryId":"b1","amountCents":1000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(firstId)));

        mockMvc.perform(get("/api/v1/wallet/balance")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCents", is(249_000)));
    }
}
