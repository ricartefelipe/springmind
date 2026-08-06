package dev.springmind.wallet.persistence.repository;

import dev.springmind.wallet.persistence.entity.TransferEntity;
import dev.springmind.wallet.persistence.entity.TransferStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransferRepository extends JpaRepository<TransferEntity, String> {

    @Query("""
            SELECT t FROM TransferEntity t
            WHERE t.status = :status
              AND t.scheduledFor IS NOT NULL
              AND t.scheduledFor <= :now
            """)
    List<TransferEntity> findDueScheduled(
            @Param("status") TransferStatus status, @Param("now") Instant now);
}
