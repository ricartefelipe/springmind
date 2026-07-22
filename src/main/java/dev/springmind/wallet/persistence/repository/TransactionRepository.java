package dev.springmind.wallet.persistence.repository;

import dev.springmind.wallet.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {}
