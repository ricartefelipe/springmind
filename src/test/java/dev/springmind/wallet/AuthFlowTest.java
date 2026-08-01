package dev.springmind.wallet;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.springmind.wallet.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/reset-wallet.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestPropertySource(properties = "totalrecall.provision-token=test-totalrecall-token")
class AuthFlowTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_comCredenciaisValidas_devolveTokenMockETodosOsDadosDoUsuario() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"demo@vuemind.dev","password":"demo123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("mock-jwt-demo")))
                .andExpect(jsonPath("$.user.email", is("demo@vuemind.dev")));
    }

    @Test
    void login_comSenhaErrada_devolve401NoFormatoApiError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"demo@vuemind.dev","password":"senha-errada"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("INVALID_CREDENTIALS")))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void login_comSenhaDemoBcryptDevolveTokenMock() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"demo@vuemind.dev","password":"demo123"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void provisionamentoInternoComTokenValidoCriaUsuarioQuePodeFazerLogin() throws Exception {
        mockMvc.perform(post("/internal/v1/totalrecall/users")
                        .header("X-TotalRecall-Token", "test-totalrecall-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"totalrecall-user@vuemind.dev",
                                  "name":"Usuário TotalRecall",
                                  "password":"senha-segura",
                                  "action":"upsert"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("totalrecall-user@vuemind.dev")))
                .andExpect(jsonPath("$.enabled", is(true)));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"totalrecall-user@vuemind.dev","password":"senha-segura"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name", is("Usuário TotalRecall")));
    }

    @Test
    void provisionamentoDisableImpedeLoginDoUsuario() throws Exception {
        String email = "totalrecall-disabled@vuemind.dev";
        mockMvc.perform(post("/internal/v1/totalrecall/users")
                        .header("X-TotalRecall-Token", "test-totalrecall-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"%s",
                                  "name":"Usuário Desabilitado",
                                  "password":"senha-segura",
                                  "action":"upsert"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/internal/v1/totalrecall/users")
                        .header("X-TotalRecall-Token", "test-totalrecall-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","action":"disable"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(false)));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"senha-segura"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void provisionamentoInternoRejeitaTokenInvalido() throws Exception {
        mockMvc.perform(post("/internal/v1/totalrecall/users")
                        .header("X-TotalRecall-Token", "token-invalido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"nao-criar@vuemind.dev",
                                  "name":"Não Criar",
                                  "password":"senha-segura"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")));
    }

    @Test
    void healthInternoExigeTokenEInformaDisponibilidade() throws Exception {
        mockMvc.perform(get("/internal/v1/totalrecall/health")
                        .header("X-TotalRecall-Token", "test-totalrecall-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }
}
