package dev.springmind.wallet.persistence.repository;

import dev.springmind.wallet.persistence.entity.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<TransferEntity, String> {}
