package dev.springmind.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.springmind.wallet.common.ApiException;
import dev.springmind.wallet.support.AbstractPostgresIntegrationTest;
import dev.springmind.wallet.transfers.PixService;
import dev.springmind.wallet.transfers.dto.CreatePixRequest;
import dev.springmind.wallet.transfers.dto.TransferDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql(scripts = "/sql/reset-wallet.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PixServiceTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private PixService pixService;

    @Test
    void executePix_comSaldoInsuficiente_lancaInsufficientFunds() {
        ApiException ex = assertThrows(
                ApiException.class,
                () -> pixService.executePix(
                        new CreatePixRequest(999_999_00L, "b1", null, null, null), "unit-insufficient"));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("INSUFFICIENT_FUNDS", ex.getCode());
    }

    @Test
    void executePix_comMesmaChaveIdempotencia_naoDebitaDuasVezes() {
        CreatePixRequest request = new CreatePixRequest(500L, "b1", null, null, null);
        TransferDto first = pixService.executePix(request, "unit-idem-key");
        TransferDto second = pixService.executePix(request, "unit-idem-key");

        assertEquals(first.id(), second.id());
        assertEquals(first.amountCents(), second.amountCents());
    }

    @Test
    void executePix_acimaDoLimiteDiario_lancaDailyLimitExceeded() {
        ApiException ex = assertThrows(
                ApiException.class,
                () -> pixService.executePix(
                        new CreatePixRequest(100_001L, "b1", null, null, null), "unit-daily-limit"));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("DAILY_LIMIT_EXCEEDED", ex.getCode());
    }
}
