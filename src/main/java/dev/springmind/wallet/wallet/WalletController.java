package dev.springmind.wallet.wallet;

import dev.springmind.wallet.wallet.dto.BalanceResponse;
import dev.springmind.wallet.wallet.dto.TransactionsResponse;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/balance")
    public BalanceResponse getBalance() {
        return walletService.getBalance();
    }

    @GetMapping("/transactions")
    public TransactionsResponse getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize) {
        return walletService.getTransactions(from, to, type, q, page, pageSize);
    }
}
