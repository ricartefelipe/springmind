package dev.springmind.wallet;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import dev.springmind.wallet.security.MockBearerTokenFilter;
import dev.springmind.wallet.support.AbstractPostgresIntegrationTest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
                .andExpect(jsonPath("$.endToEndId", notNullValue()))
                .andExpect(jsonPath("$.correlationId", notNullValue()))
                .andReturn();

        mockMvc.perform(get("/api/v1/wallet/balance")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCents", is(249_000)))
                .andExpect(jsonPath("$.blockedCents", is(10_000)))
                .andExpect(jsonPath("$.dailyLimitCents", is(100_000)))
                .andExpect(jsonPath("$.dailySpentCents", is(1000)));

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

    @Test
    void pixAgendado_processaAoConsultarSaldo() throws Exception {
        String scheduledFor = Instant.now().minus(1, ChronoUnit.MINUTES).toString();

        MvcResult scheduled = mockMvc.perform(post("/api/v1/transfers/pix")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN)
                        .header("Idempotency-Key", "it-pix-scheduled")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"beneficiaryId":"b1","amountCents":2000,"scheduledFor":"%s"}
                                """.formatted(scheduledFor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("SCHEDULED")))
                .andReturn();

        mockMvc.perform(get("/api/v1/wallet/balance")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCents", is(248_000)))
                .andExpect(jsonPath("$.dailySpentCents", is(2000)));

        String transferId = JsonPath.read(scheduled.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(get("/api/v1/transfers/" + transferId)
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void notificacoes_listarEMarcarLidas() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.items[0].read", is(false)));

        mockMvc.perform(post("/api/v1/notifications/n1/read")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id=='n1')].read", is(org.hamcrest.Matchers.contains(true))));

        mockMvc.perform(post("/api/v1/notifications/read-all")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].read", is(true)))
                .andExpect(jsonPath("$.items[1].read", is(true)))
                .andExpect(jsonPath("$.items[2].read", is(true)));
    }

    @Test
    void onboarding_marcaViewStatementAoListarExtrato() throws Exception {
        mockMvc.perform(get("/api/v1/me/onboarding")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(false)))
                .andExpect(jsonPath("$.steps[?(@.id=='VIEW_STATEMENT')].done", is(org.hamcrest.Matchers.contains(false))));

        mockMvc.perform(get("/api/v1/wallet/transactions")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items", hasSize(1)));

        mockMvc.perform(get("/api/v1/me/onboarding")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps[?(@.id=='VIEW_STATEMENT')].done", is(org.hamcrest.Matchers.contains(true))));
    }

    @Test
    void qrPayload_devolveFormatoEstavel() throws Exception {
        mockMvc.perform(get("/api/v1/transfers/pix/qr-payload")
                        .header("Authorization", "Bearer " + MockBearerTokenFilter.MOCK_TOKEN)
                        .param("amountCents", "1500")
                        .param("pixKey", "ana@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload", is("MINDPIX|v1|ana@email.com|1500")));
    }
}
