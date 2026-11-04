package dev.springmind.wallet.common;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /** Relógio do PIX agendado; os testes trocam por um relógio controlado. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
