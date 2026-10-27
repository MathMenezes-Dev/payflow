package com.payflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.payflow.entity.TransferIdempotencyKey;

public interface TransferIdempotencyKeyRepository
        extends JpaRepository<TransferIdempotencyKey, String> {

    @Modifying
    @Query(
        value = "INSERT INTO transfer_idempotency_key (idempotency_key) "
              + "VALUES (:idempotencyKey) "
              + "ON DUPLICATE KEY UPDATE "
              + "idempotency_key = idempotency_key",
        nativeQuery = true
    )
    void lockOrCreate(
            @Param("idempotencyKey") String idempotencyKey);
}