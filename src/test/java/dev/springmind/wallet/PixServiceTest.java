package dev.springmind.wallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.springmind.wallet.common.ApiException;
import dev.springmind.wallet.support.AbstractPostgresIntegrationTest;
import dev.springmind.wallet.support.MutableClock;
import dev.springmind.wallet.support.TestClockConfig;
import dev.springmind.wallet.transfers.EndToEndIds;
import dev.springmind.wallet.transfers.PixService;
import dev.springmind.wallet.transfers.dto.CreatePixRequest;
import dev.springmind.wallet.transfers.dto.TransferDto;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Import(TestClockConfig.class)
@Sql(scripts = "/sql/reset-wallet.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PixServiceTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private PixService pixService;

    @Autowired
    private MutableClock clock;

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
        assertEquals(first.endToEndId(), second.endToEndId());
    }

    @Test
    void executePix_imediato_geraEndToEndNoFormatoPix_eUnicoPorTransferencia() {
        TransferDto first = pixService.executePix(new CreatePixRequest(300L, "b1", null, null, null), "unit-e2e-1");
        TransferDto second = pixService.executePix(new CreatePixRequest(300L, "b1", null, null, null), "unit-e2e-2");

        assertTrue(EndToEndIds.FORMATO.matcher(first.endToEndId()).matches(), first.endToEndId());
        assertTrue(EndToEndIds.FORMATO.matcher(second.endToEndId()).matches(), second.endToEndId());
        assertNotEquals(first.endToEndId(), second.endToEndId());
    }

    @Test
    void executePix_agendado_soRecebeEndToEndQuandoExecutado() {
        TransferDto scheduled = pixService.executePix(
                new CreatePixRequest(400L, "b1", null, null, clock.instant().plus(1, ChronoUnit.MINUTES)),
                "unit-e2e-scheduled");
        assertEquals("SCHEDULED", scheduled.status().toString());
        assertNull(scheduled.endToEndId());

        pixService.processDueScheduledTransfers();
        assertEquals("SCHEDULED", pixService.getById(scheduled.id()).status().toString());

        clock.advance(Duration.ofMinutes(2));
        pixService.processDueScheduledTransfers();

        TransferDto executed = pixService.getById(scheduled.id());
        assertEquals("COMPLETED", executed.status().toString());
        assertTrue(EndToEndIds.FORMATO.matcher(executed.endToEndId()).matches(), executed.endToEndId());
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

    @Test
    void executePix_agendadoNoPassado_lancaScheduleInPast() {
        ApiException ex = assertThrows(
                ApiException.class,
                () -> pixService.executePix(
                        new CreatePixRequest(400L, "b1", null, null, clock.instant().minus(1, ChronoUnit.MINUTES)),
                        "unit-schedule-past"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("SCHEDULE_IN_PAST", ex.getCode());
    }

    @Test
    void executePix_agendadoParaAgora_lancaScheduleInPast() {
        ApiException ex = assertThrows(
                ApiException.class,
                () -> pixService.executePix(
                        new CreatePixRequest(400L, "b1", null, null, clock.instant()), "unit-schedule-now"));
        assertEquals("SCHEDULE_IN_PAST", ex.getCode());
    }

    @Test
    void executePix_agendadoAlemDe365Dias_lancaScheduleTooFar() {
        Instant depoisDoLimite = clock.instant().plus(PixService.MAX_SCHEDULE_AHEAD).plusSeconds(1);
        ApiException ex = assertThrows(
                ApiException.class,
                () -> pixService.executePix(
                        new CreatePixRequest(400L, "b1", null, null, depoisDoLimite), "unit-schedule-far"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("SCHEDULE_TOO_FAR", ex.getCode());
    }

    @Test
    void executePix_agendadoNoLimiteDe365Dias_ficaAgendado() {
        TransferDto scheduled = pixService.executePix(
                new CreatePixRequest(400L, "b1", null, null, clock.instant().plus(PixService.MAX_SCHEDULE_AHEAD)),
                "unit-schedule-limit");
        assertEquals("SCHEDULED", scheduled.status().toString());
    }

    @Test
    void executePix_repeticaoDeAgendamentoQueJaVenceu_devolveOOriginal() {
        CreatePixRequest request = new CreatePixRequest(400L, "b1", null, null, clock.instant().plusSeconds(30));
        TransferDto first = pixService.executePix(request, "unit-schedule-replay");
        clock.advance(Duration.ofMinutes(1));

        TransferDto replay = pixService.executePix(request, "unit-schedule-replay");

        assertEquals(first.id(), replay.id());
    }
}
